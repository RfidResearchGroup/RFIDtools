package cn.rrg.rdv.widget;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import cn.dxl.common.util.DisplayUtil;
import cn.rrg.rdv.R;

public class MaterialAlertDialog extends Dialog {

    private View layout;
    private Button btn1;
    private Button btn2;
    private TextView txtMsg;
    private TextView txtTitle;
    private View verticalDecorateView;
    private View horizontalDecorateView;
    private OnWidgetStyle onWidgetStyle;

    private MaterialAlertDialog(@NonNull Context context) {
        super(context, R.style.CustomerDialogStyle);
        initViews();
    }

    private MaterialAlertDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
        initViews();
    }

    private MaterialAlertDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        initViews();
    }

    private void initViews() {
        layout = View.inflate(getContext(), R.layout.dialog_materila_alert_dialog, null);

        btn2 = layout.findViewById(R.id.btn2);
        btn1 = layout.findViewById(R.id.btn1);
        txtMsg = layout.findViewById(R.id.txtView_showMsg);
        txtTitle = layout.findViewById(R.id.txtView_Title);
        horizontalDecorateView = layout.findViewById(R.id.v1);
        verticalDecorateView = layout.findViewById(R.id.v2);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layout);

        if (onWidgetStyle != null)
            onWidgetStyle.onStyle(txtTitle, txtMsg, btn1, btn2);

        Window window = getWindow();
        if (window != null) {
            WindowManager.LayoutParams params = window.getAttributes();
            params.width = (int) (DisplayUtil.getWindowWidth(getContext()) * 0.8);
        }
    }

    public Button getBtn1() {
        return btn1;
    }

    public void setBtn1(Button btn1) {
        this.btn1 = btn1;
    }

    public Button getBtn2() {
        return btn2;
    }

    public void setBtn2(Button btn2) {
        this.btn2 = btn2;
    }

    public TextView getTxtMsg() {
        return txtMsg;
    }

    public void setTxtMsg(TextView txtMsg) {
        this.txtMsg = txtMsg;
    }

    public TextView getTxtTitle() {
        return txtTitle;
    }

    public void setTxtTitle(TextView txtTitle) {
        this.txtTitle = txtTitle;
    }

    public OnWidgetStyle getOnWidgetStyle() {
        return onWidgetStyle;
    }

    public void setOnWidgetStyle(OnWidgetStyle onWidgetStyle) {
        this.onWidgetStyle = onWidgetStyle;
    }

    public interface OnWidgetStyle {
        void onStyle(TextView title, TextView msg, Button btn1, Button btn2);
    }

    public static class Builder {
        private String msg;

        private View.OnClickListener onClickListener1;
        private View.OnClickListener onClickListener2;
        private OnWidgetStyle onWidgetStyle;

        private String btn1Text;
        private String btn2Text;
        private String titleText;

        private boolean cancelable = true;

        private Context context;

        public Builder(Context context) {
            this.context = context;
        }

        public Builder setTitle(String title) {
            titleText = title;
            return this;
        }

        public Builder setTitle(int resID) {
            setTitle(context.getString(resID));
            return this;
        }

        public Builder setMessage(String msg) {
            this.msg = msg;
            return this;
        }

        public Builder setMessage(int resID) {
            setMessage(context.getString(resID));
            return this;
        }

        public Builder setButton1(String text, View.OnClickListener listener) {
            onClickListener1 = listener;
            btn1Text = text;
            return this;
        }

        public Builder setButton2(String text, View.OnClickListener listener) {
            onClickListener2 = listener;
            btn2Text = text;
            return this;
        }

        public Builder setButton1(int resID, View.OnClickListener listener) {
            setButton1(context.getString(resID), listener);
            return this;
        }

        public Builder setButton2(int resID, View.OnClickListener listener) {
            setButton2(context.getString(resID), listener);
            return this;
        }

        public Builder setStyle(OnWidgetStyle style) {
            onWidgetStyle = style;
            return this;
        }

        public Builder setCancelable(boolean cancelable) {
            this.cancelable = cancelable;
            return this;
        }

        public MaterialAlertDialog create() {
            MaterialAlertDialog dialog = new MaterialAlertDialog(context);
            dialog.onWidgetStyle = onWidgetStyle;
            if (btn1Text != null) {
                dialog.btn1.setText(btn1Text);
                dialog.btn1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (onClickListener1 != null)
                            onClickListener1.onClick(v);
                        dialog.dismiss();
                    }
                });
            } else {
                dialog.btn1.setVisibility(View.GONE);
                dialog.verticalDecorateView.setVisibility(View.GONE);
            }
            if (btn2Text != null) {
                dialog.btn2.setText(btn2Text);
                dialog.btn2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (onClickListener2 != null)
                            onClickListener2.onClick(v);
                        dialog.dismiss();
                    }
                });
            } else {
                dialog.btn2.setVisibility(View.GONE);
                dialog.verticalDecorateView.setVisibility(View.GONE);
            }
            if (btn1Text == null && btn2Text == null) {
                dialog.horizontalDecorateView.setVisibility(View.GONE);
            }
            if (msg == null) {
                throw new RuntimeException("The msg is null.");
            }
            dialog.txtMsg.setText(msg);
            if (titleText != null) {
                dialog.txtTitle.setText(titleText);
            } else {
                dialog.txtTitle.setVisibility(View.GONE);
            }
            dialog.setCancelable(cancelable);
            return dialog;
        }

        public MaterialAlertDialog show() {
            MaterialAlertDialog dialog = create();
            dialog.show();
            return dialog;
        }
    }
}
