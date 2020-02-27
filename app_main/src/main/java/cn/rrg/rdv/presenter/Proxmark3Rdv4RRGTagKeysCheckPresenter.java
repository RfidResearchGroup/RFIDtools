package cn.rrg.rdv.presenter;

import cn.dxl.mifare.MifareAdapter;
import cn.rrg.natives.PM3Rdv4RRGMifare;
import cn.rrg.rdv.models.AbsTagKeysCheckModel;

public class Proxmark3Rdv4RRGTagKeysCheckPresenter extends TagKeysCheckPresenterImpl {

    @Override
    public AbsTagKeysCheckModel getTagKeysCheckModel() {
        return new AbsTagKeysCheckModel(this) {
            @Override
            public MifareAdapter getTag() {
                return PM3Rdv4RRGMifare.get();
            }
        };
    }
}
