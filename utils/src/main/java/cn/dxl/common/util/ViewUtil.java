package cn.dxl.common.util;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

public class ViewUtil {
    //加载layout文件并且返回view引用
    public static View inflate(Context context, int layID) {
        View v = LayoutInflater.from(context).inflate(layID, null);
        v.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        return v;
    }

    //给编辑器请求焦点和虚拟键盘
    public static void requestFocusAndShowInputMethod(final EditText edt) {
        if (edt == null) return;
        edt.post(new Runnable() {
            @Override
            public void run() {
                edt.setFocusable(true);
                edt.setFocusableInTouchMode(true);
                edt.requestFocus();
                InputMethodManager imm = (InputMethodManager) edt.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null)
                    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_NOT_ALWAYS);
            }
        });
    }
}
