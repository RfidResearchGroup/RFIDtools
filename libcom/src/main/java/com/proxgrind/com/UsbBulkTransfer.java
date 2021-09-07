package com.proxgrind.com;

/**
 * 通用USB Bulk传输实现!
 *
 * @author dxl
 * @version 1.0
 */
public class UsbBulkTransfer extends AbsUsbBulkTransfer {

    private static UsbBulkTransfer transfer = new UsbBulkTransfer();

    public static UsbBulkTransfer getTransfer() {
        return transfer;
    }

    @Override
    public boolean isRawDevice(int producetId, int ventorId) {
        return true;
    }

    @Override
    public String getDeviceDiscoveryAction() {
        return "cn.rrg.com.UniversalBulkTransfer";
    }

    @Override
    public String getDeviceNameOnFound() {
        return "PN53X";
    }
}
