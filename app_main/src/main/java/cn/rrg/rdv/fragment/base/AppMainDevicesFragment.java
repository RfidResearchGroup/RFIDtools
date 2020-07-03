package cn.rrg.rdv.fragment.base;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import cn.dxl.common.util.AppUtil;
import cn.dxl.mifare.StdMifareImpl;
import cn.rrg.devices.PN53X;
import cn.rrg.rdv.R;
import cn.rrg.rdv.activities.chameleon.ChameleonGUIActivity;
import cn.rrg.rdv.activities.connect.Acr122uHkUsbConnectActivity;
import cn.rrg.rdv.activities.connect.ChameleonUsb2UartConnectActivity;
import cn.rrg.rdv.activities.connect.PN532UartConnectActivity;
import cn.rrg.rdv.activities.connect.PN53XUsbBulkTransferActivity;
import cn.rrg.rdv.activities.connect.Proxmark3Rdv4RRGConnectActivity;
import cn.rrg.rdv.activities.main.GeneralNfcDeviceMain;
import cn.rrg.rdv.activities.main.PN53XNfcMain;
import cn.rrg.rdv.activities.proxmark3.rdv4_rrg.Proxmark3NewTerminalInitActivity;
import cn.rrg.rdv.binder.BannerImageViewBinder;
import cn.rrg.rdv.binder.DeviceInfoViewBinder;
import cn.rrg.rdv.binder.TitleTextViewBinder;
import cn.rrg.rdv.javabean.BannerBean;
import cn.rrg.rdv.javabean.DeviceInfoBean;
import cn.rrg.rdv.javabean.TitleTextBean;
import me.drakeet.multitype.Items;
import me.drakeet.multitype.MultiTypeAdapter;

/**
 * UI redesign starts on July 29, 2019!
 *
 * @author DXL
 */
public class AppMainDevicesFragment extends BaseFragment {

    public static String ACTION_CONNECTION_STATE_UPDATE = "AppMainDevicesFragment.connection_state_update";
    public static String EXTRA_CONNECTION_STATE = "state";

    private boolean isBackPressed = false;
    private boolean isConnected = false;

    private MultiTypeAdapter multiTypeAdapter;
    private Items deviceItems;

