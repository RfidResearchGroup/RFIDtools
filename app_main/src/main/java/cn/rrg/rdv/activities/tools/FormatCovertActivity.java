package cn.rrg.rdv.activities.tools;

import android.app.AlertDialog;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import cn.dxl.common.widget.FilesSelectorDialog;
import cn.rrg.rdv.R;

import java.io.File;

import cn.rrg.rdv.activities.main.BaseActivity;
import cn.rrg.rdv.presenter.FormatConvertPresenter;
import cn.rrg.rdv.view.FormatConvertView;
import cn.rrg.rdv.util.Commons;
import cn.rrg.rdv.util.Paths;
import cn.dxl.common.util.StringUtil;

public class FormatCovertActivity extends BaseActivity implements FormatConvertView {

    private int mType = 0;
    //中介者对象持有
    private FormatConvertPresenter presenter = null;
    //文件选择器对象
    private FilesSelectorDialog mSelector = null;

    private RadioGroup radioGroupPathSelect = null;

    private Button btnSelectFromQQ = null;
    private Button btnSelectFromWecat = null;
    private Button btnSelectFromSdcard = null;
    private Button btnSelectFromThiz = null;
    private Button btnSelectFromMCT = null;
    private Button btnSelectFromMTools = null;
    private Button btnCovert2Bin = null;
    private Button btnCovert2Txt = null;

    private TextView txtSelectFormAnywhere = null;
    private TextView txtShowFormatDiffrences = null;

    private EditText edtInputSaveName = null;

    private String mSourceFilePath = null;
    private String mSaveFileName = null;
    private String mSavePath = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setContentView(R.layout.act_format_convert);
        super.onCreate(savedInstanceState);

        //初始化中介者对象
        presenter = new FormatConvertPresenter();
        //绑定视图
        presenter.attachView(this);

