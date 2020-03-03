package cn.rrg.rdv.models;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

import cn.rrg.com.DriverInterface;
import cn.rrg.com.Device;
import cn.rrg.devices.PN53X;

/*
 * PN53X连接实现!
 * */
public class PN532SppUartModel extends AbstractSppDeviceModel {
    @Override
    public DriverInterface<BluetoothDevice, BluetoothAdapter> getDriverInterface() {
        return DriverSource.driverMap.get(0x02);
    }

    @Override
    public Device getDeviceInitImpl() {
        return new PN53X(PN53X.NAME.PN532);
    }
}
