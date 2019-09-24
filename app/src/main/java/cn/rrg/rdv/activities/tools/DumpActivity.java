package cn.rrg.rdv.activities.tools;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;

import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import cn.dxl.common.implement.DialogOnclickListenerImpl;
import cn.dxl.common.widget.FilesSelectorDialog;
import cn.rrg.rdv.R;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;

import cn.rrg.rdv.activities.main.BaseActivity;
import cn.rrg.rdv.javabean.M1Bean;
import cn.rrg.rdv.presenter.DumpPresenter;
import cn.rrg.rdv.view.DumpEditotView;
import cn.rrg.rdv.util.Commons;
import cn.dxl.common.util.ContextUtil;
import cn.rrg.rdv.util.DumpUtils;
import cn.dxl.common.util.FileUtil;
import cn.dxl.common.widget.SingleInputDialog;
import cn.rrg.rdv.util.Paths;
import cn.dxl.common.util.StringUtil;
import cn.dxl.common.util.TextStyleUtil;
import cn.dxl.common.util.ViewUtil;

public class DumpActivity extends BaseActivity implements DumpEditotView {

    /*
     * log tag
     */
    private static final String LOG_TAG = DumpActivity.class.getSimpleName();
    //数据填充容器
    LinearLayout dataWaper = null;
    //数据保存容器
    ArrayList<M1Bean> dataList = new ArrayList<>(256);
    //中介者对象
    private DumpPresenter presenter = null;
    //将要保存的文件
    private File dump = null;
    //被选择编辑的文件名
    private TextView txtShowName = null;
    //单扇区模式下，需要一个扇区
    private int mSector = -1;
    //对话框
    private AlertDialog mDialog = null;
    //文件源选择
    private RadioGroup radioGroupDataPath = null;

    //当前是否是DUMP请求模式!
    private boolean isDUmpRequestMode = false;
    //默认的标准初始化地址!
    private String mPath = null;
    //是否是自定义地址模式!
    private boolean isCustomerPathMode = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setContentView(R.layout.act_dump_edit);
        super.onCreate(savedInstanceState);

        //初始化中介者对象
        presenter = new DumpPresenter();
        //初始化对象持有
        presenter.attachView(this);

        mDialog = new AlertDialog.Builder(this).create();

        initToolbar();

        //初始化控件实例以及动作事件
        initViews();
        initActions();

