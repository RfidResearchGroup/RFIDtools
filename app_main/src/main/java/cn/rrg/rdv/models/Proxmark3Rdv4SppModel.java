package cn.rrg.rdv.models;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

import com.iobridges.com.DeviceChecker;

import cn.proxgrind.com.DriverInterface;
import cn.proxgrind.com.SppNonBlock;
import cn.rrg.devices.Proxmark3RRGRdv4;

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
        //此驱动对应的RDV4的客户端，因此需要返回的是RDV4的初始化测试实现!
        return new Proxmark3RRGRdv4(mDI);
    }
}
