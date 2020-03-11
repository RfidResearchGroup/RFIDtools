package cn.dxl.common.widget;

import android.app.AlertDialog;
import android.content.Context;
import android.text.Editable;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import cn.dxl.common.R;

public class SingleInputDialog extends AlertDialog {

    private EditText editText;
    private TextView txtTips;
    private View mView;

    public SingleInputDialog(Context context) {
        super(context);
        mView = View.inflate(getContext(), R.layout.item_dialog_single_input, null);
        editText = mView.findViewById(R.id.edtInput);
        txtTips = mView.findViewById(R.id.txtShowTips);
    }

    public SingleInputDialog setTips(CharSequence tips) {
        txtTips.setText(tips);
        return this;
    }

    public SingleInputDialog setTips(int resId) {
        txtTips.setText(resId);
        return this;
    }

    public SingleInputDialog setText(CharSequence text) {
        editText.setText(text);
        return this;
    }

    public SingleInputDialog setText(int resId) {
        editText.setText(resId);
        return this;
    }

    public Editable getInput() {
        return editText.getText();
    }

    public EditText getEditText() {
        return editText;
    }

    public TextView getTextView() {
        return txtTips;
    }

    @Override
    public void show() {
        setView(mView);
        super.show();
    }
}
