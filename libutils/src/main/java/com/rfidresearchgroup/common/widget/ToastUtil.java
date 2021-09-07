package com.rfidresearchgroup.common.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.Toast;

public class ToastUtil {

    private static Toast t;
    private static int code = 0x4141;
    private static Handler h = new Handler(Looper.getMainLooper()) {
        @SuppressLint("ShowToast")
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == code) {
                Bundle data = msg.getData();
                Context ctx = (Context) msg.obj;
                String msgStr = data.getString("msg");
                boolean longTime = data.getBoolean("isLongTime");
                if (t != null) {
                    t.cancel();
                    t = null;
                    try {
                        t = Toast.makeText(ctx, "", (longTime ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT));
                        t.setText(msgStr);
                    } catch (Exception ignored) {
                    }
                } else {
                    try {
                        t = Toast.makeText(ctx, "", (longTime ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT));
                        t.setText(msgStr);
                    } catch (Exception ignored) {
                    }
                }
                if (t != null)
                    t.show();
            }
            super.handleMessage(msg);
        }
    };

    /**
     * 显示一个Toast，可覆盖类型!
     *
     * @param context  上下文
     * @param msgStr   消息
     * @param longTime 是否设置 Toast.LENGTH_LONG
     *                 如果不，则设置
     *                 为默认的 Toast.LENGTH_SHORT
     */
    public static void show(Context context, String msgStr, boolean longTime) {
        Bundle data = new Bundle();
        data.putString("msg", msgStr);
        data.putBoolean("isLongTime", longTime);
        Message message = Message.obtain();
        message.obj = context;
        message.what = code;
        message.setData(data);
        h.sendMessage(message);
    }

    public static void show(Context context, String msgStr, boolean longTime, boolean uniqueView) {
        if (uniqueView) {
            Toast t = Toast.makeText(context, msgStr, Toast.LENGTH_SHORT);
            t.setText(msgStr);
            t.show();
        } else
            show(context, msgStr, longTime);
    }
}
