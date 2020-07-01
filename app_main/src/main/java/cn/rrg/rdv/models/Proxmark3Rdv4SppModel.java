package cn.rrg.rdv.models;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

import com.iobridges.com.DeviceChecker;

import java.io.IOException;

import cn.proxgrind.com.DriverInterface;
import cn.proxgrind.com.SppNonBlock;

/*
 * PM3 RDV4 SPP连接实现!
 * */
public class Proxmark3Rdv4SppModel extends AbstractSppDeviceModel {

    @Override
    public DriverInterface<BluetoothDevice, BluetoothAdapter> getDriverInterface() {
        return SppNonBlock.get();
    }

    @Override
    public DeviceChecker getDeviceInitImpl() {
        return new DeviceChecker(mDI) {
            @Override
            protected boolean checkDevice() throws IOException {
                return true;
            }

            @Override
            public void close() throws IOException {

            }
        };
    }
}
