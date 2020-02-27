package cn.rrg.rdv.presenter;

import cn.rrg.rdv.models.AbstractDeviceModel;
import cn.rrg.rdv.view.DeviceExistsView;

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
