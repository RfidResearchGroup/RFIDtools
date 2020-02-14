package cn.rrg.rdv.presenter;

import cn.dxl.mifare.MifareAdapter;
import cn.rrg.natives.SpclMf;
import cn.rrg.rdv.models.AbsTagKeysCheckModel;

public class PN53XTagKeysCheckModel extends AbsTagKeysCheckModel {
    public PN53XTagKeysCheckModel(KeyFilesCallback callback) {
        super(callback);
    }

    @Override
    public MifareAdapter getTag() {
        return SpclMf.get();
    }
}
