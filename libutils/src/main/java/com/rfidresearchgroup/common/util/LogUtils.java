package com.rfidresearchgroup.common.util;

import android.util.Log;

import com.rfidresearchgroup.common.BuildConfig;

public class LogUtils {
    private static boolean log_open;

    static {
        // 是否需要打开DEBUG模式!
        log_open = BuildConfig.DEBUG;
    }

    public static void setEnable(boolean enable) {
        log_open = enable;
    }

    public static String makeStackTrace() {
        StackTraceElement[] stacks = Thread.currentThread().getStackTrace();
        return String.format("Clz:%s,Fun:%s,File:%s,Line:%s",
                stacks[2].getClassName(),
                stacks[2].getMethodName(),
                stacks[2].getFileName(),
                stacks[2].getLineNumber()
        );
    }

    public static void v(String msg) {
        if (log_open)
            Log.v(makeStackTrace(), msg);
    }

    public static void v(String msg, Throwable throwable) {
        if (log_open)
            Log.v(makeStackTrace(), msg, throwable);
    }

    public static void d(String msg) {
        if (log_open)
            Log.d(makeStackTrace(), msg);
    }

    public static void d(String msg, Throwable throwable) {
        if (log_open)
            Log.d(makeStackTrace(), msg, throwable);
    }

    public static void i(String msg) {
        if (log_open)
            Log.i(makeStackTrace(), msg);
    }

    public static void i(String msg, Throwable throwable) {
        if (log_open)
            Log.i(makeStackTrace(), msg, throwable);
    }

    public static void w(String msg) {
        if (log_open)
            Log.w(makeStackTrace(), msg);
    }

    public static void w(String msg, Throwable throwable) {
        if (log_open)
            Log.w(makeStackTrace(), msg, throwable);
    }

    public static void e(String msg) {
        if (log_open)
            Log.e(makeStackTrace(), msg);
    }

    public static void e(String msg, Throwable throwable) {
        if (log_open)
            Log.e(makeStackTrace(), msg, throwable);
    }
}
