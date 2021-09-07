package com.rfidresearchgroup.activities.connect;

import com.rfidresearchgroup.activities.main.PN53XNfcMain;
import com.rfidresearchgroup.activities.tools.DeviceConnectActivity;
import com.rfidresearchgroup.callback.ConnectFailedCtxCallback;
import com.rfidresearchgroup.rfidtools.R;

public abstract class AbstractPN53XConnectActivity extends DeviceConnectActivity {

    @Override
    public Class getTarget() {
        return PN53XNfcMain.class;
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
