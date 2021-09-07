package com.rfidresearchgroup.common.util;

import android.content.Context;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.TextAppearanceSpan;

/*
 * 设置文本的样式!
 * */
public class TextStyleUtil {

    /*
     * 合并多个富文本对象到构造器里!
     * */
    public static SpannableStringBuilder merge(SpannableString... text) {
        SpannableStringBuilder ssb = new SpannableStringBuilder();
        for (SpannableString ss : text) {
            ssb.append(ss);
        }
        return ssb;
    }

    /*
     * 设置字体前景色！
     * */
    public static SpannableString getColorString(String str, int color) {
        SpannableString ret = new SpannableString(str);
        ret.setSpan(new ForegroundColorSpan(color), 0, str.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return ret;
    }

    /*
     * 设置背景样式!
     * */
    public static SpannableString getStyleString(Context context, String str, int style) {
        // TextAppearanceSpan 文本外貌（包括字体、大小、样式和颜色）
        SpannableString ret = new SpannableString(str);
        ret.setSpan(new TextAppearanceSpan(context, style), 0, str.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return ret;
    }


}
