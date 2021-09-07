package com.rfidresearchgroup.activities.connect;

import android.Manifest;
import android.os.Bundle;

import androidx.annotation.Nullable;

import com.rfidresearchgroup.models.AbstractDeviceModel;
import com.rfidresearchgroup.models.PN532SppUartModel;
import com.rfidresearchgroup.models.PN532Usb2UartModel;

import com.rfidresearchgroup.common.util.PermissionUtil;

public class PN532UartConnectActivity extends AbstractPN53XConnectActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
}