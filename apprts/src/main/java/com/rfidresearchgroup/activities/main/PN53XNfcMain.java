package com.rfidresearchgroup.activities.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;

import com.proxgrind.com.LocalComBridgeAdapter;
import com.rfidresearchgroup.activities.px53x.PN53XConsoleActivity;
import com.rfidresearchgroup.rfidtools.R;

public class PN53XNfcMain extends BaseActivity {

    private Button btnReader = null;
    private Button btnConsole = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_main_pn53x);

        initViews();
        initActions();
    }

    private void initViews() {
        btnReader = findViewById(R.id.btnStartPN53XReaderAct);
        btnConsole = findViewById(R.id.btnStartPN53XConsole);
    }

    private void initActions() {
        //设置读卡的按钮的接口实现
        btnReader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(context, PN53XReaderMain.class));
            }
        });

        btnConsole.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(context, PN53XConsoleActivity.class));
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalComBridgeAdapter.getInstance()
                .stopClient();
    }
}
