package cn.proxgrind.com;

import com.iobridges.com.Communication;

/*
 * 驱动程序类
 * 泛型1 -> 设备实体类
 * 泛型2 -> 适配器类
 */
public interface DriverInterface<D, A> extends Communication {
    //注册广播之类的事件
    void register(DevCallback<D> callback);

    //链接到设备
    boolean connect(D t);

    //得到当前的驱动程序适配器类
    A getAdapter();

    //得到当前的驱动程序的设备类
    D getDevice();

    //断开与设备的链接（在某些设备上不一定是立刻生效的）
    void disconect();

    //解注册广播之类的
    void unregister();
}