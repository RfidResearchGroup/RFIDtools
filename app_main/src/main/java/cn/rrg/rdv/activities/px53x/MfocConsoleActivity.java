package cn.rrg.rdv.activities.px53x;

import android.content.DialogInterface;
import android.content.Intent;
import androidx.appcompat.app.AlertDialog;
import android.util.Log;
import android.view.View;

import java.io.File;

import cn.rrg.console.define.ICommandTools;
import cn.rrg.console.define.ICommandType;
import cn.rrg.natives.MfocTools;
import cn.rrg.rdv.activities.tools.DumpActivity;
import cn.rrg.rdv.util.DumpUtils;
import cn.rrg.rdv.util.Paths;
import cn.rrg.rdv.util.UnionAction;

public class MfocConsoleActivity extends PN53XConsoleActivity {

    private boolean isNeedRequestMfcuk = false;
    private boolean isAutoExcuted = false;
    private String mDefaultDumpFile = Paths.DUMP_DIRECTORY + File.separator + "data.dump";

    @Override
    protected void onResume() {
        super.onResume();
        //在回到界面时判断是否是组件间开启了传输，然后需要重新开启请求!
        if (isNeedRequestMfcuk || mIsRequsetMode) {
            //判断是否需要重新开启执行!
            if (!isTesting() && isAutoExcuted) {
                btnStart.performClick();
                //不让他多次执行!
                isAutoExcuted = false;
            }
            isNeedRequestMfcuk = false;
            mIsRequsetMode = false;
        }
    }

    @Override
    protected View getCommandGUI() {
        return null;
    }

    @Override
    protected ICommandTools initCMD() {
        return new MfocTools();
    }

    @Override
    protected ICommandType initType() {
        return new ICommandType() {
            @Override
            public boolean isKey(String output) {
                return output.matches(" {2}Found Key: \\w \\[[A-Za-z0-9]+\\]");
            }

            @Override
            public String parseKey(String output) {
                return output.substring(output.indexOf('[') + 1, output.indexOf(']'));
            }

            @Override
            public boolean isData(String output) {
                return output.matches("Block [0-9]{2}, type A, key [A-Za-z0-9]{12} :.*");
            }

            @Override
            public String parseData(String output) {
                return output.substring(output.indexOf(':') + 1).replaceAll(" {2}", "");
            }

            @Override
            public boolean isText(String output) {
                //可以在这里做一些文本识别，非退出行，或是其他的一些行!
                return !isKey(output) && !isData(output);
            }

            @Override
            public Runnable parseText(String output) {
                return null;
            }
        };
    }

    @Override
    protected int startTest(ICommandTools cmd) {
        //判断联动密钥是否可用，拼接进去!
        String[] keys = UnionAction.getKeys();
        StringBuilder keyContact = new StringBuilder();
        if (keys.length >= 1) {
            for (String key : keys) {
                //判断需不需要添加!
                if (!isDefaultKeys(key)) {
                    keyContact.append("-k ").append(key).append(" ");
                }
            }
        }
        mDefaultCMD = "mfoc " + keyContact.toString() + " -O " + mDefaultDumpFile;
        Log.d(LOG_TAG, "测试输出命令: " + mDefaultCMD);
        if (!isTesting()) {
            //没有正在执行的任务，先清空可能缓存的联动数据!
            UnionAction.removeData();
        }
        //执行父类，进行真正的逻辑判断，检查输入与是否在执行，进行下一步的操作!
        mIsRequsetMode = true;
        return super.startTest(cmd);
    }

    private boolean isDefaultKeys(String key) {
        if (key.equalsIgnoreCase("ffffffffffff")) return true;
        if (key.equalsIgnoreCase("a0a1a2a3a4a5")) return true;
        if (key.equalsIgnoreCase("d3f7d3f7d3f7")) return true;
        if (key.equalsIgnoreCase("000000000000")) return true;
        if (key.equalsIgnoreCase("b0b1b2b3b4b5")) return true;
        if (key.equalsIgnoreCase("1a982c7e459a")) return true;
        if (key.equalsIgnoreCase("aabbccddeeff")) return true;
        if (key.equalsIgnoreCase("714c5c886e97")) return true;
        if (key.equalsIgnoreCase("587ee5f9350f")) return true;
        if (key.equalsIgnoreCase("a0478cc39091")) return true;
        if (key.equalsIgnoreCase("533cb6c723f6")) return true;
        if (key.equalsIgnoreCase("8fd0a4f256e9")) return true;
        return false;
    }

    @Override
    protected void stopTest() {
        //强行停止的情况则不自动返回上一层!
        mIsRequsetMode = false;
        super.stopTest();
    }

    @Override
    protected void onNewOutLine(String line) {
        super.onNewOutLine(line);
    }

    @Override
    protected void onNewErrLine(String line) {
        super.onNewErrLine(line);
        if (line.equalsIgnoreCase("No sector encrypted with the default key has been found, exiting..")) {
            //这是没有密钥的错误消息，需要请求全加密来进行破解!
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    new AlertDialog.Builder(MfocConsoleActivity.this)
                            .setTitle("请求跳转")
                            .setMessage("我们发现您的卡片是全加密，因此mfoc半加测试无法继续进行，是否允许我们请求mfcuk全加测试探测一个有效密钥?")
                            .setPositiveButton("允许请求", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    isNeedRequestMfcuk = true;
                                    //开始封装消息进行请求!
                                    String cmd = "mfcuk -C -R 0:A -w -v 2";
                                    //开始请求!
                                    Intent intent = new Intent(MfocConsoleActivity.this, MfcukConsoleActivity.class);
                                    intent.putExtra("cmd", cmd);
                                    intent.putExtra("ctm", true);
                                    startActivity(intent);
                                    finish();
                                }
                            })
                            .setNegativeButton("我再玩玩", null)
                            .show();
                }
            });
        }
    }

    @Override
    protected void onTestEnd() {
        //不需要尝试反转数据!
        //UnionAction.reverse();
        //需要直接退出当前ACT，返回可能存在的上层ACT，
        //TODO 注意，此时应当对数据进行判断，如果有足够数量的数据则调用dump组件显示!
        String[] datas = UnionAction.getDatas();
        int dataCount = datas.length;
        for (String line : datas) {
            Log.d(LOG_TAG, "line: " + line);
            if (!DumpUtils.isBlockData(line)) {
                showToast("解析出来的数据格式有误!");
                return;
            }
        }
        if (dataCount == 64 || dataCount == 128 || dataCount == 256) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    //先提醒用户是否需要跳转!
                    new AlertDialog.Builder(mContext)
                            .setTitle("请求跳转")
                            .setMessage("我们发现了可能有效的数据，需要我们跳转到Dump编辑器显示这个数据么?")
                            .setPositiveButton("需要", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    //拥有足够的扇区数据，可以被解析显示!
                                    //TODO 因为控制台的输出有点问题，因此不能直接利用，需要打开输出的dump！
                                    Intent intent = new Intent(mContext, DumpActivity.class);
                                    intent.putExtra("isFileMode", true);
                                    intent.putExtra("isConnected", true);
                                    intent.putExtra("file", mDefaultDumpFile);
                                    startActivity(intent);
                                    //使用后直接移除数据！
                                    UnionAction.removeData();
                                    //结束此活动!
                                    finish();
                                }
                            })
                            .setNegativeButton("不需要", null).show();
                }
            });
        }
    }
}
