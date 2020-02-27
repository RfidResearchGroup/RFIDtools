package cn.rrg.rdv.presenter;

import cn.rrg.rdv.models.AbsTagStateModel;
import cn.rrg.rdv.models.PN53XTagStateModel;

public class PN53XTagStatePresenter extends AbsTagStatePresenter {

    @Override
    protected AbsTagStateModel getModel() {
        return new PN53XTagStateModel();
    }
}
