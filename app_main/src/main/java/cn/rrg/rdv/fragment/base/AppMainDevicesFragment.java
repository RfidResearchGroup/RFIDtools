package cn.rrg.rdv.fragment.base;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import cn.dxl.common.util.AppUtil;
import cn.rrg.rdv.R;
import cn.rrg.rdv.binder.BannerImageViewBinder;
import cn.rrg.rdv.binder.DeviceInfoViewBinder;
import cn.rrg.rdv.binder.TitleTextViewBinder;
import cn.rrg.rdv.javabean.BannerBean;
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
public class AppMainDevicesFragment extends BaseFragment {

    private RecyclerView rvMainContainer;

    private AbstractDeviceModel[] models;

    //是否是横屏切换!
    private boolean isBackPressed = false;

    private MultiTypeAdapter multiTypeAdapter;
    private Items deviceItems;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.act_app_devices, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        initViews(view);
        initActions();

        // USB_Device opt
        models = new AbstractDeviceModel[]{
                new Acr122uUsbRawModel(),
                new ChameleonUsb2UartModel(),
                new PN532Usb2UartModel()
        };

        for (AbstractDeviceModel model : models) {
            model.register(view.getContext());
        }

        initBanner();
        initDeviceList();
        initToolsList();
    }

    private void initViews(View view) {
        rvMainContainer = view.findViewById(R.id.rvMainContainer);
        multiTypeAdapter = new MultiTypeAdapter();
        deviceItems = new Items();
        multiTypeAdapter.register(DeviceInfoBean.class, new DeviceInfoViewBinder());
        multiTypeAdapter.register(TitleTextBean.class, new TitleTextViewBinder());
        multiTypeAdapter.register(BannerBean.class, new BannerImageViewBinder());
        rvMainContainer.setLayoutManager(new LinearLayoutManager(view.getContext()));
        multiTypeAdapter.setItems(deviceItems);
        rvMainContainer.setAdapter(multiTypeAdapter);
    }

    private void initActions() {
        /*StandardDriver sd = StandardDriver.get();
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
        }*/
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
        deviceItems.add(beanParent);
        multiTypeAdapter.notifyDataSetChanged();
    }

    private void initDeviceList() {
        // init device list!
        deviceItems.add(new DeviceInfoBean("PhoneNFC Reader", R.drawable.phone_nfc_icon));
        deviceItems.add(new DeviceInfoBean("Proxmark3 Rdv4.0", R.drawable.rdv4));
        deviceItems.add(new DeviceInfoBean("ChameleonMini RevE", R.drawable.chameleon_rdv2));
        deviceItems.add(new DeviceInfoBean("PN532 NXP Module", R.drawable.pn532core));
        deviceItems.add(new DeviceInfoBean("ACR122U ACS", R.drawable.acr122u));
        multiTypeAdapter.notifyDataSetChanged();
    }

    private void initToolsList() {
    }

    public void onBackPressed() {
        isBackPressed = true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //如果是横屏动作，那我们就没必要全部销毁。
        if (isBackPressed) {
            AppUtil.getInstance().finishAll();
            System.exit(0);
        }
        for (AbstractDeviceModel model : models) {
            model.unregister(getContext());
        }
    }
}
