package cn.rrg.rdv.activities.connect;

import cn.rrg.rdv.R;
import cn.rrg.rdv.activities.main.PN53XNfcMain;
import cn.rrg.rdv.activities.tools.DeviceConnectActivity;
import cn.rrg.rdv.callback.ConnectFailedCtxCallback;
import cn.rrg.rdv.models.AbstractDeviceModel;
import cn.rrg.rdv.models.Acr122uUsbRawModel;

public class Acr122uHkUsbConnectActivity extends DeviceConnectActivity {

    @Override
    public AbstractDeviceModel[] getModels() {
        return new AbstractDeviceModel[]{
                new Acr122uUsbRawModel()
        };
    }

    @Override
    public Class getTarget() {
        return PN53XNfcMain.class;
    }

    @Override
    public String getConnectingMsg() {
        return getString(R.string.msg_connect_122u);
    }

    @Override
    public ConnectFailedCtxCallback getCallback() {
        return this;
    }
}
