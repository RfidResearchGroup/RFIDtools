package com.rfidresearchgroup.view;

import com.rfidresearchgroup.javabean.DevBean;

public interface DeviceExistsView
        extends DeviceView {
    //显示已配对的设备的接口
    void showExistsDev(DevBean[] devList);
}
