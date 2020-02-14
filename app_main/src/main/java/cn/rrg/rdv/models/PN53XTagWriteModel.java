package cn.rrg.rdv.models;

import cn.dxl.mifare.MifareAdapter;
import cn.rrg.natives.SpclMf;

public class PN53XTagWriteModel extends AbsTagWriteModel {
    @Override
    protected MifareAdapter getTag() {
        return SpclMf.get();
    }
}
