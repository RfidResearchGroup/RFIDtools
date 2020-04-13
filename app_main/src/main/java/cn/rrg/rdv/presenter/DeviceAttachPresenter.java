package cn.rrg.rdv.presenter;

import android.content.Context;

import cn.rrg.com.DevCallback;
import cn.rrg.rdv.javabean.DevBean;
import cn.rrg.rdv.models.AbstractDeviceModel;
import cn.rrg.rdv.view.DeviceAttachView;

public class DeviceAttachPresenter
        extends DevicePresenter<DeviceAttachView> {

    public DeviceAttachPresenter(AbstractDeviceModel model) {
        super(model);
    }

    //注册设备广播
    public void register(int[] pers) {
        //先设置回调!
        adm.addCallback(new DevCallback<DevBean>() {
            @Override
            public void onAttach(DevBean dev) {
                if (isSubViewAttach())
                    subView.devAttach(dev);
            }

            @Override
            public void onDetach(DevBean dev) {
                if (isViewAttach())
                    view.devDetach(dev);
            }
        });
        //再尝试注册实际的驱动!
        adm.register();
    }
}
