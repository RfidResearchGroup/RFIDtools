package com.rfidresearchgroup.common.services;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import androidx.annotation.Nullable;

public class RestartService extends Service {

    private static final String LOG_TAG = RestartService.class.getSimpleName();
    private Handler handler;
    private String PackageName;

    public RestartService() {
        handler = new Handler();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        /*
         * 关闭应用后多久重新启动
         */
        long stopDelayed = intent.getLongExtra("Delayed", 2000);
        PackageName = intent.getStringExtra("PackageName");
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent LaunchIntent = RestartService.this.getPackageManager().getLaunchIntentForPackage(PackageName);
                RestartService.this.startActivity(LaunchIntent);
                RestartService.this.stopSelf();
            }
        }, stopDelayed);
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
