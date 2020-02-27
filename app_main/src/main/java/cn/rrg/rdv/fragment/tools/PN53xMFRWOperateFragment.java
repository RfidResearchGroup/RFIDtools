package cn.rrg.rdv.fragment.tools;

import cn.rrg.rdv.presenter.AbsTagKeysCheckPresenter;
import cn.rrg.rdv.presenter.AbsTagReadPresenter;
import cn.rrg.rdv.presenter.AbsTagStatePresenter;
import cn.rrg.rdv.presenter.AbsTagWritePresenter;
import cn.rrg.rdv.presenter.PN53XTagKeysCheckPresenter;
import cn.rrg.rdv.presenter.PN53XTagReadPresenter;
import cn.rrg.rdv.presenter.PN53XTagStatePresenter;
import cn.rrg.rdv.presenter.PN53XTagWritePresenter;

public class PN53xMFRWOperateFragment extends AbsMfOperatesFragment {
    @Override
    protected AbsTagStatePresenter getTagStatePresenter() {
        return new PN53XTagStatePresenter();
    }

    @Override
    protected AbsTagKeysCheckPresenter getKeysCheckPresenter() {
        return new PN53XTagKeysCheckPresenter();
    }

    @Override
    protected AbsTagReadPresenter getTagReadPresenter() {
        return new PN53XTagReadPresenter();
    }

    @Override
    protected AbsTagWritePresenter getTagWritePresenter() {
        return new PN53XTagWritePresenter();
    }
}
