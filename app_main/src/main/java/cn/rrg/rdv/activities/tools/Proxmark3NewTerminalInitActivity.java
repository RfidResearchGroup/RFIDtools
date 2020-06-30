package cn.rrg.rdv.activities.tools;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import androidx.annotation.Nullable;

import com.termux.app.TermuxActivity;

import cn.rrg.rdv.R;
import cn.rrg.rdv.activities.main.BaseActivity;
import cn.rrg.rdv.activities.main.PM3FlasherMainActivity;
import cn.rrg.rdv.util.Commons;
import cn.rrg.rdv.util.Proxmark3Installer;

public class Proxmark3NewTerminalInitActivity extends BaseActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_pm3_terminal_init);


        findViewById(R.id.btnFwFlash).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(activity, PM3FlasherMainActivity.class));
            }
        });

        findViewById(R.id.btnGoToTermux).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Commons.isPM3ClientDecompressed()) {
                    go();
                } else {
                    Proxmark3Installer.installIfNeed(activity, new Runnable() {
                        @Override
                        public void run() {
                            go();
                        }
                    });
                }
            }
        });

        CheckBox box = findViewById(R.id.ckBoxAutoGoTermux);
        box.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Commons.setAutoGoToTermux(isChecked);
            }
        });

        // Must have init pm3 client and check auto go!
        if (Commons.getAutoGoToTermux() && Commons.isPM3ClientDecompressed()) {
            go();
        }
    }

    public void go() {
        startActivity(new Intent(this, TermuxActivity.class));
        finish();
    }
}
