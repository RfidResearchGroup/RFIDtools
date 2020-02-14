package cn.rrg.rdv.activities.tools;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import cn.dxl.common.implement.DialogOnclickListenerImpl;
import cn.dxl.common.widget.FilesSelectorDialog;
import cn.rrg.rdv.R;

import java.io.File;
import java.lang.reflect.Method;

import cn.rrg.rdv.activities.main.BaseActivity;
import cn.rrg.rdv.presenter.KeyFilePresenter;
import cn.rrg.rdv.view.KeyFileView;
import cn.dxl.common.util.FileUtil;
import cn.dxl.common.widget.SingleInputDialog;
import cn.rrg.rdv.util.Paths;
import cn.dxl.common.util.MyArrays;

/**
 * Created by DXL on 2018/3/10.
 */
public class KeyFileEditActivity
        extends BaseActivity
        implements KeyFileView {

    private EditText edtKeyFile = null;
    private TextView txtShowEditError = null;
    private TextView txtShowFileName = null;
    private AlertDialog createFileDialog = null;
    private File operaFile = null;
    private KeyFilePresenter presenter = null;
    private String fileNameCache = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_keys_edit);

        //初始化视图与点击事件
        initView();
        //初始化文件创建的对话框
        initFileCreateDialog();

        //初始化中介者对象
        presenter = new KeyFilePresenter();
        presenter.attachView(this);

        //判断是否有数据传输过来
        Bundle _datas_bundle = getIntent().getBundleExtra("keys");
        if (_datas_bundle != null) {
            //在数据封号不为空的情况下，取出其中的密钥组
            String keyStr = _datas_bundle.getString("keyStr");
            if (keyStr != null) {
                showKeyList(keyStr);
            }
        }
    }

    private void initView() {
        Toolbar mToolbar = findViewById(R.id.toolbar_act_keysedt);
        mToolbar.setTitle(R.string.title_keys_editor);
        setSupportActionBar(mToolbar);
        edtKeyFile = findViewById(R.id.editText_edtKeyFile);
        txtShowEditError = findViewById(R.id.txtShowKeyEditError);
        txtShowFileName = findViewById(R.id.txtShowFileName);
    }

    private void initFileCreateDialog() {
        //转换成功后要保存，弹窗提示用户选择路径和输入文件名字
        SingleInputDialog singleInputDialog = new SingleInputDialog(this)
                .setFinishCallback(new SingleInputDialog.OnFininshCallback() {
                    @Override
                    public void onFinish(CharSequence inputs) {
                        String _tmp = inputs.toString();
                        if (_tmp.length() > 0) {
                            fileNameCache = _tmp;
                            presenter.createKeyFile(_tmp);
                        } else {
                            showToast(getString(R.string.input_check));
                        }
                    }
                });
        singleInputDialog.setTitle(getString(R.string.title_plz_input_name));
        singleInputDialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.ok), new DialogOnclickListenerImpl());
        singleInputDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.cancel), new DialogOnclickListenerImpl());
        createFileDialog = singleInputDialog;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在销毁Act时顺带销毁中介持有的引用
        presenter.detachView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //加载我们在menu文件中定义的菜单
        getMenuInflater().inflate(R.menu.activity_keyedt, menu);
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
        //在这里做菜单事件处理!
        FilesSelectorDialog.Builder builder = new FilesSelectorDialog.Builder(this);
        builder.setCancelable(false).setTitle(R.string.title_file_select);
        switch (item.getItemId()) {
            case R.id.menu_key_save:
                if (operaFile != null) {
                    presenter.writeKeyList(edtKeyFile.getText().toString(), operaFile.getPath());
                } else {
                    //当前没有选择要操作的文件，询问用户是否需要新建文件!
                    createFileDialog.show();
                    showToast(getString(R.string.key_file_new_tips));
                }
                break;
            case R.id.menu_key_select:
                builder.setCanMultiple(false)
                        .setOnSelectListener(new FilesSelectorDialog.OnSelectListener() {
                            @Override
                            public void selected(File file) {
                                //缓存当前操作的文件到全局
                                operaFile = file;
                                //显示名字
                                txtShowFileName.setText(operaFile.getName());
                                //在选择了密钥后加载密钥的信息到列表!
                                presenter.showKeyList(file.getPath());
                            }
                        }).setPathOnLoad(Paths.KEY_DIRECTORY)
                        .setIcon(R.drawable.file_select)
                        .create().show();
                txtShowEditError.setText("");
                break;
            case R.id.menu_key_new:
                createFileDialog.show();
                break;
            case R.id.menu_key_del:
                builder.setCanMultiple(true)
                        .setOnSelectsListener(new FilesSelectorDialog.OnSelectsListener() {
                            @Override
                            public void selected(File[] files) {
                                for (File file : files) {
                                    if (file.equals(operaFile)) {
                                        operaFile = null;
                                        txtShowFileName.setText(getString(R.string.no_select));
                                    }
                                    file.delete();
                                }
                            }
                        }).setPathOnLoad(Paths.KEY_DIRECTORY)
                        .setIcon(R.drawable.file_select)
                        .create().show();
                break;
            case R.id.menu_key_repeat: {
                //在编辑板上去重!
                String[] _tmpKeys = edtKeyFile.getText().toString().split("\n");
                if (_tmpKeys.length <= 0) {
                    showToast(getString(R.string.unrepeat_no_line));
                } else {
                    _tmpKeys = MyArrays.unrepeat(_tmpKeys);
                    showToast(getString(R.string.success));
                    StringBuilder sb = new StringBuilder();
                    for (String str : _tmpKeys) {
                        sb.append(str).append('\n');
                    }
                    edtKeyFile.setText(sb.toString());
                }
            }
            break;
            case R.id.menu_key_share:
                builder.setCanMultiple(false)
                        .setOnSelectListener(new FilesSelectorDialog.OnSelectListener() {
                            @Override
                            public void selected(File file) {
                                FileUtil.shareFile(KeyFileEditActivity.this, file);
                            }
                        }).setPathOnLoad(Paths.KEY_DIRECTORY)
                        .setIcon(R.drawable.file_select)
                        .create().show();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
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

    @Override
    public void showKeyList(String key) {
        runOnUiThread(() -> {
            edtKeyFile.setText(key);
        });
    }

    @Override
    public void showKeyError() {
        runOnUiThread(() -> {
            txtShowEditError.setText(getString(R.string.keys_show_failed));
        });
    }

    @Override
    public void onKeysModifySuccess() {
        runOnUiThread(() -> {
            txtShowEditError.setText("");
        });
    }

    @Override
    public void onCreateFileSuccess() {
        if (fileNameCache != null) {
            operaFile = new File(Paths.KEY_DIRECTORY + "/" + fileNameCache);
            presenter.writeKeyList(edtKeyFile.getText().toString(), operaFile.getPath());
            txtShowFileName.setText(operaFile.getName());
        }
        showToast(getString(R.string.success));
    }

    @Override
    public void onCreateFileFailed() {
        showToast(getString(R.string.file_create_failed));
    }
}
