package com.rfidresearchgroup.models;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

import com.proxgrind.com.DeviceChecker;

import com.proxgrind.com.DriverInterface;
import com.proxgrind.com.SppHasBlock;
import com.rfidresearchgroup.devices.PN53X;

/*
 * PN53X连接实现!
 * */
public class PN532SppUartModel extends AbstractSppDeviceModel {
    @Override
    public DriverInterface<BluetoothDevice, BluetoothAdapter> getDriverInterface() {
        return SppHasBlock.get();
    }

    @Override
    public DeviceChecker getDeviceInitImpl() {
        return new PN53X(PN53X.NAME.PN532, mDI);
    }
}
