package cn.dxl.common.util;

import android.app.Activity;
import android.app.Application;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;

public class ContextUtil {

    private static Application context = AppUtil.getInstance().getApp();

    public ContextUtil() {
    }

    //根据colorId得到颜色
    public static int getColor(int colorId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return context.getColor(colorId);
        }
        return context.getResources().getColor(colorId);
    }

    //根据drawableId得到drawable对象
    public static Drawable getDrawable(int drawableId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return context.getDrawable(drawableId);
        }
        return context.getResources().getDrawable(drawableId);
    }

    //复制文本到剪贴板
    public static void copyStr2Clipborad(String label, String content) {
        ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboardManager == null) return;
        //创建ClipData对象
        ClipData clipData = ClipData.newPlainText(label, content);
        //添加ClipData对象到剪切板中
        clipboardManager.setPrimaryClip(clipData);
    }
}
