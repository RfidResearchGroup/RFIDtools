package com.rfidresearchgroup.fragment.tools;

import com.rfidresearchgroup.presenter.AbsTagKeysCheckPresenter;
import com.rfidresearchgroup.presenter.AbsTagReadPresenter;
import com.rfidresearchgroup.presenter.AbsTagStatePresenter;
import com.rfidresearchgroup.presenter.AbsTagWritePresenter;
import com.rfidresearchgroup.presenter.PN53XTagKeysCheckPresenter;
import com.rfidresearchgroup.presenter.PN53XTagReadPresenter;
import com.rfidresearchgroup.presenter.PN53XTagStatePresenter;
import com.rfidresearchgroup.presenter.PN53XTagWritePresenter;

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
