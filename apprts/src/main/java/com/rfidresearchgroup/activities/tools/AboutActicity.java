package com.rfidresearchgroup.activities.tools;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.Nullable;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.rfidresearchgroup.activities.main.BaseActivity;
import com.rfidresearchgroup.rfidtools.R;
import com.rfidresearchgroup.util.Commons;

public class AboutActicity extends BaseActivity {

    private TextView txtShowVersion = null;
    private Button btnGo2ProxgrindWebsite;
    private Button btnGo2RRGWebsite;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setContentView(R.layout.act_app_about);
        super.onCreate(savedInstanceState);

        initViews();
        initActions();
        showVersion();
    }

    private void initViews() {
        txtShowVersion = findViewById(R.id.txtShowAppVersion);

        btnGo2ProxgrindWebsite = findViewById(R.id.btnGo2ProxgrindWebsite);
        btnGo2RRGWebsite = findViewById(R.id.btnGo2RRGWebsite);
    }

    private void initActions() {
        btnGo2ProxgrindWebsite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Commons.openUrl(context, "http://proxgrind.com");
            }
        });

        btnGo2RRGWebsite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Commons.openUrl(context, "https://rfidresearchgroup.com");
            }
        });
    }

    private void showVersion() {
        PackageManager pm = getPackageManager();
        try {
            PackageInfo pi = pm.getPackageInfo(getPackageName(), 0);
            String vn = getString(R.string.version) + ": " + String.valueOf(pi.versionName);
            txtShowVersion.setText(vn);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            txtShowVersion.setText(R.string.error);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
