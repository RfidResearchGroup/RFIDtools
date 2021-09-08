package com.rfidresearchgroup.activities.tools;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.os.Message;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.rfidresearchgroup.activities.main.BaseActivity;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import com.rfidresearchgroup.common.util.FileUtils;
import com.rfidresearchgroup.common.util.IOUtils;
import com.rfidresearchgroup.common.util.LogUtils;
import com.rfidresearchgroup.common.widget.FillParentWidthDialog;
import com.rfidresearchgroup.common.widget.ToastUtil;
import com.rfidresearchgroup.rfidtools.R;

/*
 * Console Base View
 * 2021/4/15 Project rebuild, views Simplify.
 * */
public abstract class BaseConsoleActivity extends BaseActivity {
    protected ScrollView svOutContain = null;
    protected EditText edtInputCmd = null;
    protected TextView txtOutConsole = null;
    protected Button btnStart = null;
    protected ImageButton btnMenus = null;

    private String mDefaultCMD = "?";
    protected boolean mIsRequsetMode = false;

    protected Context mContext = this;
    protected FillParentWidthDialog mDialogGUI = null;
    protected AlertDialog mDialog = null;

    protected ProcessBuilder processBuilder = null;
    protected Process process = null;

    private long mBackTime = 0;
    private boolean isConsoleReady = false;
    private boolean isTaskBusy = false;

    private final Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            if (msg.what == 1) {
                txtOutConsole.append((CharSequence) msg.obj);
            }
        }
    };

    class StdoutThread extends Thread {

        @Override
        public void run() {
            // the stdout thread must run on process running and activity not destroyed!
            if (isProcessAlive()) {
                //转成字符输入流
                InputStreamReader inputStreamReader = null;
                try {
                    inputStreamReader = new InputStreamReader(process.getInputStream());
                    int len;
                    char[] c = new char[1024];
                    StringBuilder outputString = new StringBuilder();
                    while (isProcessAlive()) {
                        //读取进程输入流中的内容
                        while ((len = inputStreamReader.read(c)) != -1) {
                            String s = new String(c, 0, len);
                            outputString.append(s);

                            Message msgObj = Message.obtain();
                            msgObj.what = 1;
                            msgObj.obj = s;
                            handler.sendMessage(msgObj);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    IOUtils.close(inputStreamReader);
                }
            } else {
                LogUtils.e("The process is dead before thread started.");
            }
        }
    }

    protected Handler mHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_console_layout);

        mDialog = new AlertDialog.Builder(this).create();

        //初始化视图!
        initViews();
        //初始化点击事件
        initOnClick();

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
    }

    private void initViews() {
        svOutContain = findViewById(R.id.svOutContain);
        txtOutConsole = findViewById(R.id.txtOutConsoleMsg);
        edtInputCmd = findViewById(R.id.edtInputCommand);

        btnStart = findViewById(R.id.btnStart);
        btnMenus = findViewById(R.id.btnMenus);

        mDialogGUI = new FillParentWidthDialog(this);
        mDialogGUI.setCanHalfScreen(true);
    }

    private void initOnClick() {
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // If task is running, we can repeat start.
                if (isTaskBusy) return;
                isTaskBusy = true;
                // Create a new thread to prepare resources or execute cmd.
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            // If console is ready, we can send cmd to process to execute.
                            // or else we need to prepare some resources.
                            if (!isConsoleReady) {
                                isConsoleReady = prepareConsole();
                                if (!isConsoleReady) {
                                    Log.d(LOG_TAG, "Prepare console failed, check log and display.");
                                    return;
                                }
                            }
                            // After prepare, We can execute cmd from user.
                            String cmd = edtInputCmd.getText().toString();
                            if (cmd.length() > 0) {
                                start(cmd, (String) null);
                            } else {
                                // The cmd length is invalid.
                                // We need tell user, enter some cmd.
                            }
                        } finally {
                            isTaskBusy = false;
                        }
                    }
                }).start();
            }
        });
    }

    /*
     *  Set cmd to input view and start process.
     * */
    protected void putCMDAndClosePop(String cmd) {
        mDefaultCMD = cmd;
        edtInputCmd.setText(cmd);
        btnStart.performClick();
        mDialogGUI.dismiss();
    }

    /*
     *  We can auto compatible with line breaks
     * */
    public void crlfCompatibility(TextView view, String line) {
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

    /*
     * get gui for custom tool view.
     * */
    protected abstract View getCommandGUI();

    /*
     * Start a process if no cmd running,
     * else send char to stdin
     * */
    protected void start(String exe, String... args) {
        if (processBuilder == null || process == null) {
            ArrayList<String> cmds = new ArrayList<>();
            cmds.add(exe);
            if (args != null && args.length > 0) {
                for (String arg : args) {
                    if (arg != null) {
                        cmds.add(arg);
                    }
                }
            }
            processBuilder = new ProcessBuilder(cmds)
                    .directory(new File(getDefaultCWD()))
                    .redirectErrorStream(true);
            processBuilder.environment().put("LD_LIBRARY_PATH", FileUtils.getNativePath());
            try {
                process = processBuilder.start();
                new StdoutThread().start();
            } catch (IOException e) {
                e.printStackTrace();
                showToast(e.getMessage());
                processBuilder = null;
                process = null;
            }
        } else {
            // The process exists, we can write some text to stdin...
            try {
                if (!exe.endsWith("\n")) {
                    exe += "\n";
                }
                Log.d("???", "发送指令: " + exe);
                process.getOutputStream().write(exe.getBytes());
                process.getOutputStream().flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /*
     *  Kill the process, if exists.
     * */
    protected void stop() {
        if (isProcessAlive()) {
            process.destroy();
            try {
                // must to wait for process exit, and memory resource recycling.
                process.waitFor();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /*
     * If has process running, return true
     * */
    protected boolean isRunning() {
        return isProcessAlive();
    }

    /**
     * Init console before execute cmds.
     *
     * @return If console are ready return true, of else return false.
     */
    protected abstract boolean prepareConsole();

    /**
     * Init console default work directory
     *
     * @return The path of work directory
     */
    protected abstract String getDefaultCWD();

    /*
     * Call on process start
     * */
    protected abstract void onProcessStart();

    /*
     * Call on process exit
     * */
    protected abstract void onProcessExit();

    /*
     * Call on print some char
     * */
    protected abstract void onPrint(String chars);

    /*
     * Call on print one line
     * */
    protected abstract void onPrintln(String chars);

    protected void showToast(String msg) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                ToastUtil.show(mContext, msg, false);
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP: {
                //音量加，控制台字体大小+
                float size = txtOutConsole.getTextSize();
                txtOutConsole.setTextSize(TypedValue.COMPLEX_UNIT_PX, ++size);
            }
            break;
            case KeyEvent.KEYCODE_VOLUME_DOWN: {
                //音量减，控制台字体大小-
                float size = txtOutConsole.getTextSize();
                txtOutConsole.setTextSize(TypedValue.COMPLEX_UNIT_PX, --size);
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

    /*
     * We can get process status
     * if process has return code, that are killed.
     * */
    boolean isProcessAlive() {
        try {
            if (process != null) {
                process.exitValue();
                return false;
            }
        } catch (IllegalThreadStateException e) {
            // e.printStackTrace();
            return true;
        }
        return false;
    }
}
