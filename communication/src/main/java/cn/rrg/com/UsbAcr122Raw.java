package cn.rrg.com;

public class UsbAcr122Raw extends AbsUsbBulkTransfer {
    //日志特征
    private static final String LOG_TAG = UsbAcr122Raw.class.getSimpleName();
    // single instance!
    private static UsbAcr122Raw mUsbRaw;
    //设备VP码
    private static final int[] VP_ID = new int[]{
            120566529, 120566532, 120554240, 120554242,
            120554247, 120554241, 120557568, 120557775,
            120557772, 120557784, 120566016, 120566017,
            120566018, 120566019, 120566020, 120566028,
            120565760, 120557778, 120528913, 120555776,
            120555777, 120555778, 120525317, 120525316,
            120525318, 120529408, 120529454, 120529463,
            120529457, 120529428, 120525440, 120529415,
            120529451, 120529414, 120529445, 120529411,
            120529434, 120529449, 120529432, 120529435,
            120529458, 120529474, 120529464, 120529487,
            120529467, 120529470, 120529476, 120529497,
            120529471, 120529465, 120529425, 120529490,
            120529152, 120529444, 120529423, 120529443,
            120529416, 120523009, 120529418, 120529429,
            120529440, 120529459, 120529460, 120529461,
            120529462, 120529427, 120529452, 120529496,
            120529482, 120553985, 120553990, 120557574,
            120557787, 120566272, 120566022
    };
    public static final String ACTION_BROADCAST = "com.rrg.devices.usb_attach_acr";
    public static final String DRIVER_ACR122U = "ACS-ACR_122U(龙杰-ACR_122U)";


    //私有构造方法，避免被直接调用
    private UsbAcr122Raw() {
        super();
        /*can't using default constructor*/
    }

    /*
     * 实例获取
     * */
    public static UsbAcr122Raw get() {
        synchronized (LOG_TAG) {
            if (mUsbRaw == null) {
                mUsbRaw = new UsbAcr122Raw();
            }
        }
        return mUsbRaw;
    }

    @Override
    public boolean isRawDevice(int producetId, int ventorId) {
        return isAcr122(producetId, ventorId);
    }

    @Override
    public String getDeviceDiscoveryAction() {
        return ACTION_BROADCAST;
    }

    @Override
    public String getDeviceNameOnFound() {
        return DRIVER_ACR122U;
    }

    @Override
    public String getDevice() {
        return DRIVER_ACR122U;
    }

    @Override
    public void disconect() {
        //TODO don't need
    }

    private static boolean isAcr122(int producetId, int ventorId) {
        boolean ret = false;
        int var3 = ventorId << 16 | producetId;
        int count = 0;
        while (true) {
            if (count >= 75) {
                break;
            }
            if (VP_ID[count] == var3) {
                ret = true;
                break;
            }
            ++count;
        }
        return ret;
    }
}
