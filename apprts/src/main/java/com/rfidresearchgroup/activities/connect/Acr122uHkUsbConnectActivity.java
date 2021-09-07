package com.rfidresearchgroup.activities.connect;

import com.rfidresearchgroup.models.AbstractDeviceModel;
import com.rfidresearchgroup.models.Acr122uUsbRawModel;
import com.rfidresearchgroup.rfidtools.R;

public class Acr122uHkUsbConnectActivity extends AbstractPN53XConnectActivity {

    @Override
    public AbstractDeviceModel[] getModels() {
        return new AbstractDeviceModel[]{
                new Acr122uUsbRawModel()
        };
    }

    @Override
    public String getConnectingMsg() {
        return getString(R.string.msg_connect_122u);
    }
}
