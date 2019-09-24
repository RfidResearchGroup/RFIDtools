package cn.rrg.rdv.activities.px53x;

import android.content.DialogInterface;
import android.content.Intent;
import androidx.appcompat.app.AlertDialog;
import android.view.View;

import cn.rrg.console.define.ICommandTools;
import cn.rrg.console.define.ICommandType;
import cn.rrg.natives.MfcukTools;
import cn.rrg.rdv.implement.EntryICommandType;

public class MfcukConsoleActivity extends PN53XConsoleActivity {

    @Override
    protected View getCommandGUI() {
        return null;
    }

    @Override
    protected ICommandTools initCMD() {
        return new MfcukTools();
    }

    @Override
    protected ICommandType initType() {
        return new EntryICommandType() {
            @Override
            public boolean isKey(String output) {
                return output.matches("INFO: block \\d recovered KEY: ([A-Za-z0-9])+");
            }

            @Override
            public String parseKey(String output) {
                return output.substring(output.length() - 12, output.length());
            }
        };
    }

    @Override
    protected int startTest(ICommandTools cmd) {
        mDefaultCMD = "mfcuk -C -R 0:A -w -v 2";
        return super.startTest(cmd);
    }

    @Override
    protected void onNewOutLine(String line) {
        super.onNewOutLine(line);
    }

    @Override
    protected void onNewErrLine(String line) {
        super.onNewErrLine(line);
    }

    @Override
    protected void onTestEnd() {
        showToast("监听到Mfcuk执行结束...");
        //如果是结束了!
        if (mIsRequsetMode) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    new AlertDialog.Builder(mContext)
                            .setTitle("温馨提示")
                            .setMessage("检测到当前活动是从其他的组件请求跳转过来的，是否需要结束，然后请求mofc半加测试进行更多的密钥探测与数据导出?")
                            .setPositiveButton("结束", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    //开始请求!
                                    Intent intent = new Intent(mContext, MfocConsoleActivity.class);
                                    intent.putExtra("ctm", true);
                                    startActivity(intent);
                                    finish();
                                }
                            })
                            .setNegativeButton("留下", null).show();
                }
            });
            mIsRequsetMode = false;
        }
    }
}
