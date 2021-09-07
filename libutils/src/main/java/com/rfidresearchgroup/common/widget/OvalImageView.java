package com.rfidresearchgroup.common.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatImageView;

import com.rfidresearchgroup.common.util.AppUtil;
import com.rfidresearchgroup.common.util.DisplayUtil;

public class OvalImageView extends AppCompatImageView {
    Path path = new Path();
    RectF rectF = new RectF();
    /*圆角的半径，依次为左上角xy半径，右上角，右下角，左下角*/
    //此处可根据自己需要修改大小
    float px = DisplayUtil.dip2px(AppUtil.getInstance().getApp(), 16);
    private float[] rids = {px, px, px, px, px, px, px, px};


    public OvalImageView(Context context) {
        super(context);
    }

    public OvalImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public OvalImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * 画图
     *
     * @param canvas
     */
    protected void onDraw(Canvas canvas) {
        int w = this.getWidth();
        int h = this.getHeight();
        rectF.set(0, 0, w, h);
        /*向路径中添加圆角矩形。radii数组定义圆角矩形的四个圆角的x,y半径。radii长度必须为8*/
        path.addRoundRect(rectF, rids, Path.Direction.CW);
        canvas.clipPath(path);
        super.onDraw(canvas);
    }
}
