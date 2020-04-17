package cn.rrg.rdv.presenter;

import android.content.Context;

import cn.proxgrind.com.DevCallback;
import cn.rrg.rdv.javabean.DevBean;
import cn.rrg.rdv.callback.ConnectCallback;
import cn.rrg.rdv.callback.InitNfcCallback;
import cn.rrg.rdv.models.AbstractDeviceModel;
import cn.rrg.rdv.view.DeviceView;

public class DevicePresenter<T extends DeviceView>
        extends BasePresenter<DeviceView> {

    AbstractDeviceModel adm;

    //在构造函数中初始化设备中介者!
    public DevicePresenter(AbstractDeviceModel model) {
        adm = model;
    }

    T subView;

    //优化中介类
    public void attachSubView(T view) {
        this.subView = view;
    }

    public void detachSubView() {
        this.view = null;
    }

    public boolean isSubViewAttach() {
        return view != null;
    }

    public T getView() {
        return subView;
    }

    //注册设备和广播!
    public void register() {
        adm.register();
    }

    //解注册广播
    public void unregister() {
        //解注册所有的驱动!
        adm.unregister();
    }

    //更新发现的设备List
    public void discovery(Context context) {
        adm.discovery(context);
    }

    //设置回调!
    public void addCallback(DevCallback<DevBean> callback) {
        adm.addCallback(callback);
    }

    //移除回调!
    public void removeCallback(DevCallback<DevBean> callback) {
        adm.removeCallback(callback);
    }

    //连接到某个设备
    public void connect(String address) {
        adm.connect(address, new ConnectCallback() {
            @Override
            public void onConnectSucces() {
                if (isViewAttach()) {
                    adm.setConnected(true);
                    view.onConnectSuccess();
                }
            }

            @Override
            public void onConnectFail() {
                if (isViewAttach()) {
                    adm.disconnect();
                    adm.setConnected(false);
                    view.onConnectFail();
                }

            }
        });
    }

    //断开设备
    public void disconnect() {
        adm.disconnect();
    }

    //初始化Nfc适配器
    public void initNfcAdapter() {
        adm.init(new InitNfcCallback() {
            @Override
            public void onInitSuccess() {
                if (isViewAttach())
                    view.onInitNfcAdapterSuccess();
            }

            @Override
            public void onInitFail() {
                if (isViewAttach())
                    view.onInitNfcAdapterFail();
            }
        });
    }
}
