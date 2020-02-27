package cn.rrg.rdv.presenter;

import cn.rrg.rdv.models.AbsTagWriteModel;
import cn.rrg.rdv.models.PN53XTagWriteModel;

public class PN53XTagWritePresenter extends AbsTagWritePresenter {
    @Override
    protected AbsTagWriteModel getWriteModel() {
        return new PN53XTagWriteModel();
    }
}