        //根据意图判断从其他组件传输过来的信息
        Intent intent = getIntent();
        //判断信息类型!
        boolean isFileIntentMode = intent.getBooleanExtra("isFileMode", false);
        //得到意图带的信息
        Bundle _datas_bundle = intent.getBundleExtra("datas");
        //判断是否需要以文件打开!
        if (isFileIntentMode) {
            String path = intent.getStringExtra("file");
            if (path != null) {
                File file = new File(path);
                if (file.exists() && file.isFile()) {
                    //调用底层数据接口请求处理!
                    presenter.showContents(file);
                    return;
                } else {
                    showToast("not file!!");
                }
            } else {
                showToast("data path null!");
                return;
            }
        }
        //判断实体数据是否存在
        if (_datas_bundle != null) {
            showToast("reader data show mode!");
            //取出数据封包
            String[] _datas = _datas_bundle.getStringArray("data_array");
            if (_datas != null) {
                //尝试得到传输过来的单扇区值!
                mSector = _datas_bundle.getInt("sector", -1);
                presenter.showContents(_datas);
            } else {
                showToast("null date.!");
            }
            //判断是否是写卡器请求传输模式!
        } else if (intent.getIntExtra("request_dump", -1) == 1) {
            showToast("request data mode!");
            //取出要被请求的初始化地址
            String path = intent.getStringExtra("path");
            if (path != null) {
                File dir = new File(path);
                if (dir.exists() && dir.isDirectory()) {
                    mPath = path;
                    isCustomerPathMode = true;
                } else {
                    showToast("file no exists.");
                }
            }
            //更改标志位!
            isDUmpRequestMode = true;
            //显示文件选择对话框!
            showFileListDialog();
        } else {
            isDUmpRequestMode = false;
        }
    }

    /*
     * 初始化状态栏
     * */
    private void initToolbar() {
        //工具栏
        Toolbar toolbar = findViewById(R.id.toolbar_act_dumpedt);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }
    }

    /*
     * 初始化视图控件 or 容器实例!
     */
    private void initViews() {
        dataWaper = findViewById(R.id.linearLayout_dumpContent);
        //设置容器视图，抢占焦点，避免一进入act就弹出软键盘
        dataWaper.setFocusable(true);
        dataWaper.setFocusableInTouchMode(true);
        radioGroupDataPath = findViewById(R.id.rdoGroupDataPath);
        txtShowName = findViewById(R.id.txtShowDumpName);
    }

    /*
     * 初始化点击事件
     */
    private void initActions() {
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        //解绑视图，避免空指针
        presenter.attachView(this);
        super.onDestroy();
    }

    /*
     * 显示文件选择框让用户选择要操作的dump文件!
     * */
    private void showFileListDialog() {
        String path = Commons.getCustomerPath(radioGroupDataPath);
        FilesSelectorDialog.Builder builder = new FilesSelectorDialog.Builder(this)
                .setPathOnLoad(Paths.DUMP_DIRECTORY);
        builder.setTitle(R.string.title_select_dump_file)
                .setCancelable(false)
                .setCanMultiple(false)
                .setPathOnLoad(isCustomerPathMode ? mPath : path)
                .setOnSelectListener(new FilesSelectorDialog.OnSelectListener() {
                    @Override
                    public void selected(File file) {
                        //判断用户的选择是否正确!
                        if (file.exists() && file.isFile()) {
                            //缓存到全局的引用!
                            dump = file;
                            //获得文件名，设置在预输入框中
                            txtShowName.setText(file.getName());
                            //中介调用M层方法进行解析显示
                            presenter.showContents(file);
                        }
                    }
                })
                .create()
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //加载我们在menu文件中定义的菜单
        getMenuInflater().inflate(R.menu.activity_dumpedt, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        //显示图标!
        if (menu != null) {
            if (menu.getClass().getSimpleName().equalsIgnoreCase("MenuBuilder")) {
                try {
                    Method method = menu.getClass().getDeclaredMethod("setOptionalIconsVisible", Boolean.TYPE);
                    method.setAccessible(true);
                    method.invoke(menu, true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return super.onMenuOpened(featureId, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //在这里做案件菜单事件处理!
        switch (item.getItemId()) {
            case R.id.menu_dump_edt_save: {
                String path = Commons.getCustomerPath(radioGroupDataPath);
                //建立对话框提示用户需要保存的方式!
                SingleInputDialog singleInputDialog = new SingleInputDialog(this);
                singleInputDialog.setFinishCallback(new SingleInputDialog.OnFininshCallback() {
                    @Override
                    public void onFinish(CharSequence inputs) {
                        String _fileName = inputs.toString();
                        //检查输入
                        if (checkNameInput(_fileName)) {
                            showToast(getString(R.string.input_err));
                            return;
                        }
                        File _file = new File(path + "/" + _fileName);
                        presenter.saveDumpModify(DumpUtils.mergeDatas(dataList), _file);
                    }
                });
                singleInputDialog.setHint("覆盖原文件不需要输入，建立新的文件储存则需要");
                singleInputDialog.setTitle(getString(R.string.title_dump_save_mode));
                singleInputDialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.newFile), new DialogOnclickListenerImpl());
                singleInputDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.cover), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //显示文件选择器对话框
                        new FilesSelectorDialog.Builder(DumpActivity.this)
                                .setTitle(R.string.title_dump_select_cover)
                                .setPathOnLoad(dump != null ? dump.getParent() : path)
                                .setOnSelectListener(new FilesSelectorDialog.OnSelectListener() {
                                    @Override
                                    public void selected(File file) {
                                        presenter.saveDumpModify(DumpUtils.mergeDatas(dataList), file);
                                    }
                                })
                                .setPositiveButtonText(getString(R.string.ok)).create().show();
                    }
                });
                singleInputDialog.show();
            }
            break;
            case R.id.menu_dump_list:
                showFileListDialog();
                break;
            case R.id.menu_dump_extract: {
                //实现密钥的提取功能!
                StringBuilder sb = new StringBuilder();
                String[] keys = DumpUtils.extractKeys(dataList.toArray(new M1Bean[0]));
                for (String key : keys) {
                    sb.append(key).append("\n");
                }
                //封包，发送到密钥编辑组件使用!
                Intent _keysIntent = new Intent(this, KeyFileEditActivity.class);
                Bundle _data_bundle = new Bundle();
                _data_bundle.putString("keyStr", sb.toString());
                _keysIntent.putExtra("keys", _data_bundle);
                startActivity(_keysIntent);
            }
            break;
            case R.id.menu_dump_edt_del:
                String path = Commons.getCustomerPath(radioGroupDataPath);
                new FilesSelectorDialog.Builder(DumpActivity.this)
                        .setTitle(R.string.title_dump_delete)
                        .setCanMultiple(true)
                        .setPathOnLoad(path)
                        .setOnSelectsListener(new FilesSelectorDialog.OnSelectsListener() {
                            @Override
                            public void selected(File[] files) {
                                for (File file : files) {
                                    if (file.isFile()) {
                                        if (file.delete()) {
                                            showToast(getString(R.string.delete_success) + file.getName());
                                        } else {
                                            showToast(getString(R.string.delete_failed) + file.getName());
                                        }
                                    } else {
                                        showToast(getString(R.string.cant_select_not_file));
                                    }
                                }
                            }
                        }).create().show();
                break;
            case R.id.menu_dump_equals:
                //携带数据跳转！
                if (dataList.size() <= 0) {
                    showToast(getString(R.string.data_err));
                    break;
                }
                //直接跳转到对比活动!
                Intent intent = new Intent(this, DumpEqualActivity.class);
                intent.putExtra("dataList", dataList);
                String name = txtShowName.getText().toString();
                intent.putExtra("dataName", StringUtil.isEmpty(name) ? getString(R.string.data_name_dafault) : name);
                startActivity(intent);
                break;
            case R.id.menu_dump_share:
                new FilesSelectorDialog.Builder(DumpActivity.this)
                        .setTitle(R.string.title_shared_dump_file)
                        .setCanMultiple(false)
                        .setPathOnLoad(Paths.DUMP_DIRECTORY)
                        .setOnSelectListener(new FilesSelectorDialog.OnSelectListener() {
                            @Override
                            public void selected(File file) {
                                FileUtil.shareFile(DumpActivity.this, file);
                            }
                        }).create().show();
                break;
            case R.id.menu_dump_rename:
                //重命名!
                new FilesSelectorDialog.Builder(DumpActivity.this)
                        .setTitle(R.string.rename)
                        .setCanMultiple(false)
                        .setPathOnLoad(Paths.DUMP_DIRECTORY)
                        .setOnSelectListener(new FilesSelectorDialog.OnSelectListener() {
                            @Override
                            public void selected(File file) {
                                SingleInputDialog singleInputDialog = new SingleInputDialog(DumpActivity.this);
                                singleInputDialog.setTitle(getString(R.string.newName));
                                singleInputDialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.ok), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        String name = ((SingleInputDialog) dialog).getInput().toString();
                                        if (FileUtil.isValidFileName(name)) {
                                            if (file.renameTo(new File(file.getParent() + "/" + name))) {
                                                showToast(getString(R.string.success));
                                            } else {
                                                showToast(getString(R.string.failed));
                                            }
                                        } else {
                                            showToast(getString(R.string.illegal_filenames));
                                        }
                                    }
                                });
                                singleInputDialog.show();
                            }
                        }).create().show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void showDumpContent(String[] contents) {
        //清除已经处在的数据(被显示出来的)
        dataWaper.removeAllViewsInLayout();
        dataWaper.removeAllViews();
        dataList.clear();

        if (contents == null) {
            showToast(getString(R.string.dump_parse_failed));
            return;
        }
        int count = 4;
        if (contents.length == 64 || contents.length == 128) {
            //1K卡,2K卡!
            M1Bean bean = null;
            String[] _tmps = null;
            for (int i = 0; i < contents.length; i += count) {
                if (DumpUtils.isHeader(i)) {
                    //在首部块!
                    bean = new M1Bean();
                    _tmps = new String[count];
                }
                //进行偏移拷贝!
                if (_tmps != null)
                    System.arraycopy(contents, i, _tmps, 0, count);
                if (bean != null) {
                    bean.setDatas(_tmps);
                    bean.setSector(DumpUtils.toSector(i));
                }
                //结束(将结果添加至集合中)
                dataList.add(bean);
            }
            if (bean == null) {
                showToast(getString(R.string.data_parse_err));
                return;
            }
        }
        if (contents.length == 256) {
            //TODO 4K卡，S70(待实现解析!)
            showToast(getString(R.string.dump_4k_no_support));
            return;
        }
        //单扇区操作处理
        if (contents.length == 4 || contents.length == 16) {
            M1Bean bean = new M1Bean();
            bean.setSector(mSector);
            bean.setDatas(contents);
            dataList.add(bean);
        }
        //直接判断是否有数据需要映射
        if (dataList.size() <= 0) {
            TextView _msg = new TextView(this);
            _msg.setText(getString(R.string.data_fragmentary));
            _msg.setTextColor(new ContextUtil(this).getColor(R.color.md_white_1000));
            dataWaper.addView(_msg);
            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) _msg.getLayoutParams();
            lp.setMarginStart(16);
            return;
        }
        //迭代一下，映射数据到视图
        for (M1Bean bean : dataList) {
            //最终进行动态解析添加!
            View items = ViewUtil.inflate(this, R.layout.mfdata_bean);
            TextView txtSectorNum = items.findViewById(R.id.txtViewShowSectorNum);
            //设置扇区
            txtSectorNum.setText(String.valueOf(bean.getSector()));
            EditText edtContents = items.findViewById(R.id.edtMfData);
            //设置编辑内容保存到list的实现
            edtContents.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    String[] datas = s.toString().split("\n");
                    //根据绑定的bean设置参数
                    bean.setDatas(datas);
                }
            });
            //设置样式
            setAppearanceSpan(bean, edtContents);
            //添加到布局中!
            dataWaper.addView(items);
        }
        //判断模式，是否是写卡器请求dump模式，是的话在当前dump正常时直接结束当前act，返回给定的数据!
        //如果是请求文件模式，则判断数据是否有效，无效则返回cancel码
        if (isDUmpRequestMode) {
            //TODO 此处做出回传动作!
            Intent intent = new Intent();
            intent.putExtra("dataList", dataList);
            String name = txtShowName.getText().toString();
            intent.putExtra("dataName", StringUtil.isEmpty(name) ? getString(R.string.data_name_dafault) : name);
            setResult(RESULT_OK, intent);
            showToast("OK，数据选择完毕，正在结束当前ACT，回传可用的数据!");
            finish();
        }
    }

    @Override
    public void onFileException() {
        showToast(getString(R.string.file_err));
    }

    @Override
    public void onFormatNoSupport() {
        showToast(getString(R.string.format_no_support));
    }

    @Override
    public void onSuccess() {
        showToast(getString(R.string.success));
    }

    private void setAppearanceSpan(M1Bean bean, EditText edt) {
        //拼接块数据
        SpannableStringBuilder ssb = new SpannableStringBuilder();
        String[] datas = bean.getDatas();
        for (int i = 0; i < datas.length; i++) {
            //取出数据!
            String data = datas[i];
            if (data.length() < 32) {
                //长度有异常!
                if (DumpUtils.isHeader(i)) {
                    data = DumpUtils.BLANK_DATA;
                } else {
                    data = DumpUtils.BLANK_TRAIL;
                }
            }
            if (i == datas.length - 1) {
                //尾部块需要进行截取设置，无需最终的换行!!!
                String keyA = data.substring(0, 12);
                String access = data.substring(12, 20);
                String keyB = data.substring(20, 32);
                ssb.append(TextStyleUtil.merge(
                        TextStyleUtil.getStyleString(this, keyA, R.style.KeyStyle),
                        TextStyleUtil.getStyleString(this, access, R.style.AccessStyle),
                        TextStyleUtil.getStyleString(this, keyB, R.style.KeyStyle)
                ));
            } else if (i == 0 && bean.getSector() == 0) {
                //是0扇区并且在首部块,需要设置样式!也需要换行!
                ssb.append(TextStyleUtil.getStyleString(this, data, R.style.ManufacturerStyle)).append("\n");
            } else {
                //数据块!,需要在后面换行!
                ssb.append(datas[i]).append("\n");
            }
        }
        edt.setText(ssb);
    }

    private boolean checkNameInput(String name) {
        return name.length() <= 0 || name.replaceAll(" ", "").length() <= 0;
    }

    @Override
    public void showToast(String msg) {
        runOnUiThread(() -> {
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void showDialog(String title, String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mDialog.isShowing()) mDialog.dismiss();
                mDialog.setTitle(title);
                mDialog.setMessage(msg);
                mDialog.show();
            }
        });
    }

    @Override
    public void hideDialog() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mDialog.isShowing()) mDialog.dismiss();
            }
        });
    }
}
