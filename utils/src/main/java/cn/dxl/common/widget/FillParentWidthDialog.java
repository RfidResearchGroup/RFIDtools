package cn.dxl.common.widget;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;

import cn.dxl.common.R;
import cn.dxl.common.util.DisplayUtil;

/*
 * 自定义的对话框
 * 允许充满屏幕完全的宽度!
 * */
public class FillParentWidthDialog extends AlertDialog {

    //默认半屏!
    private boolean canHalfScreen = false;

    /*
     * 重写构造函数，传入去除边框的Style
     * */
    public FillParentWidthDialog(Context context) {
        //设置样式!
        super(context, R.style.FillParentWidthDialog);
    }

    @Override
    public void show() {
        super.show();
        /*
         * 在显示之后再操作window
         * */
        setFillparent(this);
    }

    public void setCanHalfScreen(boolean can) {
        canHalfScreen = can;
    }

    private void setFillparent(Dialog dialog) {
        Window win = dialog.getWindow();
        if (win != null) {
            registerForContextMenu(win.getDecorView());
            win.getDecorView().setPadding(
                    0,
                    win.getDecorView().getPaddingTop(),
                    0,
                    win.getDecorView().getPaddingBottom()
            );
            win.setGravity(Gravity.BOTTOM);
            WindowManager.LayoutParams lp = win.getAttributes();
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            if (canHalfScreen) {
                //设置如果不到半屏就设备为半屏
                if (lp.height < DisplayUtil.getWindowHeight(getContext())) {
                    lp.height = DisplayUtil.getWindowHeight(getContext()) / 2;
                } else {
                    lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
                }
            } else {
                //不需要半屏!
                lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
            }
            win.setAttributes(lp);
        }
    }
}
