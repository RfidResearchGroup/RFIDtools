package cn.rrg.rdv.activities.main;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ScrollView;

import cn.dxl.common.util.AppUtil;
import cn.dxl.common.util.DisplayUtil;
import cn.rrg.com.StandardDriver;
import cn.rrg.rdv.R;
import cn.rrg.rdv.activities.chameleon.ChameleonGUIActivity;
import cn.rrg.rdv.activities.connect.Acr122uHkUsbConnectActivity;
import cn.rrg.rdv.activities.connect.ChameleonUsb2UartConnectActivity;
import cn.rrg.rdv.activities.connect.PN532UartConnectActivity;
import cn.rrg.rdv.activities.connect.Proxmark3Rdv4RRGConnectActivity;
import cn.rrg.rdv.activities.tools.AboutActicity;
import cn.rrg.rdv.activities.tools.MainSettingsActivity;
import cn.rrg.rdv.activities.tools.ToolsAccessActivity;
import cn.rrg.rdv.application.RuntimeProperties;

/**
 * 界面重构在2019/7/29启动!
 * 此次重构主要是将设备的可用列表迁移到主页面进行全览
 * 此页面应当只需要跳转到对应的设备的设备连接界面，不应当做过多的逻辑！
 *
 * @author DXL
 */
public class AppMainDevices extends BaseActivity {

    private Button btnDeviceProxmark3RDV4RRG;
    private Button btnDeviceStandardNfc;
    private Button btnDeviceChameleon;
    private Button btnDeviceAcr122uHk;
    private Button btnDevicePN532;
    private Button btnCommonTools;
    private Button btnAppAbout;

    private ScrollView scrollView;
    private Toolbar toolbar;

    //是否是横屏切换!
    private boolean isBackPressed = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.act_app_devices);
        initViews();
        initActions();
    }

    private void initViews() {
        btnDeviceProxmark3RDV4RRG = findViewById(R.id.btnDeviceProxmark3Rdv4RRG);
        btnDeviceStandardNfc = findViewById(R.id.btnDeviceStandardNfc);
        btnDeviceChameleon = findViewById(R.id.btnDeviceChameleon);
        btnDeviceAcr122uHk = findViewById(R.id.btnDeviceAcr122u);
        btnDevicePN532 = findViewById(R.id.btnDevicePN532);
        btnCommonTools = findViewById(R.id.btnCommonTools);
        btnAppAbout = findViewById(R.id.btnAppAbout);
        scrollView = findViewById(R.id.scrollView);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    private void initActions() {
        btnDeviceProxmark3RDV4RRG.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (RuntimeProperties.isConnected) {
                    //设备已经连接，直接进入功能块!
                    startActivity(new Intent(v.getContext(), Proxmark3Rdv4RRGMain.class));
                } else {
                    //设备未连接，进入连接块!
                    startActivity(new Intent(v.getContext(), Proxmark3Rdv4RRGConnectActivity.class));
                }
            }
        });
        btnDeviceStandardNfc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StandardDriver sd = StandardDriver.get();
                sd.register(context, null);
                if (sd.getAdapter() != null) {
                    if (sd.getAdapter().isEnabled()) {
                        //设备已经连接，直接进入功能块!
                        startActivity(new Intent(context, GeneralNfcDeviceMain.class));
                    } else {
                        //设备未连接，进入连接块!
                        new AlertDialog.Builder(context).setTitle(R.string.tips)
                                .setTitle(R.string.nfc_not_turned_on).setMessage(R.string.nfc_not_open)
                                .setPositiveButton(getString(R.string.open), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        // Goto NFC Settings.
                                        startActivity(new Intent(Settings.ACTION_NFC_SETTINGS));
                                    }
                                }).setNegativeButton(R.string.cancel, null).show();
                    }
                } else {
                    //设备不支持NFC！
                    new AlertDialog.Builder(context).setTitle(R.string.error)
                            .setTitle(R.string.nfc_not_supported).setMessage(R.string.msg_nfc_not_supported)
                            .setPositiveButton(getString(R.string.ok), null).show();
                }
            }
        });
        btnDeviceChameleon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (RuntimeProperties.isConnected) {
                    //设备已经连接，直接进入功能块!
                    startActivity(new Intent(view.getContext(), ChameleonGUIActivity.class));
                } else {
                    //设备未连接，进入连接块!
                    startActivity(new Intent(view.getContext(), ChameleonUsb2UartConnectActivity.class));
                }
            }
        });
        btnDeviceAcr122uHk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (RuntimeProperties.isConnected) {
                    //设备已经连接，直接进入功能块!
                    startActivity(new Intent(view.getContext(), PN53XNfcMain.class));
                } else {
                    //设备未连接，进入连接块!
                    startActivity(new Intent(view.getContext(), Acr122uHkUsbConnectActivity.class));
                }
            }
        });
        btnDevicePN532.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (RuntimeProperties.isConnected) {
                    //设备已经连接，直接进入功能块!
                    startActivity(new Intent(view.getContext(), PN53XNfcMain.class));
                } else {
                    //设备未连接，进入连接块!
                    startActivity(new Intent(view.getContext(), PN532UartConnectActivity.class));
                }
            }
        });
        btnCommonTools.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //直接进入附加功能块!
                startActivity(new Intent(v.getContext(), ToolsAccessActivity.class));
            }
        });
        btnAppAbout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(v.getContext(), AboutActicity.class));
            }
        });
        scrollView.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                //Log.d(LOG_TAG, "ScrollView x: " + scrollView.getScrollX() + " y: " + scrollView.getScrollY());
                int y = scrollView.getScrollY();
                if (y > toolbar.getHeight()) {
                    toolbar.setVisibility(View.GONE);
                    DisplayUtil.dismissStatusAndNavigationBar(AppMainDevices.this);
                    if (!DisplayUtil.isScreenLand(context)) {
                        btnCommonTools.setVisibility(View.GONE);
                        btnAppAbout.setVisibility(View.GONE);
                    }
                }
                if (y <= 0) {
                    toolbar.setVisibility(View.VISIBLE);
                    DisplayUtil.showStatusAndNavigationBar(AppMainDevices.this);
                    if (!DisplayUtil.isScreenLand(context)) {
                        btnCommonTools.setVisibility(View.VISIBLE);
                        btnAppAbout.setVisibility(View.VISIBLE);
                    }
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //加载菜单!
        getMenuInflater().inflate(R.menu.act_app_devices_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //菜单选择事件!
        if (item.getItemId() == R.id.menu_settings) {
            //跳转到设置界面!
            startActivity(new Intent(context, MainSettingsActivity.class));
        }
        /*switch (item.getItemId()) {
            case R.id.menu_settings:
                break;
        }*/
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        isBackPressed = true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //如果是横屏，那我们就没必要全部销毁。
        if (isBackPressed) {
            AppUtil.getInstance().finishAll();
            System.exit(0);
        }
    }
}
