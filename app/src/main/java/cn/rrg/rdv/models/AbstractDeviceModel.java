package cn.rrg.rdv.models;

import android.content.Context;
import android.util.Log;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import cn.rrg.com.DevCallback;
import cn.rrg.com.DriverInterface;

import cn.dxl.com.Com;
import cn.rrg.com.Device;
import cn.rrg.rdv.javabean.DevBean;
import cn.rrg.rdv.callback.ConnectCallback;
import cn.rrg.rdv.callback.InitNfcCallback;
import cn.rrg.rdv.util.Commons;

/**
 * 抽象的设备数据层对象，定义了一系列设备操作的字段与动作!
 *
 * @author DXL
 */
public abstract class AbstractDeviceModel<D, A>
        implements Serializable {

    protected final String TAG = this.getClass().getSimpleName();
    //设备初始化类
    protected Device mDevice;
    //驱动的实现,此实例必须被初始化!
    protected DriverInterface<D, A> mDI;

    //是否是连接成功的!
    private boolean isConnected = false;
    //设备广播回调实现，此变量必须被初始设置!
    protected DevCallback<D> devCallbackImpl;
    //缓存当前的发现的设备列表，应当所有的底层都共享这个列表!
    protected static final List<DevBean> devAttachList = new ArrayList<>();
    //缓存一个设备回调!
    protected static final List<DevCallback<DevBean>> callbacks = new ArrayList<>();

    //在构造函数中初始化设备驱动等等资源!
    protected AbstractDeviceModel() {
        mDI = getDriverInterface();
        mDevice = getDeviceInitImpl();
        devCallbackImpl = getDevCallback();
    }

    //分发到注册的回调中!
    public void attachDispatcher(DevBean devBean) {
        if (devBean != null) {
            for (DevCallback<DevBean> callback : callbacks) {
                if (callback != null) {
                    callback.onAttach(devBean);
                }
            }
        }
    }

    public void detachDispatcher(DevBean devBean) {
        if (devBean != null) {
            for (DevCallback<DevBean> callback : callbacks) {
                if (callback != null) {
                    callback.onDetach(devBean);
                }
            }
        }
    }

    //添加到列表中!
    public void addDev2List(DevBean devBean) {
        boolean isExists = false;
        for (DevBean devs : devAttachList) {
            if (Commons.isDevBeanDataNotNull(devBean)
                    && Commons.isDevBeanDataNotNull(devs)) {
                if (Commons.equalDebBean(devBean, devs)) {
                    //已经存在在列表中
                    isExists = true;
                }
            }
        }
        if (!isExists) {
            devAttachList.add(devBean);
        }
    }

    //设置当需要被设备更新时的通知!
    public void addCallback(DevCallback<DevBean> c) {
        if (c != null) {
            //如果回调不为空，我们就进行回调处理!
            callbacks.add(c);
            //设置回调的时候可能需要将历史缓存的数据进行更新!
            for (DevBean devBean : devAttachList) {
                //可能在没有设置视图的时候静默状态下也有设备插入了！
                c.onAttach(devBean);
            }
        }
    }

    //移除回调!
    public void removeCallback(DevCallback<DevBean> c) {
        //从回调列表中移除该回调!
        callbacks.remove(c);
    }

    //注册需要的广播事件
    public void register(Context context) {
        if (mDI != null) {
            //直接注册!
            mDI.register(context, devCallbackImpl);
        } else {
            Log.d(TAG, "驱动为空，可能是因为没有加载该驱动!");
        }
    }

    //解注册广播事件!
    public void unregister(Context context) {
        //直接解注册!
        if (mDI != null)
            mDI.unregister(context);
    }

    // 设置当前驱动的链接状态!
    public void setConnected(boolean connected) {
        isConnected = connected;
    }

    // 得到当前的链接状态!
    public boolean isConnected() {
        return isConnected;
    }

    //必须初始化的设备驱动!
    public abstract DriverInterface<D, A> getDriverInterface();

    //必须初始化的设备初始化类!
    public abstract Device getDeviceInitImpl();

    //必须初始化的设备扫描相关的实现!
    public abstract DevCallback<D> getDevCallback();

    //寻找设备，由于每个设备的寻找实现都不同，此处直接定义为抽象即可!
    public abstract void discovery(Context context);

    //获得已存在的设备列表，同上!
    public abstract DevBean[] getHistory();

    //连接一个设备,不再繁杂的判断，直接使用子类的实现!
    public abstract void connect(String address, ConnectCallback callback);

    //断开与设备的连接，由于某些设备的停止不能单停止驱动层，因此这里也作为抽象动作!
    public abstract void disconnect();

    //在连接设备完成后初始化NFC设备!
    public void init(InitNfcCallback callback) {
        Log.d(TAG, "调用了初始化!");
        //TODO 最终是在这里实现的NFC设备的通信初始化和设备唤醒!
        if (isConnected) {
            Log.d(TAG, "该驱动已经链接初始化成功，将使用该驱动进行初始化设备: " + mDI.getClass().getName());
            new Thread(new Runnable() {
                @Override
                public void run() {
                    //直接连接，有需要时可重写!
                    if (Com.initCom(mDI, mDevice)) {
                        callback.onInitSuccess();
                    } else {
                        callback.onInitFail();
                    }
                }
            }).start();
        } else {
            Log.d(TAG, "该驱动未链接，将禁用该驱动在接下来的初始化设备任务: " + mDI.getClass().getName());
        }
    }
}
