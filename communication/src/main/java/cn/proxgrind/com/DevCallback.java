package cn.proxgrind.com;

import java.io.Serializable;

public interface DevCallback<T> extends Serializable {
    //新设备发现回调
    void onAttach(T dev);

    //设备移除回调
    void onDetach(T dev);
}
