package com.rfidresearchgroup.activities.connect;

import com.rfidresearchgroup.activities.tools.DeviceConnectActivity;
import com.rfidresearchgroup.callback.ConnectFailedCtxCallback;
import com.rfidresearchgroup.models.AbstractDeviceModel;
import com.rfidresearchgroup.models.ChameleonUsb2UartModel;
import com.rfidresearchgroup.activities.chameleon.ChameleonGUIActivity;
import com.rfidresearchgroup.rfidtools.R;

public class ChameleonUsb2UartConnectActivity extends DeviceConnectActivity {
    @Override
    public AbstractDeviceModel[] getModels() {
        return new AbstractDeviceModel[]{
                new ChameleonUsb2UartModel()
        };
    }

    @Override
    public Class getTarget() {
        return ChameleonGUIActivity.class;
    }

    @Override
    public String getConnectingMsg() {
        return getString(R.string.msg_connect_common);
    }

    @Override
    public ConnectFailedCtxCallback getCallback() {
        return this;
    }
}
