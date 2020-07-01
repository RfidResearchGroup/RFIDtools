package cn.rrg.rdv.activities.tools;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;

import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;

import java.io.File;
import java.io.IOException;

import cn.rrg.rdv.R;
import cn.rrg.rdv.activities.main.BaseActivity;
import cn.rrg.rdv.util.Paths;
import cn.rrg.rdv.application.Properties;
import cn.dxl.common.util.DiskKVUtil;
import cn.dxl.common.widget.ToastUtil;

public class ChameleonSoltAliasesActivity
        extends BaseActivity {

    private EditText edt1;
    private EditText edt2;
    private EditText edt3;
    private EditText edt4;
    private EditText edt5;
    private EditText edt6;
    private EditText edt7;
    private EditText edt8;

    private Button btnSave;

    private SwitchCompat swUseAliases;

    private File setFile = new File(Paths.SETTINGS_FILE);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_chameleon_slot_aliases);

        initViews();
        initActions();
    }

    private void initViews() {
        edt1 = findViewById(R.id.edt1);
        edt2 = findViewById(R.id.edt2);
        edt3 = findViewById(R.id.edt3);
        edt4 = findViewById(R.id.edt4);
        edt5 = findViewById(R.id.edt5);
        edt6 = findViewById(R.id.edt6);
        edt7 = findViewById(R.id.edt7);
        edt8 = findViewById(R.id.edt8);

        btnSave = findViewById(R.id.btnSaveSlotAliases);

        swUseAliases = findViewById(R.id.swUseAliases);

        updateViews();
    }

    private void initActions() {
        swUseAliases.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                enabled(isChecked);
                updateViews();
                try {
                    //写入到设置里!
                    DiskKVUtil.update2Disk(Properties.k_chameleon_aliases_status, String.valueOf(isChecked), setFile);
                    //更新全局参数!
                    Properties.v_chameleon_aliases_status = isChecked;
                } catch (IOException e) {
                    e.printStackTrace();
                    ToastUtil.show(context, getString(R.string.failed), false);
                }
            }
        });
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //保存相关的信息!
                String s1 = edt1.getText().toString();
                String s2 = edt2.getText().toString();
                String s3 = edt3.getText().toString();
                String s4 = edt4.getText().toString();
                String s5 = edt5.getText().toString();
                String s6 = edt6.getText().toString();
                String s7 = edt7.getText().toString();
                String s8 = edt8.getText().toString();

                try {
                    String[] aliases = new String[]{s1, s2, s3, s4, s5, s6, s7, s8};
                    DiskKVUtil.update2Disk(Properties.k_chameleon_aliases, aliases, setFile);
                    //更新到内存中!
                    Properties.v_chameleon_aliases = aliases;
                    ToastUtil.show(context, getString(R.string.successful), false);
                } catch (IOException e) {
                    e.printStackTrace();
                    ToastUtil.show(context, getString(R.string.failed), false);
                }
            }
        });
    }

    private void enabled(boolean status) {
        //更新缓存的变量!
        Properties.v_chameleon_aliases_status = status;

        btnSave.setEnabled(status);

        edt1.setEnabled(status);
        edt2.setEnabled(status);
        edt3.setEnabled(status);
        edt4.setEnabled(status);
        edt5.setEnabled(status);
        edt6.setEnabled(status);
        edt7.setEnabled(status);
        edt8.setEnabled(status);
    }

    private void updateViews() {
        if (Properties.v_chameleon_aliases_status) {
            //需要使用别名，启用相关的视图！
            swUseAliases.setChecked(true);
            enabled(true);

            //设置相关的视图参数!
            int index = 0;
            edt1.setText(Properties.v_chameleon_aliases[index++]);
            edt2.setText(Properties.v_chameleon_aliases[index++]);
            edt3.setText(Properties.v_chameleon_aliases[index++]);
            edt4.setText(Properties.v_chameleon_aliases[index++]);
            edt5.setText(Properties.v_chameleon_aliases[index++]);
            edt6.setText(Properties.v_chameleon_aliases[index++]);
            edt7.setText(Properties.v_chameleon_aliases[index++]);
            edt8.setText(Properties.v_chameleon_aliases[index++]);
        } else {
            //禁用相关的视图!
            swUseAliases.setChecked(false);
            enabled(false);
        }
    }
}
