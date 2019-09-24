package cn.dxl.common.widget;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Message;
import android.text.Editable;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import cn.dxl.common.R;
import cn.dxl.common.util.ViewUtil;

public class SingleInputDialog extends AlertDialog {

    public interface OnFininshCallback {
        void onFinish(CharSequence inputs);
    }

    private OnFininshCallback callback;

    private DialogInterface.OnClickListener onPositiveClick;

    private EditText editText;
    private TextView txtTips;

    private View mView;

    public SingleInputDialog(Context context) {
        super(context);
        mView = ViewUtil.inflate(getContext(), R.layout.single_input_dialog);
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

    public SingleInputDialog setHint(CharSequence hint) {
        editText.setHint(hint);
        return this;
    }

    public SingleInputDialog setHint(int hint) {
        editText.setHint(hint);
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

    public SingleInputDialog setFinishCallback(OnFininshCallback callback) {
        this.callback = callback;
        return this;
    }

    @Override
    public void show() {
        setView(mView);
        super.show();
    }

    private class ClickImpl implements DialogInterface.OnClickListener {
        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
            // 先执行官方外置实例动作的传入!
            onPositiveClick.onClick(dialogInterface, i);
            // 然后再执行输入回调!
            callback.onFinish(getInput());
        }
    }
}
