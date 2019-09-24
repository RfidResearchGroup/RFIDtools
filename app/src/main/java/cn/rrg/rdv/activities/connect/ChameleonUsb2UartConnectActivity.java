package cn.rrg.rdv.activities.connect;

import cn.rrg.rdv.R;
import cn.rrg.rdv.activities.chameleon.ChameleonGUIActivity;
import cn.rrg.rdv.activities.tools.DeviceConnectActivity;
import cn.rrg.rdv.callback.ConnectFailedCtxCallback;
import cn.rrg.rdv.models.AbstractDeviceModel;
import cn.rrg.rdv.models.ChameleonUsb2UartModel;

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
