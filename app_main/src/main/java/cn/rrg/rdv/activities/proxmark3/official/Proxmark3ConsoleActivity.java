package cn.rrg.rdv.activities.proxmark3.official;

import android.os.Bundle;

import androidx.annotation.Nullable;

import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;

import java.io.File;

import cn.dxl.common.util.FileUtils;
import cn.rrg.console.define.ICommandType;
import cn.rrg.freo.IORedirector;
import cn.rrg.console.define.ICommandTools;
import cn.rrg.natives.Proxmark3Tools;
import cn.rrg.rdv.activities.tools.BaseConsoleActivity;
import cn.rrg.rdv.implement.EntryICommandType;
import cn.rrg.rdv.util.Paths;
import cn.rrg.rdv.R;

public abstract class Proxmark3ConsoleActivity
        extends BaseConsoleActivity
        implements View.OnClickListener,
        CompoundButton.OnCheckedChangeListener {

    //滞留一个视图实例，可用如GUI，子控件实现!
    protected View guiView = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        //重定向!
        IORedirector.setStdEO(Paths.PM3_FORWARD_O, IORedirector.STD_OUT);
        IORedirector.setStdEO(Paths.PM3_FORWARD_E, IORedirector.STD_ERR);
        IORedirector.setStdIN(Paths.COMMON_FORWARD_I);
        //父类先初初始化!
        super.onCreate(savedInstanceState);

        //默认关闭错误消息输出区域(用不上...)
        ckBoxOpenOutputError.setChecked(false);
        //覆盖父类的停止按钮点击实例
        btnStop.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btnStop) {//判断当前是否有客户端可终止的任务在执行，是的话则终止!
            new Thread(new Runnable() {
                @Override
                public void run() {
                    //TODO 此处进行IO，模拟键盘输入!
                    stopPM3Client4KeyBorad();
                }
            }).start();
        }
    }

    //通过键盘输入结束任务!
    protected void stopPM3Client4KeyBorad() {
        FileUtils.writeString(new File(Paths.COMMON_FORWARD_I), "\n", true);
    }

    @Override
    protected void onDestroy() {
        //非常有必要进行相关的结束操作!
        stopPM3Client4KeyBorad();
        mCMD.stopExecute();
        super.onDestroy();
    }

    /*
     * 重写视图实例寻找，方便进行控制!
     * */
    @Override
    public <T extends View> T findViewById(int id) {
        T view = super.findViewById(id);
        return view != null ? view : guiView != null ? guiView.findViewById(id) : null;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        //don't need process!!!
    }

    @Override
    protected View getCommandGUI() {
        return guiView;
    }

    @Override
    protected ICommandTools initCMD() {
        return new Proxmark3Tools();
    }

    @Override
    protected File initOutTarget() {
        return new File(Paths.PM3_FORWARD_O);
    }

    @Override
    protected File initErrTarget() {
        return new File(Paths.PM3_FORWARD_E);
    }

    @Override
    protected int startTest(ICommandTools cmd) {
        mDefaultCMD = "help";
        if (edtInputCmd.getText().toString().length() > 0) {
            mDefaultCMD = edtInputCmd.getText().toString();
        }
        //此处做出多行执行优化!
        int ret = -2;
        //优化，如果是多行命令的操作!
        if (mDefaultCMD.contains("\n")) {
            //包含换行，则当作多行处理!
            String[] cmds = mDefaultCMD.split("\n");
            Log.d(LOG_TAG, "警告，当前执行的是多行处理操作!");
            for (String c : cmds) {
                Log.d(LOG_TAG, "当前执行的命令: " + c);
                mCMD.startExecute(c);
            }
            return ret;
        } else {
            //直接执行命令并且在执行命令完成后进行回调!
            ret = cmd.startExecute(mDefaultCMD);
        }
        return ret;
    }

    @Override
    protected void onNewOutLine(String line) {
    }

    @Override
    protected void onNewErrLine(String line) {
    }

    @Override
    protected ICommandType initType() {
        return new EntryICommandType();
    }

    @Override
    protected void onTestEnd() {
    }

    @Override
    protected boolean isTesting() {
        return super.isTesting();
    }

    @Override
    protected void stopTest() {
        //super.stopTest();
        //TODO 无必要停止PM3的底层线程!
    }
}