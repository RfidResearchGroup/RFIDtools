package cn.rrg.rdv.activities.connect;

import android.Manifest;
import android.os.Bundle;

import androidx.annotation.Nullable;

import cn.dxl.common.util.PermissionUtil;
import cn.rrg.rdv.R;
import cn.rrg.rdv.activities.main.PN53XNfcMain;
import cn.rrg.rdv.activities.tools.DeviceConnectActivity;
import cn.rrg.rdv.callback.ConnectFailedCtxCallback;
import cn.rrg.rdv.models.AbstractDeviceModel;
import cn.rrg.rdv.models.PN532Usb2UartModel;
import cn.rrg.rdv.models.PN532SppUartModel;

public class PN532UartConnectActivity extends DeviceConnectActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //这里我们申请一下权限，因为RDV4需要蓝牙!
        PermissionUtil pu = new PermissionUtil(this);
        pu.setRequestCode(0x666);
        pu.request(Manifest.permission.ACCESS_COARSE_LOCATION);
    }

    @Override
    public AbstractDeviceModel[] getModels() {
        return new AbstractDeviceModel[]{
                // PN532支持USB转串口链接
                new PN532Usb2UartModel(),
                // 并且还支持蓝牙SPP链接
                new PN532SppUartModel()
        };
    }

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