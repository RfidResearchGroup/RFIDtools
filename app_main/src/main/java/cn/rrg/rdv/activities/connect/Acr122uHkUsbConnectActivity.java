package cn.rrg.rdv.activities.connect;

import cn.rrg.rdv.R;
import cn.rrg.rdv.models.AbstractDeviceModel;
import cn.rrg.rdv.models.Acr122uUsbRawModel;

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
