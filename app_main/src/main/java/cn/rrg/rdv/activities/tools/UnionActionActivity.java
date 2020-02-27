package cn.rrg.rdv.activities.tools;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

import cn.dxl.common.implement.DialogOnclickListenerImpl;
import cn.dxl.common.widget.FilesSelectorDialog;
import cn.rrg.rdv.R;
import cn.rrg.rdv.activities.main.BaseActivity;
import cn.rrg.rdv.callback.KeyFileCallbak;
import cn.rrg.rdv.models.KeyFileModel;
import cn.rrg.rdv.util.DumpUtils;
import cn.dxl.common.util.FileUtil;
import cn.dxl.common.widget.SingleInputDialog;
import cn.rrg.rdv.util.Paths;
import cn.rrg.rdv.util.UnionAction;
import cn.dxl.common.util.StringUtil;
import cn.dxl.common.widget.ToastUtil;

public class UnionActionActivity extends BaseActivity {

    private EditText edtUnionKeyContent = null;
    private Button btnSaveUnionKey = null;
    private Button btnLoadUnionKey = null;
    private TextView txtUnionKeyHelp = null;
    private Context context = this;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setContentView(R.layout.act_union_action);
        super.onCreate(savedInstanceState);

        initViews();
        initActions();
    }

    private void initViews() {
        edtUnionKeyContent = findViewById(R.id.edtUnionKeyContent);
        btnSaveUnionKey = findViewById(R.id.btnSaveUnionKey);
        btnLoadUnionKey = findViewById(R.id.btnLoadUnionKey);
        txtUnionKeyHelp = findViewById(R.id.txtUnionKeyHelp);
    }

    private void initActions() {
        btnSaveUnionKey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(context)
                        .setTitle("保存位置选择").setMessage("您是需要更新到运行内存中还是保存到本地文件呢?")
                        .setPositiveButton("本地", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                SingleInputDialog singleInputDialog = new SingleInputDialog(context)
                                        .setFinishCallback(new SingleInputDialog.OnFininshCallback() {
                                            @Override
                                            public void onFinish(CharSequence inputs) {
                                                //输入的文件名!
                                                String name = inputs.toString();
                                                //具体的密钥内容!
                                                String content = edtUnionKeyContent.getText().toString();
                                                File file = new File(Paths.KEY_DIRECTORY + File.separator + name);
                                                //判断文件是否存在!
                                                if (file.exists()) {
                                                    //直接覆盖写入!
                                                    FileUtil.writeString(file, content, false);
                                                    Toast.makeText(context, "已存在文件,覆盖文件保存完成!", Toast.LENGTH_SHORT).show();
                                                } else {
                                                    //创建文件再写入!
                                                    try {
                                                        if (file.createNewFile()) {
                                                            //创建成功，写入!
                                                            FileUtil.writeString(file, content, false);
                                                            Toast.makeText(context, "不存在文件,新建文件保存完成!", Toast.LENGTH_SHORT).show();
                                                        } else {
                                                            Toast.makeText(context, "创建文件失败，写入失败！", Toast.LENGTH_SHORT).show();
                                                        }
                                                    } catch (IOException e) {
                                                        e.printStackTrace();
                                                    }
                                                }
                                            }
                                        });
                                singleInputDialog.setTitle("输入文件名!");
                                singleInputDialog.setButton(DialogInterface.BUTTON_POSITIVE, "确定", new DialogOnclickListenerImpl());
                                singleInputDialog.show();
                            }
                        })
                        .setNegativeButton("内存", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //更新到内存!
                                String[] keys = edtUnionKeyContent.getText().toString().split("\n");
                                for (String key : keys)
                                    if (DumpUtils.isKeyFormat(key))
                                        UnionAction.addKey(key);
                                //更新后进行刷新到界面!
                                edtUnionKeyContent.setText(StringUtil.arr2Str(UnionAction.getKeys(), "\n", true));
                                Toast.makeText(UnionActionActivity.this, "更新完成!", Toast.LENGTH_SHORT).show();
                            }
                        }).show();
            }
        });
        btnLoadUnionKey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new FilesSelectorDialog.Builder(UnionActionActivity.this)
                        .setTitle("选择加载的文件")
                        .setPathOnLoad(Paths.KEY_DIRECTORY)
                        .setOnSelectListener(new FilesSelectorDialog.OnSelectListener() {
                            @Override
                            public void selected(File file) {
                                KeyFileModel.getKeyString(file.getAbsolutePath(), new KeyFileCallbak.KeyFileReadCallbak() {
                                    @Override
                                    public void onReadSuccess(String msg) {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                edtUnionKeyContent.setText(msg);
                                                ToastUtil.show(UnionActionActivity.this, "加载成功，需要更新到内存请点击保存!", true);
                                            }
                                        });
                                    }

                                    @Override
                                    public void onReadFail() {
                                        ToastUtil.show(UnionActionActivity.this, "读取失败", false);
                                    }
                                });
                            }
                        }).create().show();
            }
        });
        txtUnionKeyHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(context)
                        .setTitle(R.string.help)
                        .setMessage(R.string.help_union_key)
                        .show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        //在运行时每次都要刷新编辑器的值!
        String[] keys = UnionAction.getKeys();
        if (keys.length == 0) {
            Toast.makeText(this, "联动密钥目前未发现有效密钥，您可自行添加!", Toast.LENGTH_SHORT).show();
        } else {
            //设置进去编辑框!
            edtUnionKeyContent.setText(StringUtil.arr2Str(keys, "\n", true));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
