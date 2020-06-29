package cn.rrg.rdv.fragment.tools;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SwitchCompat;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import cn.dxl.common.util.DiskKVUtil;
import cn.dxl.common.util.DisplayUtil;
import cn.dxl.common.util.FileUtils;
import cn.dxl.common.util.FragmentUtil;
import cn.dxl.common.util.HexUtil;
import cn.dxl.common.util.LogUtils;
import cn.dxl.common.widget.FilesSelectorDialog;
import cn.dxl.common.widget.ToastUtil;
import cn.dxl.mifare.MifareClassicUtils;
import cn.rrg.rdv.R;
import cn.rrg.rdv.activities.standard.AbsStandardM1Activity;
import cn.rrg.rdv.activities.tools.DumpEditActivity;
import cn.rrg.rdv.activities.tools.DumpListActivity;
import cn.rrg.rdv.application.Properties;
import cn.rrg.rdv.fragment.base.BaseFragment;
import cn.rrg.rdv.javabean.M1Bean;
import cn.rrg.rdv.javabean.M1KeyBean;
import cn.rrg.rdv.presenter.AbsTagKeysCheckPresenter;
import cn.rrg.rdv.presenter.AbsTagReadPresenter;
import cn.rrg.rdv.presenter.AbsTagStatePresenter;
import cn.rrg.rdv.presenter.AbsTagWritePresenter;
import cn.rrg.rdv.util.Commons;
import cn.rrg.rdv.util.DumpUtils;
import cn.rrg.rdv.util.Paths;
import cn.rrg.rdv.view.MfKeysCheckView;
import cn.rrg.rdv.view.TagReadView;
import cn.rrg.rdv.view.TagStateView;
import cn.rrg.rdv.view.TagWriteView;

/**
 * 封装读写卡操作!
 *
 * @author DXL
 */
