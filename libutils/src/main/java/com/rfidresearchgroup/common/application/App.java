package com.rfidresearchgroup.common.application;

import android.app.Application;
import android.content.Context;

import com.rfidresearchgroup.common.util.AppUtil;
import com.rfidresearchgroup.common.util.CrashUtil;

public class App extends Application {

    public interface ApplicationCallback {
        Context onAttachBaseContext(Context context);
    }

    public ApplicationCallback getCallback() {
        return callback;
    }

    public void setCallback(ApplicationCallback callback) {
        this.callback = callback;
    }

    private ApplicationCallback callback;

    @Override
    public void onCreate() {
        super.onCreate();
        //异常处理
        CrashUtil.register(this);
        //全局context，app环境缓存!
        AppUtil.register(this);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(callback != null ? callback.onAttachBaseContext(base) : base);
    }
}
