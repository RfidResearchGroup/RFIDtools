package cn.rrg.rdv.presenter;

import cn.rrg.rdv.models.AbsTagWriteModel;
import cn.rrg.rdv.models.Proxmark3Rdv4RRGTagWriteModel;

public class Proxmark3Rdv4RRGTagWritePresenter extends AbsTagWritePresenter {
    @Override
    protected AbsTagWriteModel getWriteModel() {
        return new Proxmark3Rdv4RRGTagWriteModel();
    }
}
