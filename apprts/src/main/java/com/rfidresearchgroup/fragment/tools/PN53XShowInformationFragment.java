package com.rfidresearchgroup.fragment.tools;

import com.rfidresearchgroup.presenter.AbsTagInformationsPresenter;
import com.rfidresearchgroup.presenter.PN53XTagInformationsPresenter;

public class PN53XShowInformationFragment extends AbsShowInformationFragment {
    @Override
    protected AbsTagInformationsPresenter getPresenter() {
        return new PN53XTagInformationsPresenter();
    }
}
