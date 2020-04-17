package cn.rrg.rdv.activities.main;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;

import com.iobridges.com.LocalComBridgeAdapter;

import cn.rrg.rdv.R;
import cn.rrg.rdv.activities.px53x.HardnestedConsoleActivity;
import cn.rrg.rdv.activities.px53x.MfcukConsoleActivity;
import cn.rrg.rdv.activities.px53x.MfocConsoleActivity;
import cn.rrg.rdv.activities.px53x.NfcListConsoleActivity;

public class PN53XNfcMain extends BaseActivity {

    private Button btnNestedCrack = null;
    private Button btnPRNGCrack = null;
    private Button btnReader = null;
    private Button btnHardnested = null;
    private Button btnNfcList = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_main_pn53x);

        initViews();
        initActions();
    }

    private void initViews() {
        btnReader = findViewById(R.id.btnStartPN53XReaderAct);
        btnPRNGCrack = findViewById(R.id.btnStartMfcukAct);
        btnNestedCrack = findViewById(R.id.btnStartMfocAct);
        btnHardnested = findViewById(R.id.btnHardnested);
        btnNfcList = findViewById(R.id.btnNfcList);
    }

    private void initActions() {
        //设置读卡的按钮的接口实现
        btnReader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(context, PN53XReaderMain.class));
            }
        });

        //设置半加密破解的按钮的接口实现
        btnNestedCrack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(context, MfocConsoleActivity.class));
            }
        });

        //设置全加密破解的按钮的接口实现
        btnPRNGCrack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(context, MfcukConsoleActivity.class));
            }
        });

        //hard破解的按钮的接口实现
        btnHardnested.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(context)
                        .setTitle(R.string.warning)
                        .setMessage("您想使用的功能是实验室功能（暂未稳定），可能会发生许多或者已知或者未知的问题，您确定要进入使用吗？")
                        .setPositiveButton(R.string.go2, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                startActivity(new Intent(context, HardnestedConsoleActivity.class));
                            }
                        })
                        .setNegativeButton(R.string.cancel, null).show();
            }
        });

        //NfcList页面跳转!
        btnNfcList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(context, NfcListConsoleActivity.class));
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
