package com.rfidresearchgroup.common.util;

import android.app.Activity;

import com.rfidresearchgroup.common.application.App;

public class AppUtil {
    private static AppUtil thiz = null;
    private static App app;

    private AppUtil() {
    }

    public static AppUtil getInstance() {
        if (thiz == null) {
            thiz = new AppUtil();
        }
        return thiz;
    }

    public static void register(App app) {
        AppUtil.app = app;
    }

    public App getApp() {
        return app;
    }

    public void finishAll() {
        for (Activity activity : CrashUtil.activities) {
            activity.finish();
        }
    }
}
