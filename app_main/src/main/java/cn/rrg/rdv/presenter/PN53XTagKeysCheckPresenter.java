package cn.rrg.rdv.presenter;

import cn.rrg.rdv.models.AbsTagKeysCheckModel;

public class PN53XTagKeysCheckPresenter
        extends TagKeysCheckPresenterImpl {
    @Override
    public AbsTagKeysCheckModel getTagKeysCheckModel() {

        return new PN53XTagKeysCheckModel(this);
    }
}
