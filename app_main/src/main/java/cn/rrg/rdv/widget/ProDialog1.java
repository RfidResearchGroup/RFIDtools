package cn.rrg.rdv.widget;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import cn.rrg.rdv.R;

public class ProDialog1 {
    private Handler main = new Handler(Looper.getMainLooper());
    private AlertDialog mAlertDialog;
    private Context context;
    private TextView tvTip;

    public ProDialog1(Context context) {
        this.context = context;
    }

    public boolean isShowing() {
        return mAlertDialog != null && mAlertDialog.isShowing();
    }

    private void showPri(String tips) {
        mAlertDialog = new AlertDialog.Builder(context, R.style.CustomProgressDialog).create();
        View loadView = LayoutInflater.from(context).inflate(R.layout.item_dialog_style1, null);
        mAlertDialog.setView(loadView, 0, 0, 0, 0);
        mAlertDialog.setCanceledOnTouchOutside(false);
        mAlertDialog.setCancelable(false);
        tvTip = loadView.findViewById(R.id.txtShowTips);
        tvTip.setText(tips);
        mAlertDialog.show();
    }

    /**
     * 弹出耗时对话框
     */
    public void show(String tips) {
        main.post(new Runnable() {
            @Override
            public void run() {
                showPri(tips);
            }
        });
    }

    public void show() {
        show(context.getString(R.string.executing));
    }

    public void setTips(String tips) {
        main.post(new Runnable() {
            @Override
            public void run() {
                setTipsPri(tips);
            }
        });
    }

    private void setTipsPri(String tips) {
        if (!mAlertDialog.isShowing())
            mAlertDialog.show();
        tvTip.setText(tips);
    }

    /**
     * 隐藏耗时对话框
     */
    public void dismiss() {
        main.post(new Runnable() {
            @Override
            public void run() {
                if (mAlertDialog != null) {
                    try {
                        mAlertDialog.dismiss();
                    } catch (Exception ignored) {

                    }
                }
            }
        });
    }
}
