package cn.rrg.rdv.activities.px53x;

import android.content.DialogInterface;
import android.content.Intent;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import cn.dxl.common.util.ViewUtil;
import cn.dxl.common.widget.ToastUtil;
import cn.rrg.console.define.ICommandTools;
import cn.rrg.console.define.ICommandType;
import cn.rrg.natives.MfcukTools;
import cn.rrg.natives.PN53XTagLeaksAdapter;
import cn.rrg.rdv.R;
import cn.rrg.rdv.implement.EntryICommandType;

public class MfcukConsoleActivity extends PN53XConsoleActivity {

    private AlertDialog checkTipsDialog;
    private TextView txtMsgView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        checkTipsDialog = new AlertDialog.Builder(context).create();
        checkTipsDialog.setTitle(R.string.tips);
        View continer = ViewUtil.inflate(context, R.layout.dialog_working_msg);
        txtMsgView = continer.findViewById(R.id.text1);
        txtMsgView.setText(R.string.msg_working_darkside_check);
        checkTipsDialog.setView(continer);
        checkTipsDialog.setCancelable(false);
    }

    private void showWorkingDialog(boolean show) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (show)
                    checkTipsDialog.show();
                else
                    checkTipsDialog.dismiss();
            }
        });
    }

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
        showWorkingDialog(true);
        // Judge if there is have a nack loophole
        if (!new PN53XTagLeaksAdapter().isDarksideSupported()) {
            showWorkingDialog(false);
            // darkside no supported.
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    new AlertDialog.Builder(context)
                            .setTitle(R.string.tips)
                            .setMessage(getString(R.string.msg_darkside_no_supported)).show();
                }
            });
            return -1;
        } else {
            showWorkingDialog(false);
            ToastUtil.show(context, getString(R.string.msg_darkside_support), false);
        }
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
