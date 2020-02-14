package cn.rrg.rdv.models;

import cn.dxl.mifare.MifareAdapter;
import cn.dxl.mifare.StdMifareImpl;

public class StandardTagWriteModel extends AbsTagWriteModel {
    @Override
    protected MifareAdapter getTag() {
        return new StdMifareImpl();
    }
}
