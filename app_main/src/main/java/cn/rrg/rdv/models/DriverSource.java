package cn.rrg.rdv.models;

import android.util.SparseArray;

import cn.rrg.com.DriverInterface;
import cn.rrg.com.SppHasBlock;
import cn.rrg.com.SppNonBlock;
import cn.rrg.rdv.driver.StandardDriver;
import cn.rrg.com.UniversalBulkTransfer;
import cn.rrg.com.UsbAcr122Raw;
import cn.rrg.com.UsbSerialControl;

/*
 * 驱动池，所有的驱动最终加载成功后都将被打进当前的实例进行缓存!!!
 * Datasource, all driver from here.
 * */
public class DriverSource {
    //一个驱动实体的映射表，当匹配到对应的驱动时将会把驱动打进或者取出!
    public static SparseArray<DriverInterface> driverMap = new SparseArray<>();

    static {
        /*
         * 放入SPP非堵塞不缓存实现驱动! code 0x01
         * 适用于不需要堵塞读取，不需要缓存发送帧的设备
         * 已知适用设备:
         * 1. PM3 RDV4 蓝牙模组
         * */
        driverMap.put(SppNonBlock.get().getUniqueId(), SppNonBlock.get());
        /*
         * 放入SPP堵塞缓存实现驱动! code 0x02
         * 适用于需要堵塞读取，在数据异常时需要发送缓存包重新请求的设备
         * 已知适用设备:
         * 1. PN532 + 蓝牙SPP
         * */
        driverMap.put(SppHasBlock.get().getUniqueId(), SppHasBlock.get());
        // 放入USB 122u原生字节实现, code 0x03
        driverMap.put(UsbAcr122Raw.get().getUniqueId(), UsbAcr122Raw.get());
        /*
         * 放入USB 转串口实现! code 0x04
         * 适用于市场上大部分USB转串口实现，该实现为ControlTransfer函数实现，速度较慢
         * 已知适用设备:
         * 1. PM3 RDV4 CDC
         * 2. PN532 Usb 2 Serial
         * 3. Chameleon CDC
         * */
        driverMap.put(UsbSerialControl.get().getUniqueId(), UsbSerialControl.get());
        // 放入标准NFC设备到驱动映射表，只适用标准NFC! code 0x05
        driverMap.put(StandardDriver.get().getUniqueId(), StandardDriver.get());
        // 通用USB RAW驱动!
        driverMap.put(UniversalBulkTransfer.getTransfer().getUniqueId(), UniversalBulkTransfer.getTransfer());
    }
}
