package cn.rrg.rdv.activities.connect;

import cn.rrg.rdv.R;
import cn.rrg.rdv.activities.main.PN53XNfcMain;
import cn.rrg.rdv.activities.tools.DeviceConnectActivity;
import cn.rrg.rdv.callback.ConnectFailedCtxCallback;

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
