package cn.rrg.rdv.view;

import cn.rrg.rdv.javabean.DevBean;

public interface DeviceAttachView extends DeviceView {
    //显示搜寻到的设备的接口
    void devAttach(DevBean devBean);
}
