package cn.rrg.rdv.models;

import cn.dxl.mifare.MifareAdapter;
import cn.dxl.mifare.StdMifareImpl;

public class StandardTagKeysCheckModel extends AbsTagKeysCheckModel {

    public StandardTagKeysCheckModel(KeyFilesCallback callback) {
        super(callback);
    }

    @Override
    public MifareAdapter getTag() {
        return StdMifareImpl.getInstance();
    }
}
