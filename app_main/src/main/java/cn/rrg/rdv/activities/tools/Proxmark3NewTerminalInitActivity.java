package cn.rrg.rdv.activities.tools;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;

import com.termux.app.TermuxActivity;

import cn.rrg.rdv.activities.main.BaseActivity;
import cn.rrg.rdv.util.Commons;
import cn.rrg.rdv.util.Proxmark3Installer;

public class Proxmark3NewTerminalInitActivity extends BaseActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Commons.isPM3ClientDecompressed()) {
            go();
        } else {
            Proxmark3Installer.installIfNeed(this, new Runnable() {
                @Override
                public void run() {
                    go();
                }
            });
        }
    }

    public void go() {
        startActivity(new Intent(this, TermuxActivity.class));
        finish();
    }
}
