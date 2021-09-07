package com.rfidresearchgroup.view;

public interface TagInformationsView<T>
        extends BaseMvpView {
    //显示信息是以什么格式传入到上层的!
    void onInformationsShow(T t);
}