package cn.rrg.rdv.activities.tools;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.appcompat.widget.AppCompatSeekBar;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

import cn.rrg.freo.IORedirector;
import cn.rrg.console.define.ICommandTools;
import cn.rrg.console.define.ICommandType;
import cn.rrg.rdv.activities.main.BaseActivity;
import cn.dxl.common.widget.FillParentWidthDialog;
import cn.dxl.common.widget.ToastUtil;
import cn.dxl.common.util.PrintUtil;
import cn.rrg.rdv.R;
import cn.dxl.common.util.DynamicLineParseUtil;
import cn.rrg.rdv.implement.TextWatcherImpl;

/*
 * TODO 此类留存，用于封装基础控制台!
 * 子类待具体实现需要分离的逻辑!
 * */
public abstract class BaseConsoleActivity extends BaseActivity {
    //滚动视图的实例
    protected ScrollView svOutContain = null;
    protected ScrollView svErrContain = null;
    protected ScrollView svLogContain = null;
    //输入命令行的Edt
    protected EditText edtInputCmd = null;
    //编辑器，标准输出控制台!
    protected TextView txtOutConsole = null;
    //编辑器，标准异常控制台
    protected TextView txtErrConsole = null;
    //编辑器，标准的日志输出!
    protected TextView txtLogConsole = null;
    //开始执行按钮
    protected Button btnStart = null;
    //停止执行的按钮
    protected Button btnStop = null;
    //清除列表中的消息
    protected Button btnClearMsg = null;
    //显示可视化得命令列表
    protected Button btnGui = null;
    //控制条，可控制显示区域的比例，以标准输出为计算点!
    protected AppCompatSeekBar skBarConsoleViewScale = null;
    //单选开关，是否需要开启输出错误日志!
    protected AppCompatCheckBox ckBoxOpenOutputError = null;
    //单选开关，是否需要开启输出开发者日志!
    protected AppCompatCheckBox ckBoxOpenOutputLog = null;
    //显示错误日志的区域!
    protected LinearLayout linearLayout_output_err_views = null;
    //显示开发者日志的区域!
    protected LinearLayout linearLayout_output_log_views = null;
    //显示正常日志的区域!
    protected LinearLayout linearLayout_output_out_views = null;
    //上下分割区域下，下半部分控制台的组容器
    protected LinearLayout linearLayout_err_log_group_views = null;
    //底部的分割线
    protected View view_bottom_log_err_division_line = null;

    //测试器对象
    protected ICommandTools mCMD = null;
    protected ICommandType mType = null;
    //默认的命令
    protected String mDefaultCMD = "?";

    //标记是否是组件间互联模式!
    protected boolean mIsRequsetMode = false;

    //上下文!
    protected Context mContext = this;
    //pop对象
    protected FillParentWidthDialog mDialogGUI = null;
    //对话框!
    protected AlertDialog mDialog = null;

    //主线程消息句柄
    protected Handler mHandler = new Handler(Looper.getMainLooper());

    private PrintUtil printOutTools = null;
    private PrintUtil printErrTools = null;

    //动态行解析工具!
    private DynamicLineParseUtil mOutDLPU = null;
    private DynamicLineParseUtil mErrDLPU = null;

    private PrintUtil.OnPrintLisenter printOutLisenter = out -> mHandler.post(() -> {
        appendReturn(txtOutConsole, out);
        //标准输出窗口拉到底部
        svOutContain.fullScroll(ScrollView.FOCUS_DOWN);
    });

    private PrintUtil.OnPrintLisenter printErrLisenter = out -> mHandler.post(() -> {
        appendReturn(txtErrConsole, out);
        //标准异常窗口拉到底部
        svErrContain.fullScroll(ScrollView.FOCUS_DOWN);
    });

    private DynamicLineParseUtil.OnNewLineLisenter dlpuOutLisenter = this::onNewOutLine;

