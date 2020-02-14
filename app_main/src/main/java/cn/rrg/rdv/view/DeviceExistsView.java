package cn.rrg.rdv.view;

import cn.rrg.rdv.javabean.DevBean;

public interface DeviceExistsView
        extends DeviceView {
    //显示已配对的设备的接口
    void showExistsDev(DevBean[] devList);
}
