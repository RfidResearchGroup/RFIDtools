package cn.rrg.rdv.activities.tools;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import cn.dxl.common.util.ContextUtil;
import cn.dxl.common.util.FileUtils;
import cn.dxl.common.util.LogUtils;
import cn.dxl.common.util.StringUtil;
import cn.dxl.common.util.TextStyleUtil;

import cn.dxl.common.widget.SingleInputDialog;
import cn.rrg.rdv.R;
import cn.rrg.rdv.activities.main.BaseActivity;
import cn.rrg.rdv.implement.TextWatcherImpl;
import cn.rrg.rdv.javabean.M1Bean;
import cn.rrg.rdv.presenter.DumpPresenter;
import cn.rrg.rdv.util.Commons;
import cn.rrg.rdv.util.DumpUtils;
import cn.rrg.rdv.util.Paths;
import cn.rrg.rdv.view.DumpEditotView;

public class DumpEditActivity extends BaseActivity implements DumpEditotView {
    /*
     * log tag
     */
    private static final String LOG_TAG = DumpEditActivity.class.getSimpleName();
    //数据填充容器
    LinearLayout dataWaper = null;
    //数据保存容器
    ArrayList<M1Bean> dataList = new ArrayList<>(256);
    //中介者对象
    private DumpPresenter presenter = null;
    //对话框
    private AlertDialog mDialog = null;
    // 保存编辑的内容!
    private ImageButton btnSaveChange;
    // 展示与保存的文件!
    private File file;
    // 字符串数据类型的数据!
    private String[] datas;
    // 单个扇区显示!
    private int sector;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setContentView(R.layout.act_dump_edit);
        super.onCreate(savedInstanceState);

        setStatus(true);

        //初始化中介者对象
        presenter = new DumpPresenter();
        //初始化对象持有
        presenter.attachView(this);

        mDialog = new AlertDialog.Builder(this).create();

        //初始化控件实例以及动作事件
        initViews();
        initActions();

        // TODO 需要实现相关的Dump信息展示与保存
        //根据意图判断从其他组件传输过来的信息
        Intent intent = getIntent();
        String path = intent.getStringExtra("file");
        //判断是否需要以文件打开!
        if (path != null) {
            file = new File(path);
            if (file.exists() && file.isFile()) {
                //调用底层数据接口请求处理!
                presenter.showContents(file);
            } else {
                showToast("not file!!");
                finish();
            }
        }
        datas = intent.getStringArrayExtra("data_array");
        sector = intent.getIntExtra("sector", -1);
        if (datas != null) {
            presenter.showContents(datas);
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
        btnSaveChange = findViewById(R.id.btnSaveChange);
    }

    /*
     * 初始化点击事件
     */
    private void initActions() {
        btnSaveChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (file == null) {
                    SingleInputDialog dialog = new SingleInputDialog(context).setTips(R.string.title_plz_input_name);
                    dialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface d, int which) {
                            String name = ((SingleInputDialog) d).getInput().toString();
                            if (FileUtils.isValidFileName(name)) {
                                File f = Commons.createInternalDump(name + ".dump");
                                presenter.saveDumpModify(DumpUtils.mergeDatas(dataList), f);
                            } else {
                                showToast(getString(R.string.input_err));
                            }
                        }
                    });
                    dialog.show();
                } else {
                    // 调用终结者请求写入!
                    presenter.saveDumpModify(DumpUtils.mergeDatas(dataList), file);
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        //解绑视图，避免空指针
        presenter.attachView(this);
        super.onDestroy();
    }

    @Override
    public void showDumpContent(String[] contents) {
        //清除已经处在的数据(被显示出来的)
        dataWaper.removeAllViewsInLayout();
        dataWaper.removeAllViews();
        dataList.clear();
        dataList.addAll(Arrays.asList(DumpUtils.getSectorFromArray(contents)));
        //直接判断是否有数据需要映射
        if (dataList.size() <= 0) {
            TextView _msg = new TextView(this);
            _msg.setText(getString(R.string.data_fragmentary));
            _msg.setTextColor(ContextUtil.getColor(R.color.md_white_1000));
            dataWaper.addView(_msg);
            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) _msg.getLayoutParams();
            lp.setMarginStart(16);
            return;
        }
        // Set sector to bean from single action sector.
        if (sector != -1 && dataList.size() == 1) {
            dataList.get(0).setSector(sector);
        }
        //迭代一下，映射数据到视图
        for (M1Bean bean : dataList) {
            //最终进行动态解析添加!
            View items = View.inflate(this, R.layout.mfdata_bean, null);
            TextView txtSectorNum = items.findViewById(R.id.txtViewShowSectorNum);
            //设置扇区
            txtSectorNum.setText(String.valueOf(bean.getSector()));
            EditText edtContents = items.findViewById(R.id.edtMfData);
            //设置编辑内容保存到list的实现
            edtContents.addTextChangedListener(new TextWatcherImpl() {
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
    }

    @Override
    public void onFileException() {
        showToast(getString(R.string.failed));
    }

    @Override
    public void onFormatNoSupport() {
        showToast(getString(R.string.format_no_support));
    }

    @Override
    public void onSuccess() {
        showToast(getString(R.string.successful));
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
                    data = DumpUtils.BLANK_TRAIL_BLOCK;
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
