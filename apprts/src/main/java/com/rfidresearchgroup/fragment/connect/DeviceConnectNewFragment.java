package com.rfidresearchgroup.fragment.connect;

import com.rfidresearchgroup.models.AbstractDeviceModel;

import java.util.ArrayList;
import java.util.List;

import com.proxgrind.com.DevCallback;
import com.rfidresearchgroup.javabean.DevBean;
import com.rfidresearchgroup.presenter.DeviceAttachPresenter;

public class DeviceConnectNewFragment
        extends DeviceConnectFragment
        implements DevCallback<DevBean> {

    //持有中介者!
    private List<DeviceAttachPresenter> presenters = new ArrayList<>();

    @Override
    public void onDestroy() {
        super.onDestroy();
        for (DeviceAttachPresenter presenter : presenters) {
            //销毁视图绑定!
            presenter.detachSubView();
            presenter.detachView();
            //移除回调注册!
            presenter.removeCallback(this);
        }
    }

    @Override
    protected void initResource() {
        super.initResource();
        if (models != null) {
            for (AbstractDeviceModel model : models) {
                DeviceAttachPresenter presenter = new DeviceAttachPresenter(model);
                presenters.add(presenter);
                //初始化中介者并且绑定视图!
                presenter.attachSubView(this);
                presenter.attachView(this);
                //设置回调，绑定视图!
                presenter.addCallback(this);
                presenter.discovery(getContext());
            }
            //判断数据是否为空，显示相关的视图!
            showOrDismissEmptyView();
        } else {
            throw new RuntimeException("models no init exception!");
        }
    }


    @Override
    protected void onDiscovery() {
        for (DeviceAttachPresenter presenter : presenters)
            presenter.discovery(getContext());
    }

    @Override
    public void onError(String e) {
        showToast(e);
    }

    @Override
    public void onAttach(DevBean dev) {
        //TODO 实际上我们只需要调用父类的方法进行处理!
        devAttach(dev);
    }

    @Override
    public void onDetach(DevBean dev) {
        //同上!
        devDetach(dev);
    }
}
