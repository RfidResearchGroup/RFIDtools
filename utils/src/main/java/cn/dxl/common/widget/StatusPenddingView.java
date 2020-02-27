package cn.dxl.common.widget;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

public class StatusPenddingView extends LinearLayout {
    public StatusPenddingView(Context context) {
        super(context);
        initHeight();
    }

    public StatusPenddingView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initHeight();
    }

    public StatusPenddingView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initHeight();
    }

    private void initHeight() {
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (resourceId > 0) {
                int height = getResources().getDimensionPixelSize(resourceId);
                setPadding(getPaddingLeft(), height, getPaddingRight(), getPaddingBottom());
            }
        } else {
            //低版本 直接设置0
            setPadding(getPaddingLeft(), 0, getPaddingRight(), getPaddingBottom());
        }
    }
}
