package cn.rrg.rdv.activities.px53x;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;

import java.io.File;

import cn.rrg.freo.Freopen;
import cn.rrg.console.define.ICommandTools;
import cn.rrg.rdv.activities.tools.BaseConsoleActivity;
import cn.rrg.rdv.util.Paths;
import cn.rrg.rdv.util.UnionAction;

public abstract class PN53XConsoleActivity extends BaseConsoleActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        //在所有的初始化开始之前先设置重定向!
        Freopen.setStdEO(Paths.PN53X_FORWARD_O, Freopen.STD_OUT);
        Freopen.setStdEO(Paths.PN53X_FORWARD_E, Freopen.STD_ERR);
        //调用父类方法初始化!
        super.onCreate(savedInstanceState);

        //父类初始化完成后进行设置，PN53X默认关闭日志框!
        ckBoxOpenOutputLog.setChecked(false);
    }

    @Override
    public void startActivity(Intent intent) {
        super.startActivity(intent);
    }

    @Override
    protected File initOutTarget() {
        return new File(Paths.PN53X_FORWARD_O);
    }

    @Override
    protected File initErrTarget() {
        return new File(Paths.PN53X_FORWARD_E);
    }

    @Override
    protected int startTest(ICommandTools cmd) {
        if (isTesting()) {
            showToast("Has some task is excuting...");
            return -1;
        }
        if (edtInputCmd.getText().toString().length() > 0) {
            mDefaultCMD = edtInputCmd.getText().toString();
        }
        return cmd.startExecute(mDefaultCMD);
    }

    @Override
    protected void onNewOutLine(String line) {
        //解析为密钥
        if (mType.isKey(line)) {
            String key = mType.parseKey(line);
            if (key == null) {
                showToast("检测到一组输出可能是密钥，但解析失败!");
                return;
            }
            UnionAction.addKey(key);
            showToast("Found a key: " + key);
            return;
        }
        //解析为数据!
        if (mType.isData(line)) {
            String data = mType.parseData(line);
            if (data == null) {
                showToast("检测到一组输出可能是数据，但解析失败!");
                return;
            }
            UnionAction.addData(data);
            //showToast("Found a dat: " + data);
            return;
        }
        //解析为普通的文本!
        if (mType.isText(line)) {
            //解析动作
            Runnable r = mType.parseText(line);
            //在主线程(UI线程!)中执行!
            if (r != null) mHandler.post(r);
        }
    }

    @Override
    protected void onNewErrLine(String line) {
    }

    @Override
    protected boolean isTesting() {
        return super.isTesting();
    }

    @Override
    protected void stopTest() {
        super.stopTest();
    }
}
