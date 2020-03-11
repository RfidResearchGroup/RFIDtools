package cn.rrg.rdv.presenter;

import cn.rrg.rdv.models.AbsTagReadModel;
import cn.rrg.rdv.models.PN53XTagReadModel;

public class PN53XTagReadPresenter extends AbsTagReadPresenter {
    @Override
    protected AbsTagReadModel getTagReadModel() {
        return new PN53XTagReadModel();
    }
}