        //初始化视图
        initViews();
        //初始化文件选择器
        initSelector();
        //初始化动作!
        initActions();
    }

    private void initViews() {
        radioGroupPathSelect = findViewById(R.id.rdoGroupDataPath);

        btnSelectFromQQ = findViewById(R.id.btnDumpSelectFromQQ);
        btnSelectFromSdcard = findViewById(R.id.btnDumpSelectFromSdcard);
        btnSelectFromWecat = findViewById(R.id.btnDumpSelectFromWecat);
        btnSelectFromThiz = findViewById(R.id.btnDumpSelectFromThis);
        btnSelectFromMCT = findViewById(R.id.btnDumpSelectFromMCT);
        btnSelectFromMTools = findViewById(R.id.btnDumpSelectFromMTools);
        btnCovert2Bin = findViewById(R.id.btnConvert2Bin);
        btnCovert2Txt = findViewById(R.id.btnConvert2Txt);

        TextView txtSaveToAnywhere = findViewById(R.id.txtSaveToAnywhere);
        txtSelectFormAnywhere = findViewById(R.id.txtSelectFormAnywhere);
        txtShowFormatDiffrences = findViewById(R.id.txtShowFormatDiffences);

        edtInputSaveName = findViewById(R.id.edtInputFileName);

        Toolbar mToolbar = findViewById(R.id.toolbar_act_format_convert);
        mToolbar.setTitle(R.string.title_format_convert);
        setSupportActionBar(mToolbar);
    }

    private void initActions() {
        btnSelectFromThiz.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSelector.setPathOnLoad(Paths.DUMP_DIRECTORY);
                initSourceFileSelector();
            }
        });
        btnSelectFromSdcard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSelector.setPathOnLoad(Paths.EXTERNAL_STORAGE_DIRECTORY);
                initSourceFileSelector();
            }
        });
        btnSelectFromQQ.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSelector.setPathOnLoad(Paths.QQ_DIRECTORY);
                initSourceFileSelector();
            }
        });
        btnSelectFromWecat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSelector.setPathOnLoad(Paths.WECAT_DIRECTORY);
                initSourceFileSelector();
            }
        });
        btnSelectFromMCT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSelector.setPathOnLoad(Paths.MCT_DUMP_DIRECTORY);
                initSourceFileSelector();
            }
        });
        btnSelectFromMTools.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSelector.setPathOnLoad(Paths.MTools_PATH);
                initSourceFileSelector();
            }
        });
        btnCovert2Bin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkSavePath();
                if (mSourceFilePath == null) {
                    showToast(getString(R.string.tisp_select_convert_target));
                    return;
                }
                if (mSaveFileName == null || mSavePath == null) {
                    showToast(getString(R.string.tisp_select_convert_save_path));
                    return;
                }
                mType = 0;
                presenter.convert2Bin(mSourceFilePath);
            }
        });
        btnCovert2Txt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkSavePath();
                if (mSourceFilePath == null) {
                    showToast(getString(R.string.tisp_select_convert_target));
                    return;
                }
                if (mSaveFileName == null || mSavePath == null) {
                    showToast(getString(R.string.tisp_select_convert_save_path));
                    return;
                }
                mType = 1;
                presenter.convert2Txt(mSourceFilePath);
            }
        });

        txtShowFormatDiffrences.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(FormatCovertActivity.this).setTitle("Format distinction")
                        .setMessage("1.Binary file is a general tag data format, which is the same as\". dump\", and compatible with most software.\n2.The tag data format in text format. is easy to view and can also be used in standard MCT programs.").show();
            }
        });
    }

    private void initSelector() {
        FilesSelectorDialog.Builder builder = new FilesSelectorDialog.Builder(this);
        builder.setTitle(R.string.title_file_select)
                .setCancelable(false)
                .setCanMultiple(false)
                .setPathOnLoad(Paths.DUMP_DIRECTORY);
        mSelector = builder.create();
    }

    private void checkSavePath() {
        String path = Commons.getCustomerPath(radioGroupPathSelect);
        String name = edtInputSaveName.getText().toString();
        if (!StringUtil.isEmpty(name)) {
            //非空文件名。检查是否有/字符，有则为非法!
            if (name.contains("/")) {
                return;
            }
            //进行最终的拼接!
            mSavePath = path;
            mSaveFileName = name;
        } else {
            // FIXME: 2019/5/31 修复空指针闪退!
            if (mSourceFilePath != null) {
                File file = new File(mSourceFilePath);
                mSavePath = file.getParent();
                mSaveFileName = file.getName();
            }
        }
    }

    private void initSourceFileSelector() {
        mSelector.setCanGetPath(false);
        mSelector.setOnSelectListener(new FilesSelectorDialog.OnSelectListener() {
            @Override
            public void selected(File file) {
                mSourceFilePath = file.getAbsolutePath();
                txtSelectFormAnywhere.setText((getString(R.string.res) + file.getName()));
                showToast(getString(R.string.success));
            }
        });
        mSelector.show();
    }

    @Override
    protected void onDestroy() {
        //销毁视图绑定!
        presenter.detachView();
        super.onDestroy();
    }

    @Override
    public void onConvertSucess(byte[] result) {
        //判断结果有效性!
        if (result == null) {
            showToast(getString(R.string.tips_convert_result_null));
            return;
        }
        if (mSavePath == null) {
            showToast(getString(R.string.tips_convert_save_path_null));
            return;
        }
        if (mSaveFileName == null) {
            showToast(getString(R.string.tips_convert_save_name_null));
            return;
        }
        switch (mType) {
            case 0:
                presenter.save2BIN(result, mSavePath, mSaveFileName);
                break;
            case 1:
                presenter.save2TXT(result, mSavePath, mSaveFileName);
                break;
            default:
                break;
        }
    }

    @Override
    public void onConvertFail(String errorMsg) {
        showToast(errorMsg);
    }

    @Override
    public void onSaveSuccess() {
        showToast(getString(R.string.success));
    }

    @Override
    public void onSaveFail(String msg) {
        if (msg != null)
            runOnUiThread(() -> {
                showToast(msg);
            });
    }

    @Override
    public void showToast(String msg) {
        runOnUiThread(() -> {
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void showDialog(String title, String msg) {
    }

    @Override
    public void hideDialog() {
    }
}