    private DeviceInfoBean deviceInfoBean;

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_CONNECTION_STATE_UPDATE.equals(action)) {
                isConnected = intent.getBooleanExtra(EXTRA_CONNECTION_STATE, false);
                updateDeviceStatus(isConnected);
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.act_app_devices, container, false);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Context context = getContext();
        if (context != null) {
            LocalBroadcastManager.getInstance(context).registerReceiver(
                    receiver, new IntentFilter(ACTION_CONNECTION_STATE_UPDATE)
            );
        }

        multiTypeAdapter = new MultiTypeAdapter();
        deviceItems = new Items();

        initDeviceList(context);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        initViews(view);
        initBanner();
        updateDeviceStatus(isConnected);
    }

    private void initViews(View view) {
        RecyclerView rvMainContainer = view.findViewById(R.id.rvMainContainer);
        multiTypeAdapter.register(DeviceInfoBean.class, new DeviceInfoViewBinder());
        multiTypeAdapter.register(TitleTextBean.class, new TitleTextViewBinder());
        multiTypeAdapter.register(BannerBean.class, new BannerImageViewBinder());
        GridLayoutManager.SpanSizeLookup spanSizeLookup = new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                Object type = deviceItems.get(position);
                if (type instanceof BannerBean) {
                    return 2;
                }
                if (type instanceof DeviceInfoBean) {
                    return 1;
                }
                return 1;
            }
        };
        GridLayoutManager gridLayoutManager = new GridLayoutManager(view.getContext(), 2);
        gridLayoutManager.setSpanSizeLookup(spanSizeLookup);
        rvMainContainer.setLayoutManager(gridLayoutManager);
        multiTypeAdapter.setItems(deviceItems);
        rvMainContainer.setAdapter(multiTypeAdapter);
    }

    private void initBanner() {
        // 添加轮播图!
        BannerBean beanParent = new BannerBean();
        ArrayList<BannerBean> tmpList = new ArrayList<>();
        String[] urls = new String[]{
                "http://www.proxgrind.com/wp-content/uploads/2020/01/Untitled-design.png",
                "http://www.proxgrind.com/wp-content/uploads/2019/07/2-1.png",
                "http://www.proxgrind.com/wp-content/uploads/2019/07/3-1.png",
                "http://www.proxgrind.com/wp-content/uploads/2019/07/4-1.png",
                "http://www.proxgrind.com/wp-content/uploads/2018/02/5.png"
        };
        for (String url : urls) {
            tmpList.add(new BannerBean(url));
        }
        beanParent.setSubs(tmpList.toArray(new BannerBean[0]));
        deviceItems.add(0, beanParent);
        multiTypeAdapter.notifyDataSetChanged();
    }

    private void initDeviceList(Context context) {
        // init device list!
        deviceItems.add(new DeviceInfoBean("PhoneNFC Reader", R.drawable.phone_nfc_icon) {
            @Override
            public void onClick() {
                if (StdMifareImpl.hasMifareClassicSupport(context)) {
                    if (StdMifareImpl.isNfcOpened(context)) {
                        startActivity(new Intent(context, GeneralNfcDeviceMain.class));
                    } else {
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
                    new AlertDialog.Builder(context).setTitle(R.string.error)
                            .setTitle(R.string.nfc_not_supported).setMessage(R.string.msg_nfc_not_supported)
                            .setPositiveButton(getString(R.string.ok), null).show();
                }
            }
        });
        deviceItems.add(new DeviceInfoBean("Proxmark3 Rdv4.0 for Termux", R.drawable.rdv4) {
            @Override
            public void onClick() {
                deviceInfoBean = this;
                connectOrGotoFunctionMain(
                        Proxmark3Rdv4RRGConnectActivity.class,
                        Proxmark3NewTerminalInitActivity.class
                );
            }
        });
        deviceItems.add(new DeviceInfoBean("ChameleonMini RevE", R.drawable.chameleon_rdv2) {
            @Override
            public void onClick() {
                deviceInfoBean = this;
                connectOrGotoFunctionMain(
                        ChameleonUsb2UartConnectActivity.class,
                        ChameleonGUIActivity.class
                );
            }
        });
        deviceItems.add(new DeviceInfoBean(PN53X.NAME.PN532, R.drawable.pn532core) {
            @Override
            public void onClick() {
                deviceInfoBean = this;
                connectOrGotoFunctionMain(
                        PN532UartConnectActivity.class,
                        PN53XNfcMain.class
                );
            }
        });
        deviceItems.add(new DeviceInfoBean(PN53X.NAME.ACR122, R.drawable.acr122u) {
            @Override
            public void onClick() {
                deviceInfoBean = this;
                connectOrGotoFunctionMain(
                        Acr122uHkUsbConnectActivity.class,
                        PN53XNfcMain.class
                );
            }
        });
        deviceItems.add(new PN53XDeviceInfoBean(PN53X.NAME.NXP_PN533, R.drawable.pn532core));
        deviceItems.add(new PN53XDeviceInfoBean(PN53X.NAME.NXP_PN531, R.drawable.pn532core));
        deviceItems.add(new PN53XDeviceInfoBean(PN53X.NAME.SCM_SCL3711, R.drawable.pn532core));
        deviceItems.add(new PN53XDeviceInfoBean(PN53X.NAME.SCM_SCL3712, R.drawable.pn532core));
        deviceItems.add(new PN53XDeviceInfoBean(PN53X.NAME.SONY_PN531, R.drawable.pn532core));
        deviceItems.add(new PN53XDeviceInfoBean(PN53X.NAME.SONY_RCS360, R.drawable.pn532core));
        deviceItems.add(new PN53XDeviceInfoBean(PN53X.NAME.ASK_LOGO, R.drawable.pn532core));
        multiTypeAdapter.notifyDataSetChanged();
    }

    private void updateDeviceStatus(boolean status) {
        // update device status to bean!
        if (status) {
            if (deviceInfoBean != null) {
                deviceInfoBean.setEnable(status);
            }
            for (Object tmp : deviceItems) {
                if (tmp instanceof DeviceInfoBean) {
                    if (tmp != deviceInfoBean) {
                        ((DeviceInfoBean) tmp).setEnable(false);
                    }
                }
            }
        } else {
            for (Object tmp : deviceItems) {
                if (tmp instanceof DeviceInfoBean) {
                    ((DeviceInfoBean) tmp).setEnable(true);
                }
            }
        }
        // update view from adapter!
        multiTypeAdapter.notifyDataSetChanged();
    }

    private void connectOrGotoFunctionMain(Class connPage, Class main) {
        if (isConnected) {
            startActivity(new Intent(getContext(), main));
        } else {
            startActivity(new Intent(getContext(), connPage));
        }
    }

    public void onBackPressed() {
        isBackPressed = true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (isBackPressed) {
            AppUtil.getInstance().finishAll();
        }
        Context context = getContext();
        if (context != null) {
            LocalBroadcastManager.getInstance(context).unregisterReceiver(receiver);
        }
    }

    class PN53XDeviceInfoBean extends DeviceInfoBean {

        PN53XDeviceInfoBean(@NonNull String name, int icon) {
            super(name, icon);
        }

        @Override
        public void onClick() {
            deviceInfoBean = this;
            if (isConnected) {
                startActivity(
                        new Intent(getContext(), PN53XNfcMain.class)
                                .putExtra("name", getName())
                );
            } else {
                startActivity(new Intent(getContext(), PN53XUsbBulkTransferActivity.class));
            }
        }
    }
}
