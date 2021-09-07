package com.rfidresearchgroup.presenter;

import com.rfidresearchgroup.models.AbstractDeviceModel;
import com.rfidresearchgroup.view.DeviceExistsView;

public class DeviceExistsPresenter
        extends DevicePresenter<DeviceExistsView> {

    public DeviceExistsPresenter(AbstractDeviceModel model) {
        super(model);
    }

    //刷新已配对的设备List
    public void existsDevList() {
        if (isViewAttach()) {
            subView.showExistsDev(adm.getHistory());
        }
    }
}
