package cn.rrg.rdv.models;

import cn.dxl.mifare.MifareAdapter;
import cn.dxl.mifare.StdMifareImpl;

public class StandardTagReadModel
        extends AbsTagReadModel {
    @Override
    protected MifareAdapter getTag() {
        return StdMifareImpl.getInstance();
    }
}
