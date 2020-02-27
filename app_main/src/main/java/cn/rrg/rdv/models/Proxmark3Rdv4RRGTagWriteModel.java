package cn.rrg.rdv.models;

import cn.dxl.mifare.MifareAdapter;
import cn.rrg.natives.PM3Rdv4RRGMifare;

public class Proxmark3Rdv4RRGTagWriteModel extends AbsTagWriteModel {
    @Override
    protected MifareAdapter getTag() {
        return PM3Rdv4RRGMifare.get();
    }
}
