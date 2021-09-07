package com.rfidresearchgroup.common.widget;

import android.content.Context;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.rfidresearchgroup.common.util.DisplayUtil;

/**
 * 提供进行数值加减的控件，带有 + - 两个按钮!
 *
 * @author DXL
 * @version 2.0
 * @date 2019/05/17
 */
public class AdditionSubtractionView
        extends LinearLayout {

    private String TAG = getClass().getSimpleName();

    //全局标志，代表增值按钮
    public static final int INCREMENT = 0;
    //代表减值按钮
    public static final int DECREMENT = 1;
    //代表输入框
    public static final int INPUT = 3;

    //初始值!
    private int value;
    //上限值
    private int maxValue = Integer.MAX_VALUE;
    //下限值
    private int minValue = 0;
    //自增按钮!
    private Button btnIncrement;
    private Button btnDecrement;
    //数值输入!
    private EditText edtInputValue;
    //回调
    private OnValueUpdateListener mOnValueUpdateListener;
    private OnIncrementListener mOnIncrementListener;
    private OnDecrementListener mOnDecrementListener;

    public AdditionSubtractionView(Context context) {
        super(context);
        initViews();
    }

    public AdditionSubtractionView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initViews();
    }

    /**
     * 初始化构造方法，支持传入初始化值，默认为 0
     *
     * @param initValue 初始化设置的值!
     */
    public AdditionSubtractionView(Context context, int initValue) {
        super(context);
        value = initValue;
        initViews();
    }

    private void initViews() {
        Log.d(TAG, "initViews: 开始初始化视图!");

        //各大控件的空间分布参数，长宽之类的!
        LinearLayout.LayoutParams leftBtnParams =
                new LinearLayout.LayoutParams(DisplayUtil.dip2px(getContext(), 38), DisplayUtil.dip2px(getContext(), 38));
        LinearLayout.LayoutParams centerBtnParams =
                new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        LinearLayout.LayoutParams rightBtnParams =
                new LinearLayout.LayoutParams(DisplayUtil.dip2px(getContext(), 38), DisplayUtil.dip2px(getContext(), 38));

        //进行基本操作控件的建立!
        btnIncrement = new Button(getContext());
        btnIncrement.setLayoutParams(leftBtnParams);
        btnIncrement.setText("+");

        btnDecrement = new Button(getContext());
        btnDecrement.setLayoutParams(rightBtnParams);
        btnDecrement.setText("-");
        btnDecrement.setEnabled(false);

        edtInputValue = new EditText(getContext());
        edtInputValue.setLayoutParams(centerBtnParams);
        edtInputValue.setBackground(null);
        edtInputValue.setMinEms(1);
        edtInputValue.setInputType(InputType.TYPE_CLASS_NUMBER);
        edtInputValue.setText(String.valueOf(value));

        //设置为水平方向分布控件!
        setOrientation(HORIZONTAL);

        //进行视图添加，构造基本操作界面!
        addView(btnDecrement);
        addView(edtInputValue);
        addView(btnIncrement);

        //初始化内置控件事件!
        initActions();
    }

    private void initActions() {
        btnIncrement.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                increment();
                updateInput();
            }
        });
        btnDecrement.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                decrement();
                updateInput();
            }
        });
        edtInputValue.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() <= 0) {
                    value = 0;
                    //禁用减按钮!
                    btnDecrement.setEnabled(false);
                } else if (s.toString().equals("0") && minValue == 0) {
                    value = 0;
                    //禁用减按钮!
                    btnDecrement.setEnabled(false);
                } else {
                    //TODO 注意检查int上限值!
                    try {
                        //启用减按钮!
                        btnDecrement.setEnabled(true);
                        //更新值!
                        String valueStr = edtInputValue.getText().toString();
                        value = Integer.valueOf(valueStr);
                    } catch (NumberFormatException nfe) {
                        nfe.printStackTrace();
                        value = 0;
                        updateInput();
                    }
                }
                //判断回调
                if (mOnValueUpdateListener != null) {
                    mOnValueUpdateListener.onUpdate(AdditionSubtractionView.this, value);
                }
            }
        });
    }

    private void updateInput() {
        String valueStr = String.valueOf(value);
        edtInputValue.setText(valueStr);
    }

    /**
     * 获得当前输入的值!
     *
     * @return 输入框中的值，如果输入框是空的，则返回 0
     */
    public int getValue() {
        int ret = 0;
        try {
            ret = Integer.parseInt(edtInputValue.getText().toString());
        } catch (NumberFormatException nfe) {
            //empty
        }
        return ret;
    }

    /**
     * 设置当前输入的值!
     *
     * @param value 一个int，将被设备到输入框与更新到全局变量!
     */
    public void setValue(int value) {
        if (value < Integer.MAX_VALUE) {
            this.value = value;
            updateInput();
        }
    }

    public void setMaxValue(int maxValue) {
        this.maxValue = maxValue;
    }

    public void setMinValue(int minValue) {
        this.minValue = minValue;
        if (minValue > 0) setValue(minValue);
    }

    //接口，在有数值更新时进行回调!
    public interface OnValueUpdateListener {
        void onUpdate(AdditionSubtractionView view, int value);
    }

    private interface OnDIActions {
        void onAction();
    }

    //接口，在进行增值的时候进行回调!
    public interface OnIncrementListener extends OnDIActions {
    }

    //接口，在进行减值的时候进行回调!
    public interface OnDecrementListener extends OnDIActions {
    }

    /**
     * 设置回调
     *
     * @param listener 在数值更新时会被回调!
     */
    public void setOnValueUpdateListener(OnValueUpdateListener listener) {
        mOnValueUpdateListener = listener;
    }

    public void setOnIncrementListener(OnIncrementListener listener) {
        this.mOnIncrementListener = listener;
    }

    public void setOnDecrementListener(OnDecrementListener listener) {
        this.mOnDecrementListener = listener;
    }

    /**
     * 自增一次
     */
    public void increment() {
        if (value + 1 < maxValue) {
            ++value;
            updateInput();
            //回调
            if (mOnIncrementListener != null) mOnIncrementListener.onAction();
        }
    }

    /**
     * 自减一次!
     */
    public void decrement() {
        if (value - 1 >= minValue) {
            --value;
            updateInput();
            //回调
            if (mOnDecrementListener != null) mOnDecrementListener.onAction();
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        if (enabled == isEnabled()) return;
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child != null) {
                child.setEnabled(enabled);
            }
        }
        super.setEnabled(enabled);
    }

    /**
     * 启用或者禁用某一个控件
     *
     * @param enabled 目标状态
     * @param type    操作的控件
     * @throws RuntimeException 如果类型传入错误，则抛出异常!
     */
    public void setEnabled(boolean enabled, int type) {
        if (enabled == isEnabled()) return;
        switch (type) {
            case INCREMENT:
                btnIncrement.setEnabled(enabled);
                break;
            case DECREMENT:
                btnDecrement.setEnabled(enabled);
                break;
            case INPUT:
                edtInputValue.setEnabled(enabled);
                break;
            default:
                throw new RuntimeException("The type is error!");
        }
    }
}
