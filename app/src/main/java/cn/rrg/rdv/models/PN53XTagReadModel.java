package cn.rrg.rdv.models;

import cn.dxl.mifare.MifareAdapter;
import cn.rrg.natives.SpclMf;
import cn.rrg.rdv.callback.ReaderCallback;
import cn.rrg.rdv.javabean.M1Bean;

public class PN53XTagReadModel extends AbsTagReadModel {
    @Override
    protected MifareAdapter getTag() {
        return SpclMf.get();
    }
}
