package cn.dxl.common.util;

import android.util.Log;

import cn.dxl.common.BuildConfig;

public class LogUtils {
    private static String TAG = "LogUtils";
    private static boolean log_open;

    static {
        // 是否需要打开DEBUG模式!
        log_open = BuildConfig.DEBUG;
    }

    public static void setEnable(boolean enable) {
        log_open = enable;
    }

    public static void setTAG(String tag) {
        TAG = tag;
    }

    public static String getTag() {
        return TAG;
    }

    public static void v(String msg) {
        if (log_open)
            Log.v(TAG, msg);
    }

    public static void v(String msg, Throwable throwable) {
        if (log_open)
            Log.v(TAG, msg, throwable);
    }

    public static void d(String msg) {
        if (log_open)
            Log.d(TAG, msg);
    }

    public static void d(String msg, Throwable throwable) {
        if (log_open)
            Log.d(TAG, msg, throwable);
    }

    public static void i(String msg) {
        if (log_open)
            Log.i(TAG, msg);
    }

    public static void i(String msg, Throwable throwable) {
        if (log_open)
            Log.i(TAG, msg, throwable);
    }

    public static void w(String msg) {
        if (log_open)
            Log.w(TAG, msg);
    }

    public static void w(String msg, Throwable throwable) {
        if (log_open)
            Log.w(TAG, msg, throwable);
    }

    public static void e(String msg) {
        if (log_open)
            Log.e(TAG, msg);
    }

    public static void e(String msg, Throwable throwable) {
        if (log_open)
            Log.e(TAG, msg, throwable);
    }
}
