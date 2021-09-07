package com.rfidresearchgroup.presenter;

public class StandardMFMagicCheckPresenter
        extends AbsMFMagicCheckPresenter {
    @Override
    protected boolean checkMagic() {
        // 自带的标准NFC设备默认是没有此类魔术卡的操作实现的!
        return false;
    }
}
