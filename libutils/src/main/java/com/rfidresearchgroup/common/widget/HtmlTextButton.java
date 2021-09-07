package com.rfidresearchgroup.common.widget;

import android.content.Context;
import androidx.appcompat.widget.AppCompatButton;
import android.text.Html;
import android.util.AttributeSet;
import android.util.Log;

public class HtmlTextButton extends AppCompatButton {
    private static final String DEFAULT_NAMESPACE = "http://schemas.android.com/apk/res/android";

    public HtmlTextButton(Context context) {
        super(context);
    }

    public HtmlTextButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        //得到text属性，直接设置为html!
        int textId = attrs.getAttributeResourceValue(null, "text", -1);
        if (textId != -1) {
            this.setText(Html.fromHtml(getResources().getString(textId)));
            Log.d("test", "设置完成!");
        } else {
            String textIdStr = attrs.getAttributeValue(DEFAULT_NAMESPACE, "text");
            if (textIdStr != null) {
                try {
                    textIdStr = textIdStr.replace("@", "");
                    textId = Integer.valueOf(textIdStr);
                    this.setText(Html.fromHtml(getResources().getString(textId)));
                    Log.d("test", "设置完成!");
                } catch (Exception ignored) {
                }
            }
        }
        //设置默认不使用全大写!
        setAllCaps(false);
    }
}
