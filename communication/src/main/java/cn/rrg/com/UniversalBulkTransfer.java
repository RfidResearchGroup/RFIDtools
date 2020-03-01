package cn.rrg.com;

/**
 * 通用USB Bulk传输实现!
 *
 * @author dxl
 * @version 1.0
 */
public class UniversalBulkTransfer extends UsbBulkTransferRaw {
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

    @Override
    public int getUniqueId() {
        return 0x06;
    }
}
