package com.rfidresearchgroup.common.widget;

import android.app.Dialog;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.rfidresearchgroup.common.R;
import com.rfidresearchgroup.common.util.ViewUtil;

/*
 * 在此封装一个专属于按钮的名称与
 * */
public class DoubleInputDialog extends Dialog {

    private EditText edtInput1;
    private EditText edtInput2;
    private TextView title;

    private TextView hint1;
    private TextView hint2;

    private OnSaveListener saveListener;
    private OnCancelListener cancelListener;

    public DoubleInputDialog(@NonNull Context context) {
        super(context);
        //设置视图!
        View view = ViewUtil.inflate(getContext(), R.layout.double_input_dialog);
        setContentView(view);

        //设置一些窗口的参数!
        Window window = getWindow();
        if (window != null) {
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
            window.setAttributes(lp);
            //设置软输入法一直伴随窗口可见!
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        }
        //获得实例!
        title = view.findViewById(R.id.txtTitle);
        hint1 = view.findViewById(R.id.txtInput1Hint);
        hint2 = view.findViewById(R.id.txtInput2Hint);
        Button btnSave = view.findViewById(R.id.btnSave);
        Button btnCancel = view.findViewById(R.id.btnCancel);
        edtInput1 = view.findViewById(R.id.edt1);
        edtInput2 = view.findViewById(R.id.edt2);
        //设置事件!
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String[] strs = new String[]{
                        edtInput1.getText().toString(),
                        edtInput2.getText().toString()
                };
                if (saveListener != null) {
                    saveListener.onSave(strs);
                }
                cancel();
            }
        });
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancel();
                if (cancelListener != null) {
                    cancelListener.onCancel();
                }
            }
        });
    }

    public OnSaveListener getSaveListener() {
        return saveListener;
    }

    public OnCancelListener getCancelListener() {
        return cancelListener;
    }

    public DoubleInputDialog setCancelListener(OnCancelListener cancelListener) {
        this.cancelListener = cancelListener;
        return this;
    }

    public DoubleInputDialog setSaveListener(OnSaveListener saveListener) {
        this.saveListener = saveListener;
        return this;
    }

    public interface OnSaveListener {
        void onSave(String[] content);
    }

    public interface OnCancelListener {
        void onCancel();
    }

    @Override
    public void setTitle(int titleId) {
        title.setText(getContext().getString(titleId));
    }

    @Override
    public void setTitle(@Nullable CharSequence title) {
        this.title.setText(title);
    }

    public EditText getEdtInput1() {
        return edtInput1;
    }

    public EditText getEdtInput2() {
        return edtInput2;
    }

    public TextView getTitle() {
        return title;
    }

    public TextView getHint1() {
        return hint1;
    }

    public TextView getHint2() {
        return hint2;
    }
}
