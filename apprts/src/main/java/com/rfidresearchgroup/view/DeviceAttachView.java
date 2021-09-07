package com.rfidresearchgroup.view;

import com.rfidresearchgroup.javabean.DevBean;

public interface DeviceAttachView extends DeviceView {
    //显示搜寻到的设备的接口
    void devAttach(DevBean devBean);
}
