package cn.rrg.rdv.fragment.tools;

import cn.rrg.rdv.presenter.AbsTagInformationsPresenter;
import cn.rrg.rdv.presenter.PN53XTagInformationsPresenter;

public class PN53XShowInformationFragment extends AbsShowInformationFragment {
    @Override
    protected AbsTagInformationsPresenter getPresenter() {
        return new PN53XTagInformationsPresenter();
    }
}
