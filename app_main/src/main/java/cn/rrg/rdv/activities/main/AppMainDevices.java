package cn.rrg.rdv.activities.main;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.provider.Settings;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.OrientationHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ScrollView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import cn.dxl.common.util.AppUtil;
import cn.dxl.common.util.DisplayUtil;
import cn.rrg.com.StandardDriver;
import cn.rrg.rdv.R;
import cn.rrg.rdv.activities.binder.DeviceInfoViewBinder;
import cn.rrg.rdv.activities.binder.TitleTextViewBinder;
import cn.rrg.rdv.activities.chameleon.ChameleonGUIActivity;
import cn.rrg.rdv.activities.connect.Acr122uHkUsbConnectActivity;
import cn.rrg.rdv.activities.connect.ChameleonUsb2UartConnectActivity;
import cn.rrg.rdv.activities.connect.PN532UartConnectActivity;
import cn.rrg.rdv.activities.connect.Proxmark3Rdv4RRGConnectActivity;
import cn.rrg.rdv.activities.tools.AboutActicity;
import cn.rrg.rdv.activities.tools.MainSettingsActivity;
import cn.rrg.rdv.activities.tools.ToolsAccessActivity;
import cn.rrg.rdv.application.RuntimeProperties;
import cn.rrg.rdv.javabean.DeviceInfoBean;
import cn.rrg.rdv.javabean.TitleTextBean;
import cn.rrg.rdv.models.AbstractDeviceModel;
import cn.rrg.rdv.models.Acr122uUsbRawModel;
import cn.rrg.rdv.models.ChameleonUsb2UartModel;
import cn.rrg.rdv.models.PN532Usb2UartModel;
import me.drakeet.multitype.Items;
import me.drakeet.multitype.MultiTypeAdapter;

/**
 * 界面重构在2019/7/29启动!
 * 此次重构主要是将设备的可用列表迁移到主页面进行全览
 * 此页面应当只需要跳转到对应的设备的设备连接界面，不应当做过多的逻辑！
 *
 * @author DXL
 */
public class AppMainDevices extends BaseActivity {

    private RecyclerView rvMainContainer;

    private Button btnDeviceProxmark3RDV4RRG;
    private Button btnDeviceStandardNfc;
    private Button btnDeviceChameleon;
    private Button btnDeviceAcr122uHk;
    private Button btnDevicePN532;
    private FloatingActionButton btnCommonTools;

    private AbstractDeviceModel[] models;

    //是否是横屏切换!
    private boolean isBackPressed = false;

    private MultiTypeAdapter multiTypeAdapter;
    private Items deviceItems;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_app_devices);

        initViews();
        initActions();

        // USB_Device opt
        models = new AbstractDeviceModel[]{
                new Acr122uUsbRawModel(),
                new ChameleonUsb2UartModel(),
                new PN532Usb2UartModel()
        };

        for (AbstractDeviceModel model : models) {
            model.register(this);
        }

        initDeviceList();
        initToolsList();
    }

    private void initViews() {
        rvMainContainer = findViewById(R.id.rvMainContainer);
        multiTypeAdapter = new MultiTypeAdapter();
        deviceItems = new Items();
        multiTypeAdapter.register(DeviceInfoBean.class, new DeviceInfoViewBinder());
        multiTypeAdapter.register(TitleTextBean.class, new TitleTextViewBinder());
        rvMainContainer.setLayoutManager(new LinearLayoutManager(context));
        DividerItemDecoration decoration = new DividerItemDecoration(context, DividerItemDecoration.VERTICAL);
        decoration.setDrawable(getResources().getDrawable(R.drawable.shape_divider_grey));
        rvMainContainer.addItemDecoration(decoration);
        multiTypeAdapter.setItems(deviceItems);
        rvMainContainer.setAdapter(multiTypeAdapter);

        btnDeviceProxmark3RDV4RRG = findViewById(R.id.btnDeviceProxmark3Rdv4RRG);
        btnDeviceStandardNfc = findViewById(R.id.btnDeviceStandardNfc);
        btnDeviceChameleon = findViewById(R.id.btnDeviceChameleon);
        btnDeviceAcr122uHk = findViewById(R.id.btnDeviceAcr122u);
        btnDevicePN532 = findViewById(R.id.btnDevicePN532);
        btnCommonTools = findViewById(R.id.btnCommonTools);
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
    }

    private void initDeviceList() {
        deviceItems.add(new TitleTextBean("Device supported list"));
        // init device list!
        deviceItems.add(new DeviceInfoBean("PhoneNFC Reader", R.drawable.phone_nfc_icon));
        deviceItems.add(new DeviceInfoBean("Proxmark3 Rdv4.0", R.drawable.rdv4));
        deviceItems.add(new DeviceInfoBean("ChameleonMini RevE", R.drawable.chameleon_rdv2));
        deviceItems.add(new DeviceInfoBean("PN532 NXP Module", R.drawable.pn532core));
        deviceItems.add(new DeviceInfoBean("ACR122U ACS", R.drawable.acr122u));
        multiTypeAdapter.notifyDataSetChanged();
    }

    private void initToolsList() {
        deviceItems.add(new TitleTextBean("Tools"));
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        isBackPressed = true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //如果是横屏动作，那我们就没必要全部销毁。
        if (isBackPressed) {
            AppUtil.getInstance().finishAll();
            System.exit(0);
        }
        for (AbstractDeviceModel model : models) {
            model.unregister(this);
        }
    }
}
