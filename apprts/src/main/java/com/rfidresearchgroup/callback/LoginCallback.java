package com.rfidresearchgroup.callback;

public interface LoginCallback{
    //在完成资源注册加载后返回权限码，后期根据权限码注册驱动
    void onFinish();

    //在失败加载后返回类型，后期根据类型做处理!
    void onFail();
}
