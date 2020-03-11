package cn.rrg.rdv.activities.tools;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;

import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import cn.rrg.rdv.R;

import java.io.File;

import cn.rrg.rdv.activities.main.BaseActivity;
import cn.rrg.rdv.presenter.KeyFilePresenter;
import cn.rrg.rdv.view.KeyFileView;
import cn.rrg.rdv.util.Paths;

/**
 * Created by DXL on 2018/3/10.
 */
public class KeyFileEditActivity
        extends BaseActivity
        implements KeyFileView {

    private EditText edtKeyFile = null;
    private TextView txtShowEditError = null;
    private File operaFile = null;
    private KeyFilePresenter presenter = null;
    private String fileNameCache = null;
    private ImageButton btnSave = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_keys_edit);

        //初始化视图与点击事件
        initView();
        initActions();

        //初始化中介者对象
        presenter = new KeyFilePresenter();
        presenter.attachView(this);

        Intent intent = getIntent();
        String tmpPath = intent.getStringExtra("file");
        if (tmpPath != null) {
            operaFile = new File(tmpPath);
            if (!operaFile.isFile()) {
                throw new RuntimeException("Param not a file.");
            }
        } else {
            throw new RuntimeException("No file param transmit.");
        }
        presenter.showKeyList(tmpPath);
    }

    private void initView() {
        edtKeyFile = findViewById(R.id.editText_edtKeyFile);
        txtShowEditError = findViewById(R.id.txtShowKeyEditError);
        btnSave = findViewById(R.id.btnSaveChange);
    }

    private void initActions() {
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.writeKeyList(edtKeyFile.getText().toString(), operaFile.getPath());
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在销毁Act时顺带销毁中介持有的引用
        presenter.detachView();
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
        }
        showToast(getString(R.string.success));
    }

    @Override
    public void onCreateFileFailed() {
        showToast(getString(R.string.file_create_failed));
    }
}