public abstract class AbsMfOperatesFragment
        extends BaseFragment
        implements TagStateView, MfKeysCheckView, TagReadView, TagWriteView {

    private enum Opera {
        READ,
        WRITE,
        NOT
    }

    private Opera opera = Opera.NOT;

    private enum Type {
        ALL,
        ONE
    }

    private Type type = Type.ALL;

    protected Button btnRead;
    protected Button btnWrite;
    protected Button btnStop;

    //标签状态支持者
    protected AbsTagStatePresenter tagStatePresenter;
    //标签秘钥检查支持者!
    protected AbsTagKeysCheckPresenter tagKeysCheckPresenter;
    //标签数据读取支持者!
    protected AbsTagReadPresenter tagReadPresenter;
    //标签数据写入支持者!
    protected AbsTagWritePresenter tagWritePresenter;

    //请求选择DUMP数据的请求码!
    private final int DUMP_REQUEST_CODE = 1;

    //文件选择器对话框
    private FilesSelectorDialog mFileSelector = null;
    //在程序执行读卡，写卡操作时的程序后台交互性提醒对话框
    private AlertDialog mDialogWorkingState = null;
    //在标签出现问题，或者未放置标签时的提醒框
    private AlertDialog mTagAbnormalDialog = null;

    //通用
    private TextView txtPN53X_MF_SelectKeys = null;
    private TextView txtPN53X_MF_SelectDump = null;
    private SwitchCompat swAllowWriteZero = null;
    private TextView txtShowKeyFileSelectedList = null;
    private ProgressBar keysCheckProgressBar = null;
    private TextView txtShowKeys = null;

    //读卡区域选项
    private EditText edtInputReadTagSingleSector = null;
    private SwitchCompat ckBoxReadTagIsSingleSectorMode = null;
    private LinearLayout llInputReadTagSingleSector = null;

    //写卡区域选项
    private SwitchCompat swOpenWriteStart2EndMode = null;
    private EditText edtInputWriteTagSingleSector = null;
    private EditText edtInputWriteTagSingleBlock = null;
    private EditText edtInputWriteTagSingleBlockData = null;
    private SwitchCompat ckBoxWriteTagIsSingleSectorMode = null;
    private LinearLayout llInputWriteTagSingleSector = null;

    /*
     * 维护一个读取成功的数据的缓存集合
     * 或者这个集合中的数据可以用来写卡
     * TODO 你必须明白在不同模式下该怎么使用这个集合
     * */
    private ArrayList<M1Bean> mDataBean = new ArrayList<>(64);

    // 通用操作中，被选择的秘钥文件(正常读写卡需要秘钥!)
    private ArrayList<File> keyFilesSelectedList = new ArrayList<>();

    private M1KeyBean[] keyBeans = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.act_mf_main, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //初始化标签状态中介者!
        tagStatePresenter = getTagStatePresenter();
        tagStatePresenter.attachView(this);

        //初始化秘钥检索中介者!
        tagKeysCheckPresenter = getKeysCheckPresenter();
        tagKeysCheckPresenter.attachView(this);

        //初始化标准读卡中介者!
        tagReadPresenter = getTagReadPresenter();
        tagReadPresenter.attachView(this);

        //初始化标准写卡中介者!
        tagWritePresenter = getTagWritePresenter();
        tagWritePresenter.attachView(this);

        Context context = getContext();
        if (context != null) {
            mFileSelector = new FilesSelectorDialog.Builder(context).create();

            mDialogWorkingState = new AlertDialog.Builder(context).create();
            View _workingStateMsgView = View.inflate(context, R.layout.dialog_working_msg, null);
            mDialogWorkingState.setView(_workingStateMsgView);
            mDialogWorkingState.setTitle(R.string.tips);
            mDialogWorkingState.setCancelable(false);

            mTagAbnormalDialog = new AlertDialog.Builder(context)
                    .setTitle(R.string.error)
                    .setMessage(R.string.tag_not_found).create();
        }

        //初始化视图实例
        initViews(view);
        //初始化相关的事件
        initActions();
        //初始化历史选择的秘钥文件
        initSelected();

        // 检查卡片状态!
        showWorkDialogNotEnableBtn();
        tagStatePresenter.check();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //此处可以解析从DUMP文件操作ACT传过来的被选择的dump文件!
        if (requestCode == DUMP_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            //得到一个数据集合，进行解析添加操作，在进行这项操作之前，先清空旧的缓存！
            mDataBean.clear();
            String path = data.getStringExtra("file");
            if (path == null) throw new RuntimeException("The file path is null.");
            File fileTmp = new File(path);
            M1Bean[] dataBeans = DumpUtils.readDumpBeans(Uri.fromFile(fileTmp));
            if (dataBeans == null) throw new RuntimeException("The dump read exception!");
            mDataBean.addAll(Arrays.asList(dataBeans));
            String name = fileTmp.getName();
            //自动的提取其中的密钥到列表
            String[] keys = DumpUtils.extractKeys(mDataBean.toArray(new M1Bean[0]));
            // 优化一下，将dump中的key提取，存放到一个文件中，并且将其设置到秘钥文件队列中!
            String keyLine = DumpUtils.mergeTxt(keys, true, "\n");
            if (keyLine != null) {
                // 创建文件!
                File file = new File(Paths.KEY_DIRECTORY + "/" + "write_optimization.txt");
                // 写入到以UID为名的文件中，并且加入到队列!
                FileUtils.writeString(file, keyLine, false);
                if (!keyFilesSelectedList.contains(file)) keyFilesSelectedList.add(file);
            }
            txtPN53X_MF_SelectDump.setText(name);
        } else if (requestCode == DUMP_REQUEST_CODE && resultCode == Activity.RESULT_CANCELED) {
            mDataBean.clear();
            txtPN53X_MF_SelectDump.setText(R.string.msg_data_invalid);
            showToast(getString(R.string.msg_data_invalid));
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void initViews(View view) {
        // 最终的读写需要做的!
        btnRead = view.findViewById(R.id.btnReadTag);
        btnWrite = view.findViewById(R.id.btnWriteTag);
        btnStop = view.findViewById(R.id.btnStop);
        //init view
        txtPN53X_MF_SelectKeys = view.findViewById(R.id.txtPN53X_MF_SELECT_KEY_FILES);
        txtPN53X_MF_SelectDump = view.findViewById(R.id.txtPN53X_MF_SELECT_DUMP_FILE);
        txtShowKeyFileSelectedList = view.findViewById(R.id.txtShowKeyFileSelectedList);
        keysCheckProgressBar = view.findViewById(R.id.progressBar_onAuth);
        txtShowKeys = view.findViewById(R.id.txtShowKeys);
        //读取区域
        edtInputReadTagSingleSector = view.findViewById(R.id.edtInputReadTagSingleSector);
        ckBoxReadTagIsSingleSectorMode = view.findViewById(R.id.ckBoxReadTagIsSingleSectorMode);
        llInputReadTagSingleSector = view.findViewById(R.id.llInputReadTagSingleSector);
        //写卡区域
        swAllowWriteZero = view.findViewById(R.id.swAllowWriteZero);
        swOpenWriteStart2EndMode = view.findViewById(R.id.swOpenWriteStart2EndMode);
        edtInputWriteTagSingleSector = view.findViewById(R.id.edtInputWriteTagSingleSector);
        edtInputWriteTagSingleBlock = view.findViewById(R.id.edtInputWriteTagSingleBlock);
        edtInputWriteTagSingleBlockData = view.findViewById(R.id.edtInputWriteTagSingleBlockData);
        ckBoxWriteTagIsSingleSectorMode = view.findViewById(R.id.ckBoxWriteTagIsSingleSectorMode);
        llInputWriteTagSingleSector = view.findViewById(R.id.llInputWriteTagSingleSector);
    }

    private void initActions() {
        ckBoxReadTagIsSingleSectorMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    llInputReadTagSingleSector.setVisibility(View.VISIBLE);
                } else {
                    llInputReadTagSingleSector.setVisibility(View.GONE);
                }
            }
        });
        ckBoxWriteTagIsSingleSectorMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    llInputWriteTagSingleSector.setVisibility(View.VISIBLE);
                } else {
                    llInputWriteTagSingleSector.setVisibility(View.GONE);
                }
            }
        });

        // 选择读卡操作!
        btnRead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showWorkDialogNotEnableBtn();
                // 更新必要信息！
                opera = Opera.READ;
                // 然后调用卡片状态检查的中介实现!
                tagStatePresenter.check();
            }
        });

        // 选择写卡操作!
        btnWrite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showWorkDialogNotEnableBtn();
                // 更新必要信息！
                opera = Opera.WRITE;
                // 然后调用卡片状态检查的中介实现!
                tagStatePresenter.check();
            }
        });

        // 选择密钥文件!
        txtPN53X_MF_SelectKeys.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateKeyFileSelectedName();
                mFileSelector.setPathOnLoad(Paths.KEY_DIRECTORY);
                mFileSelector.setCanMultiple(true);
                mFileSelector.addFileSelected(keyFilesSelectedList.toArray(new File[0]));
                mFileSelector.setOnUnSelectListener(new FilesSelectorDialog.OnUnSelectListener() {
                    @Override
                    public void onUnSelect(File[] file) {
                        for (File tmpFile : file) {
                            keyFilesSelectedList.remove(tmpFile);
                            // 从本地持久域中删除
                            Commons.delKeyFileSelected(tmpFile.getAbsolutePath());
                        }
                    }
                });
                mFileSelector.setOnSelectsListener(new FilesSelectorDialog.OnSelectsListener() {
                    @Override
                    public void selected(File[] files) {
                        if (files == null) return;
                        for (File tmp : files) {
                            // TODO 因为有时候，历史记录的选择列表会和选择的有重复，所以我们这里需要进行判断!
                            if (!keyFilesSelectedList.contains(tmp)) {
                                keyFilesSelectedList.add(tmp);
                                // 添加到本地持久域!
                                Commons.addKeyFileSelect(tmp.getAbsolutePath());
                                Log.d(LOG_TAG, "添加了秘钥文件: " + tmp.getName());
                            }
                        }
                        updateKeyFileSelectedName();
                    }
                });
                mFileSelector.setOnNoSelectListener(new FilesSelectorDialog.OnNoSelectListener() {
                    @Override
                    public void noSelect() {
                        txtShowKeyFileSelectedList.setText(R.string.keyfiles_no_select);
                    }
                });
                mFileSelector.show();
            }
        });

        // 停止操作
        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tagKeysCheckPresenter.stopChecks();
                txtShowKeys.setText("");
                keysCheckProgressBar.setProgress(0);
            }
        });

        /*
         * 写卡模式下在直接进入写卡act时（非dump编辑界面携带扇区数据传输过来时）
         * 则需要在此处选择dump文件!
         * */
        txtPN53X_MF_SelectDump.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //构建意图!
                Intent intent = new Intent(getContext(), DumpListActivity.class);
                intent.putExtra("mode", DumpListActivity.MODE.SELECT);
                startActivityForResult(intent, DUMP_REQUEST_CODE);
            }
        });
    }

    private void initSelected() {
        String[] ss = Commons.getKeyFilesSelected().toArray(new String[0]);
        for (String v : ss) {
            File f = new File(v);
            if (f.exists() && f.isFile()) {
                keyFilesSelectedList.add(new File(v));
            }
        }
        updateKeyFileSelectedName();
    }

    private void updateKeyFileSelectedName() {
        if (keyFilesSelectedList.size() > 0) {
            // Show!
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < keyFilesSelectedList.size(); i++) {
                if (i != keyFilesSelectedList.size() - 1) {
                    sb.append(keyFilesSelectedList.get(i).getName()).append("\n-\n");
                } else {
                    sb.append(keyFilesSelectedList.get(i).getName());
                }
            }
            txtShowKeyFileSelectedList.setText(sb);
        } else {
            txtShowKeyFileSelectedList.setText("");
        }
    }

    private void delSelected(String file) {
        Log.d(LOG_TAG, "删除了本地保存的选择记录: " + file);
        File settingsFile = new File(Paths.SETTINGS_FILE);
        try {
            if (DiskKVUtil.isKVExists(Properties.k_common_rw_keyfile_selected, file, settingsFile)) {
                DiskKVUtil.deleteKV(Properties.k_common_rw_keyfile_selected, file, settingsFile);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void enableRWButton(boolean enable) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    btnRead.setEnabled(enable);
                    btnWrite.setEnabled(enable);
                }
            });
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        tagStatePresenter.detachView();
        tagKeysCheckPresenter.detachView();
        tagReadPresenter.detachView();
        tagWritePresenter.detachView();
    }

    @Override
    public File[] getKeyFiles() {
        if (keyFilesSelectedList.size() == 0)
            return new File[0];
        else
            return keyFilesSelectedList.toArray(new File[0]);
    }

    @Override
    public void onStart(int sectorCount) {
        keysCheckProgressBar.setMax(sectorCount);
    }

    @Override
    public void onKeysInvalid() {
        ToastUtil.show(getContext(), getString(R.string.keys_invalid), false);
        FragmentUtil.runOnUiThread(getActivity(), new Runnable() {
            @Override
            public void run() {
                DisplayUtil.setShakeAnimation(txtPN53X_MF_SelectKeys);
                dismissWorkDialogAndEnableBtn();
                setCanScrollPager(true);
            }
        });
    }

    @Override
    public void onAuth(int sectorRemains) {
        // 进度条拉伸!
        Log.d(LOG_TAG, "扇区总数: " + keysCheckProgressBar.getMax());
        Log.d(LOG_TAG, "剩余扇区: " + sectorRemains);
        FragmentUtil.runOnUiThread(getActivity(), new Runnable() {
            @Override
            public void run() {
                keysCheckProgressBar.setProgress(keysCheckProgressBar.getMax() - sectorRemains);
            }
        });
    }

    @Override
    public void onKeys(String key) {
        FragmentUtil.runOnUiThread(getActivity(), new Runnable() {
            @Override
            public void run() {
                txtShowKeys.setText(key);
                if (getActivity() != null) {
                    ((AbsStandardM1Activity) getActivity()).setCanChange(false);
                }
            }
        });
    }

    @Override
    public void onResults(M1KeyBean[] keyBeans) {
        FragmentUtil.runOnUiThread(getActivity(), new Runnable() {
            @Override
            public void run() {
                showWorkDialogNotEnableBtn();
            }
        });
        //缓存到全局存放!
        this.keyBeans = keyBeans;
        // 当前肯定是正常的读写操作，我们需要判断并且调用!
        ToastUtil.show(getContext(), getString(R.string.msg_keys_auth_finish) + keyBeans.length, false);
        if (opera == Opera.READ) {
            if (type == Type.ALL) {
                // 读整卡!
                tagReadPresenter.readNormallAll();
            } else {
                // 读扇区!
                tagReadPresenter.readNormallOne();
            }
        } else {
            if (type == Type.ALL) {
                // 写整卡!
                tagWritePresenter.writeNormallAll();
            } else {
                // 写单块!
                tagWritePresenter.writeNormallOne();
            }
        }
        //读正常卡
        for (M1KeyBean keyBean : keyBeans) {
            Log.d(LOG_TAG, "扇区: " + keyBean.getSector() + " 秘钥A: " + keyBean.getKeyA() + " 秘钥B" + keyBean.getKeyB());
        }
    }

    @Override
    public void onTagAbnormal() {
        FragmentUtil.runOnUiThread(getActivity(), new Runnable() {
            @Override
            public void run() {
                dismissWorkDialogAndEnableBtn();
                mTagAbnormalDialog.show();
            }
        });
        setCanScrollPager(true);
    }

    @Override
    public void onTagOrdinary() {
        // 在普通标签发现时的回调
        if (opera == Opera.READ) { // 判断输入是否有效!
            dismissWorkDialogNotEnableBtn();
            setCanScrollPager(false);
            boolean isReadSingleSectorMode = ckBoxReadTagIsSingleSectorMode.isChecked();
            type = isReadSingleSectorMode ? Type.ONE : Type.ALL;
            if (isReadSingleSectorMode && TextUtils.isEmpty(edtInputReadTagSingleSector.getText())) {
                ToastUtil.show(getContext(), getString(R.string.invalid_input), false);
                DisplayUtil.setShakeAnimation(edtInputReadTagSingleSector);
                dismissWorkDialogAndEnableBtn();
                return;
            }
        }
        if (opera == Opera.WRITE) {
            dismissWorkDialogNotEnableBtn();
            setCanScrollPager(false);
            boolean isReadSingleSectorMode = ckBoxWriteTagIsSingleSectorMode.isChecked();
            type = isReadSingleSectorMode ? Type.ONE : Type.ALL;
            // 校验输入!
            if (type == Type.ALL) {
                // 读所有的话，校验一下数据就行了!
                if (mDataBean.size() == 0) {
                    ToastUtil.show(getContext(), getString(R.string.data_invalid), false);
                    DisplayUtil.setShakeAnimation(txtPN53X_MF_SelectDump);
                    dismissWorkDialogAndEnableBtn();
                    return;
                }
            } else {
                // 校验扇区，块，数据的输入
                try {
                    Integer.valueOf(edtInputWriteTagSingleSector.getText().toString());
                } catch (Exception ignored) {
                    ToastUtil.show(getContext(), getString(R.string.msg_sector_invalid), false);
                    DisplayUtil.setShakeAnimation(edtInputWriteTagSingleSector);
                    dismissWorkDialogAndEnableBtn();
                    return;
                }
                try {
                    Integer.valueOf(edtInputWriteTagSingleBlock.getText().toString());
                } catch (Exception ignored) {
                    ToastUtil.show(getContext(), getString(R.string.msg_block_invalid), false);
                    DisplayUtil.setShakeAnimation(edtInputWriteTagSingleBlock);
                    dismissWorkDialogAndEnableBtn();
                    return;
                }
                if (!HexUtil.isHexString(edtInputWriteTagSingleBlockData.getText().toString())) {
                    ToastUtil.show(getContext(), getString(R.string.data_invalid), false);
                    DisplayUtil.setShakeAnimation(edtInputWriteTagSingleBlockData);
                    dismissWorkDialogAndEnableBtn();
                    return;
                }
            }
        }
        if (opera != Opera.NOT) {
            Log.d(LOG_TAG, "开始检索秘钥!");
            tagKeysCheckPresenter.startCheck();
        } else {
            dismissWorkDialogAndEnableBtn();
            setCanScrollPager(true);
            showToast(getString(R.string.msg_normal_tag_found));
        }
    }

    @Override
    public void onTagSpecial() {
        dismissWorkDialogNotEnableBtn();
        // 在特殊标签发现时的回调，不需要秘钥验证，直接进行相关的操作!
        if (opera == Opera.READ) {
            dismissWorkDialogNotEnableBtn();
            setCanScrollPager(false);
            //直接读卡
            boolean isReadSingleSectorMode = ckBoxReadTagIsSingleSectorMode.isChecked();
            type = isReadSingleSectorMode ? Type.ONE : Type.ALL;
            if (type == Type.ALL) {
                tagReadPresenter.readSpecialAll();
            } else {
                tagReadPresenter.readSpecialOne();
            }
        }
        if (opera == Opera.WRITE) {
            dismissWorkDialogNotEnableBtn();
            setCanScrollPager(false);
            //直接写卡!
            boolean isReadSingleSectorMode = ckBoxWriteTagIsSingleSectorMode.isChecked();
            type = isReadSingleSectorMode ? Type.ONE : Type.ALL;
            if (type == Type.ALL) {
                tagWritePresenter.writeSpecialAll();
            } else {
                tagWritePresenter.writeSpecoalOne();
            }
        }
        if (opera == Opera.NOT) {
            dismissWorkDialogAndEnableBtn();
            setCanScrollPager(true);
            showToast(getString(R.string.msg_special_tag));
        }
    }

    private void setCanScrollPager(boolean can) {
        // 有任务进行，我们需要进行fragment禁止切换!
        if (getActivity() != null) {
            ((AbsStandardM1Activity) getActivity()).setCanChange(can);
        }
    }

    private void showWorkDialogNotEnableBtn() {
        FragmentUtil.runOnUiThread(getActivity(), new Runnable() {
            @Override
            public void run() {
                mDialogWorkingState.show();
                enableRWButton(false);
            }
        });
    }

    private void dismissWorkDialogAndEnableBtn() {
        FragmentUtil.runOnUiThread(getActivity(), new Runnable() {
            @Override
            public void run() {
                mDialogWorkingState.dismiss();
                enableRWButton(true);
            }
        });
    }

    private void dismissWorkDialogNotEnableBtn() {
        FragmentUtil.runOnUiThread(getActivity(), new Runnable() {
            @Override
            public void run() {
                mDialogWorkingState.dismiss();
                enableRWButton(false);
            }
        });
    }

    @Override
    public void onReadFinish(M1Bean[] datas) {
        dismissWorkDialogAndEnableBtn();
        setCanScrollPager(true);
        if (datas == null) {
            Log.d(LOG_TAG, "数据集为空!");
            ToastUtil.show(getContext(), getString(R.string.failed), false);
            return;
        }
        for (M1Bean b : datas) {
            LogUtils.d(b.toString());
        }
        Log.d(LOG_TAG, "数据集长度: " + datas.length);
        if (datas.length == 0) {
            ToastUtil.show(getContext(), getString(R.string.mag_read_failed), false);
            return;
        }
        mDataBean.clear();
        mDataBean.addAll(Arrays.asList(datas));
        String[] _datas = DumpUtils.mergeDatas(mDataBean);
        if (_datas != null) {
            Intent _intent_to_dump_act = new Intent(getContext(), DumpEditActivity.class);
            if (datas.length == 1) {
                _intent_to_dump_act.putExtra("sector", datas[0].getSector());
            }
            _intent_to_dump_act.putExtra("data_array", _datas);
            startActivity(_intent_to_dump_act);
        } else {
            showToast(getString(R.string.msg_failed_merge_data));
        }
    }

    @Override
    public M1KeyBean[] getKeyBeanForOne() {
        if (keyBeans == null || keyBeans.length == 0) {
            return null;
        } else {
            ArrayList<M1KeyBean> ret = new ArrayList<>();
            int sector = -1;
            try {
                // TODO 后期可以做类似MCT的多扇区(非全部扇区)或者单扇区读写实现!
                //  目前只返回用户输入的扇区即可!
                sector = Integer.parseInt(edtInputReadTagSingleSector.getText().toString());
                if (!MifareClassicUtils.validateSector(sector)) sector = -1;
            } catch (Exception ignored) {
            }
            for (M1KeyBean keyBean : keyBeans) {
                if (keyBean.getSector() == sector) {
                    ret.add(keyBean);
                }
            }
            return ret.toArray(new M1KeyBean[0]);
        }
    }

    @Override
    public M1KeyBean[] getKeyBeanForAll() {
        return keyBeans;
    }

    @Override
    public boolean isWriteManufacturerAllow() {
        return swAllowWriteZero.isChecked();
    }

    @Override
    public boolean isWriteSecOrderImplement() {
        return swOpenWriteStart2EndMode.isChecked();
    }

    @Override
    public int[] getReadeSectorSelected() {
        int i = -1;
        try {
            i = Integer.parseInt(edtInputReadTagSingleSector.getText().toString());
        } catch (Exception ignored) {
        }
        // 暂时只返回一个扇区，后期可以做多个扇区同时选择!
        return new int[]{i};
    }

    @Override
    public void onWriteFinish() {
        FragmentUtil.runOnUiThread(getActivity(), new Runnable() {
            @Override
            public void run() {
                dismissWorkDialogAndEnableBtn();
                setCanScrollPager(true);
            }
        });
        ToastUtil.show(getContext(), getString(R.string.msg_wite_finish), false);
    }

    @Override
    public void onDataInvalid() {
        dismissWorkDialogAndEnableBtn();
        setCanScrollPager(true);
        ToastUtil.show(getContext(), getString(R.string.data_invalid), false);
    }

    @Override
    public int getSector() {
        int sector = -1;
        try {
            sector = Integer.parseInt(edtInputWriteTagSingleSector.getText().toString());
            return sector;
        } catch (Exception ignored) {
        }
        return sector;
    }

    @Override
    public int getBlock() {
        int block = -1;
        try {
            block = Integer.parseInt(edtInputWriteTagSingleBlock.getText().toString());
            return block;
        } catch (Exception ignored) {
        }
        return block;
    }

    @Override
    public String getData() {
        String hexStr = edtInputWriteTagSingleBlockData.getText().toString();
        return HexUtil.isHexString(hexStr) ? hexStr : null;
    }

    @Override
    public M1Bean[] getDatas() {
        M1Bean[] tmp = mDataBean.toArray(new M1Bean[0]);
        if (tmp.length > 0) return tmp;
        return null;
    }

    /**
     * 子类必须实现，使父类可以进行标签状态支持者的获取!
     *
     * @return 标签状态支持者，用于支持查询标签的具体状态!
     */
    protected abstract AbsTagStatePresenter getTagStatePresenter();

    /**
     * 子类必须实现，使父类可以进行标签秘钥支持者的获取!
     *
     * @return 秘钥检索支持者，用于支持检索秘钥!
     */
    protected abstract AbsTagKeysCheckPresenter getKeysCheckPresenter();

    /**
     * 子类必须实现，使父类可以进行标签读取支持者的获取!
     *
     * @return 秘钥检索支持者，用于支持检索秘钥!
     */
    protected abstract AbsTagReadPresenter getTagReadPresenter();

    /**
     * 子类必须实现，使父类可以进行标签写入支持者的获取!
     *
     * @return 秘钥检索支持者，用于支持检索秘钥!
     */
    protected abstract AbsTagWritePresenter getTagWritePresenter();
}