    private DynamicLineParseUtil.OnNewLineLisenter dlpuErrLisenter = this::onNewErrLine;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_console_layout);

        //初始化视图!
        initViews();
        //初始化点击事件
        initOnClick();

        //初始化需要的资源!
        File lisenterOutTarget = initOutTarget();
        File lisenterErrTarget = initErrTarget();
        mCMD = initCMD();
        mType = initType();

        if (lisenterOutTarget == null || mCMD == null || lisenterErrTarget == null) {
            throw new RuntimeException("BaseConsoleActivity has some resources did not init!");
        }

        txtOutConsole.addTextChangedListener(new TextWatcherImpl() {
            //存放上次的尾部索引
            int last = 0;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //在文本改变之前的回调!
                last = s.length();
            }

            @Override
            public void afterTextChanged(Editable s) {
                //在文本改变后的回调，这里我们需要截取他的字符串!
                if (s.length() == 0) return;
                if (last > s.length()) return;
                String str = s.toString().substring(last);
                for (Character c : str.toCharArray()) {
                    mOutDLPU.appendText(c);
                }
            }
        });

        txtErrConsole.addTextChangedListener(new TextWatcherImpl() {
            //存放上次的尾部索引
            int last = 0;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //在文本改变之前的回调!
                last = s.length();
            }

            @Override
            public void afterTextChanged(Editable s) {
                //在文本改变后的回调，这里我们需要截取他的字符串!
                if (s.length() == 0) return;
                String str = s.toString().substring(last);
                for (Character c : str.toCharArray()) {
                    mErrDLPU.appendText(c);
                }
            }
        });

        txtLogConsole.addTextChangedListener(new TextWatcherImpl() {
            @Override
            public void afterTextChanged(Editable s) {
                svLogContain.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });

        //接收一个传递过来的命令!
        Intent intent = getIntent();
        //判断是否是组件间传输模式!
        boolean isCTM = intent.getBooleanExtra("ctm", false);
        if (isCTM) {
            //进行解析，判断是否需要自定义命令!
            String cmdStr = intent.getStringExtra("cmd");
            //判断可用性!
            if (cmdStr != null) {
                mDefaultCMD = cmdStr;
                mIsRequsetMode = true;
                Toast.makeText(this, "组件间互联请求模式!", Toast.LENGTH_SHORT).show();
                edtInputCmd.setText(mDefaultCMD);
                //直接开始!
                btnStart.performClick();
            } else {
                mIsRequsetMode = true;
                //不需要直接开始!
                //btnStart.performClick();
            }
        } else {
            mIsRequsetMode = false;
        }

        //文件监听器对象!
        printOutTools = new PrintUtil(lisenterOutTarget);
        printErrTools = new PrintUtil(lisenterErrTarget);
        //设置监听器!
        printOutTools.setPrintLisenter(printOutLisenter);
        printErrTools.setPrintLisenter(printErrLisenter);
        //建立解析器!
        mOutDLPU = new DynamicLineParseUtil(dlpuOutLisenter);
        mErrDLPU = new DynamicLineParseUtil(dlpuErrLisenter);

        mDialog = new AlertDialog.Builder(this).create();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //启动一些任务!
        mOutDLPU.start();
        mErrDLPU.start();
        //再尝试启动!
        printOutTools.start();
        printErrTools.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //暂停一些任务
        mOutDLPU.pause();
        mErrDLPU.pause();
        //尝试暂停!
        printOutTools.pause();
        printErrTools.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //停止一些任务!
        stopTest();
        mOutDLPU.cancel();
        mErrDLPU.cancel();
        //尝试停止监听线程!
        printOutTools.stop();
        printErrTools.stop();
    }

    private void initViews() {
        svOutContain = findViewById(R.id.svOutContain);
        svErrContain = findViewById(R.id.svErrContain);
        svLogContain = findViewById(R.id.svLogContain);

        txtOutConsole = findViewById(R.id.txtOutConsoleMsg);
        txtErrConsole = findViewById(R.id.txtErrConsoleMsg);
        txtLogConsole = findViewById(R.id.txtLogConsoleMsg);

        edtInputCmd = findViewById(R.id.edtInputCommand);

        btnStart = findViewById(R.id.btnStart);
        btnStop = findViewById(R.id.btnStop);
        btnClearMsg = findViewById(R.id.btnClearMsg);
        btnGui = findViewById(R.id.btnGui);

        mDialogGUI = new FillParentWidthDialog(this);
        mDialogGUI.setCanHalfScreen(true);

        //三大分割容器中的控件，控制视图一些参数!
        skBarConsoleViewScale = findViewById(R.id.skBarConsoleViewScale);
        ckBoxOpenOutputError = findViewById(R.id.ckBoxOpenOutputError);
        ckBoxOpenOutputLog = findViewById(R.id.ckBoxOpenOutputLogMsg);

        //容器，是三大输出块的分割容器!
        linearLayout_output_err_views = findViewById(R.id.linearLayout_output_err_views);
        linearLayout_output_log_views = findViewById(R.id.linearLayout_output_log_views);
        linearLayout_output_out_views = findViewById(R.id.linearLayout_output_out_views);
        linearLayout_err_log_group_views = findViewById(R.id.linearLayout_err_log_group_views);

        //分割线
        view_bottom_log_err_division_line = findViewById(R.id.view_console_err_log_division);
    }

    private void initOnClick() {
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //在未执行完命令之前，不允许多次执行!
                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        enableStartBtn(false);
                        //清除可能存在的输入缓存!
                        IORedirector.clearStdIN();
                        //TODO 此次做出优化，针对命令中的换行做出多行执行的操作!
                        startTest(mCMD);
                        //在执行命令完成后进行回调!
                        onTestEnd();
                        enableStartBtn(true);
                    }
                });
                t.setPriority(Thread.MAX_PRIORITY);
                t.start();
            }
        });

        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopTest();
            }
        });

        btnClearMsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                txtOutConsole.setText("");
                txtErrConsole.setText("");
            }
        });

        btnGui.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mDialogGUI.isShowing()) {
                    View dynamicView = getCommandGUI();
                    if (dynamicView == null) {
                        showToast(getString(R.string.tips_gui_not_support));
                        return;
                    }
                    mDialogGUI.setView(dynamicView);
                    mDialogGUI.show();
                } else {
                    mDialogGUI.dismiss();
                }
            }
        });

        skBarConsoleViewScale.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //有进度发生了改变!!!得到当前比例，设置到新的视图参数中!
                int max = skBarConsoleViewScale.getMax();
                //两大分割区域！
                LinearLayout.LayoutParams newTopParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, progress);
                LinearLayout.LayoutParams newBottomParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, max - progress);
                //进行设置!
                linearLayout_output_out_views.setLayoutParams(newTopParams);
                linearLayout_err_log_group_views.setLayoutParams(newBottomParams);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        ckBoxOpenOutputError.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    //需要显示则显示
                    linearLayout_output_err_views.setVisibility(View.VISIBLE);
                } else {
                    //不需要显示则隐藏!
                    linearLayout_output_err_views.setVisibility(View.GONE);
                }
                showOrDismissDivisionLine();
            }
        });

        ckBoxOpenOutputLog.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    linearLayout_output_log_views.setVisibility(View.VISIBLE);
                } else {
                    linearLayout_output_log_views.setVisibility(View.GONE);
                }
                showOrDismissDivisionLine();
            }
        });
    }

    protected void enableStartBtn(boolean enable) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                btnStart.setEnabled(enable);
            }
        });

    }

    protected void showOrDismissDivisionLine() {
        if (ckBoxOpenOutputLog.isChecked() && ckBoxOpenOutputError.isChecked()) {
            view_bottom_log_err_division_line.setVisibility(View.VISIBLE);
        } else {
            view_bottom_log_err_division_line.setVisibility(View.GONE);
        }
    }

    protected void putCMDAndClosePop(String cmd) {
        putCMDButNoClosePop(cmd);
        //关闭窗口
        mDialogGUI.dismiss();
    }

    protected void putCMDButNoClosePop(String cmd) {
        putCMDPre(cmd);
        //模拟点击，请求开启执行!
        btnStart.performClick();
    }

    protected void putCMDPre(String cmd) {
        //设置默认的命令
        mDefaultCMD = cmd;
        //设置进入输入框中
        edtInputCmd.setText(cmd);
    }

    public void appendReturn(TextView view, String line) {
        if (line.contains("\r")) {
            // 如果存在回车，则光标移动到当前行的首部!
            String str = view.getText().toString();
            String[] arr = str.split("\n");
            boolean lastHasNewLine = str.endsWith("\n");
            if (arr.length > 0) {
                // 替换最后一行,如果新行有很多个\r的话，我们也需要一并处理了!
                String[] newLineReturnArr = line.split("\r");
                if (newLineReturnArr.length > 1) {
                    // 我们需要单独处理其中的每个/r，进行视觉更新!
                    for (String s : newLineReturnArr) {
                        arr[arr.length - 1] = s;
                        Log.d(LOG_TAG, "当前被newLineReturnArr拼接的行: " + arr[arr.length - 1]);
                        // 拼接回去!
                        StringBuilder sb = new StringBuilder();
                        for (int j = 0; j < arr.length; ++j) {
                            if (j != arr.length - 1) sb.append(arr[j]).append("\n");
                            else if (lastHasNewLine) sb.append(arr[j]).append("\n");
                            else sb.append(arr[j]);
                        }
                        view.setText(sb);
                    }
                } else {
                    arr[arr.length - 1] = line;
                    Log.d(LOG_TAG, "当前被arr拼接的行: " + arr[arr.length - 1]);
                    // 拼接回去!
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < arr.length; ++i) {
                        if (i != arr.length - 1) sb.append(arr[i]).append("\n");
                        else if (lastHasNewLine) sb.append(arr[i]).append("\n");
                        else sb.append(arr[i]);
                    }
                    view.setText(sb);
                }
            } else {
                // 只有一行，直接覆盖当前的行!
                view.append(str);
                view.append(line);
            }
        } else {
            // 只有一行，直接覆盖当前的行!
            view.append(line);
        }
    }

    protected abstract View getCommandGUI();

    protected abstract ICommandTools initCMD();

    protected abstract ICommandType initType();

    protected abstract File initOutTarget();

    protected abstract File initErrTarget();

    protected abstract int startTest(ICommandTools cmd);

    protected int startTest(String cmd) {
        if (mCMD == null)
            return -1;
        if (cmd == null)
            return -1;
        int ret = -2;
        //优化，如果是多行命令的操作!
        if (cmd.contains("\n")) {
            //包含换行，则当作多行处理!
            String[] cmds = cmd.split("\n");
            Log.d(LOG_TAG, "警告，当前执行的是多行处理操作!");
            for (String c : cmds) {
                Log.d(LOG_TAG, "当前执行的命令: " + c);
                mCMD.startExecute(c);
            }
            return ret;
        } else {
            //直接执行命令并且在执行命令完成后进行回调!
            ret = mCMD.startExecute(cmd);
        }
        onTestEnd();
        return ret;
    }

    protected abstract void onNewOutLine(String line);

    protected abstract void onNewErrLine(String line);

    protected abstract void onTestEnd();

    protected boolean isTesting() {
        return mCMD.isExecuting();
    }

    protected void stopTest() {
        mCMD.stopExecute();
    }

    protected void showToast(String msg) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                ToastUtil.show(mContext, msg, false);
            }
        });
    }

    protected void showDialog(String title, String msg) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (!mDialog.isShowing()) mDialog.dismiss();
                mDialog.setTitle(title);
                mDialog.setMessage(msg);
                mDialog.show();
            }
        });
    }

    protected void putOutMsg2Console(String msg) {
        if (txtOutConsole != null) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    txtOutConsole.append(msg);
                }
            });
        }
    }

    protected void putErrMsg2Console(String msg) {
        if (txtErrConsole != null) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    txtErrConsole.append(msg);
                }
            });
        }
    }

    protected void putLogMsg2Console(String msg) {
        if (txtLogConsole != null) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    txtLogConsole.append(msg);
                }
            });
        }
    }

    protected void dismissDialog() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mDialog.dismiss();
            }
        });
    }

    private long mBackTime = 0;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP: {
                //音量加，控制台字体大小+
                float size = txtOutConsole.getTextSize();
                txtOutConsole.setTextSize(TypedValue.COMPLEX_UNIT_PX, ++size);
                size = txtErrConsole.getTextSize();
                txtErrConsole.setTextSize(TypedValue.COMPLEX_UNIT_PX, ++size);
            }
            break;
            case KeyEvent.KEYCODE_VOLUME_DOWN: {
                //音量减，控制台字体大小-
                float size = txtOutConsole.getTextSize();
                txtOutConsole.setTextSize(TypedValue.COMPLEX_UNIT_PX, --size);
                size = txtErrConsole.getTextSize();
                txtErrConsole.setTextSize(TypedValue.COMPLEX_UNIT_PX, --size);
            }
            break;
            case KeyEvent.KEYCODE_BACK: {
                if (System.currentTimeMillis() - mBackTime > 2000) {
                    showToast(getString(R.string.double_tap));
                    mBackTime = System.currentTimeMillis();
                    return false;
                } else finish();
            }
            break;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }
}
