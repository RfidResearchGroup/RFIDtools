package cn.rrg.rdv.activities.tools;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

import cn.dxl.common.util.AppUtil;
import cn.dxl.common.widget.FilesSelectorDialog;
import cn.rrg.rdv.R;
import cn.rrg.rdv.activities.main.BaseActivity;
import cn.rrg.rdv.util.Commons;
import cn.dxl.common.util.FileUtil;
import cn.rrg.rdv.util.Paths;
import cn.dxl.common.util.RestartUtils;
import cn.dxl.common.widget.ToastUtil;

public class AboutActicity extends BaseActivity {

    private TextView txtUserHelp = null;
    private TextView txtDisclaimer = null;
    private TextView txtJoinGroup = null;
    private TextView txtContactDev = null;
    private TextView txtShowVersion = null;
    private TextView txtSendLogFile = null;
    private TextView txtCleanData = null;

    private Button btnGo2ProxgrindWebsite;
    private Button btnGo2RRGWebsite;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setContentView(R.layout.act_app_about);
        super.onCreate(savedInstanceState);

        initViews();
        initActions();
        showVersion();
    }

    private void initViews() {
        txtUserHelp = findViewById(R.id.txtUserHelp);
        txtDisclaimer = findViewById(R.id.txtDisclaimer);
        txtJoinGroup = findViewById(R.id.txtJoinQQGroup);
        txtContactDev = findViewById(R.id.txtContactDev);
        txtShowVersion = findViewById(R.id.txtShowAppVersion);
        txtSendLogFile = findViewById(R.id.txtSendLogFile);
        txtCleanData = findViewById(R.id.txtCleanData);

        btnGo2ProxgrindWebsite = findViewById(R.id.btnGo2ProxgrindWebsite);
        btnGo2RRGWebsite = findViewById(R.id.btnGo2RRGWebsite);
    }

    private void initActions() {
        txtUserHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //建立视图
                WebView wv = new WebView(AboutActicity.this);
                //加载文件
                wv.loadUrl("file:///android_asset/help.html");
                //加载进对话框中
                new AlertDialog.Builder(AboutActicity.this)
                        .setView(wv).show();
            }
        });

        txtDisclaimer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });

        txtJoinGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean result = joinQQGroup("4QQrqUHKMNZDNWLvi4kEWeEDHYMekp7x");
                if (!result) {
                    Toast.makeText(AboutActicity.this, "未安装手Q或安装的版本不支持!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        txtContactDev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Commons.callQQ(AboutActicity.this, "64101226", new Runnable() {
                    @Override
                    public void run() {
                        ToastUtil.show(context, getString(R.string.error), false);
                    }
                });
            }
        });

        txtSendLogFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new FilesSelectorDialog.Builder(AboutActicity.this)
                        .setPathOnLoad(Paths.LOG_DIRECTORY)
                        .setTitle(R.string.logList)
                        .setOnSelectListener(new FilesSelectorDialog.OnSelectListener() {
                            @Override
                            public void selected(File file) {
                                FileUtil.shareFile(AboutActicity.this, file);
                            }
                        })
                        .create().show();
            }
        });

        txtCleanData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(AboutActicity.this)
                        .setTitle(R.string.warning)
                        .setMessage(R.string.tipls_clearData)
                        .setPositiveButton(getString(R.string.clear), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(AboutActicity.this, getString(R.string.cleaning), Toast.LENGTH_SHORT).show();
                                FileUtil.delete(new File(Paths.TOOLS_DIRECTORY));
                                Toast.makeText(AboutActicity.this, getString(R.string.clearFinish), Toast.LENGTH_SHORT).show();
                                //必须重启APP
                                RestartUtils.restartAPP(AboutActicity.this, 1000, new RestartUtils.OnExitAction() {
                                    @Override
                                    public boolean usingSystemExit() {
                                        //调用act管理器结束所有的活动！
                                        AppUtil.getInstance().finishAll();
                                        return false;
                                    }
                                });
                                AppUtil.getInstance().finishAll();
                            }
                        })
                        .setNegativeButton(R.string.cancel, null)
                        .show();
            }
        });

        btnGo2ProxgrindWebsite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Commons.openUrl(context, "http://proxgrind.com");
            }
        });

        btnGo2RRGWebsite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Commons.openUrl(context, "https://rfidresearchgroup.com");
            }
        });
    }

    private void showVersion() {
        PackageManager pm = getPackageManager();
        try {
            PackageInfo pi = pm.getPackageInfo(getPackageName(), 0);
            String vn = getString(R.string.version) + ": " + String.valueOf(pi.versionName);
            txtShowVersion.setText(vn);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            txtShowVersion.setText(R.string.error);
        }
    }

    /****************
     *
     * 发起添加群流程。群号：NFC PN532 计算机 单片机(573359551) 的 key 为： 4QQrqUHKMNZDNWLvi4kEWeEDHYMekp7x
     * 调用 joinQQGroup(4QQrqUHKMNZDNWLvi4kEWeEDHYMekp7x) 即可发起手Q客户端申请加群 NFC PN532 计算机 单片机(573359551)
     *
     * @param key 由官网生成的key
     * @return 返回true表示呼起手Q成功，返回fals表示呼起失败
     ******************/
    public boolean joinQQGroup(String key) {
        Intent intent = new Intent();
        intent.setData(Uri.parse("mqqopensdkapi://bizAgent/qm/qr?url=http%3A%2F%2Fqm.qq.com%2Fcgi-bin%2Fqm%2Fqr%3Ffrom%3Dapp%26p%3Dandroid%26k%3D" + key));
        // 此Flag可根据具体产品需要自定义，如设置，则在加群界面按返回，返回手Q主界面，不设置，按返回会返回到呼起产品界面    //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            startActivity(intent);
            return true;
        } catch (Exception e) {
            // 未安装手Q或安装的版本不支持
            return false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
