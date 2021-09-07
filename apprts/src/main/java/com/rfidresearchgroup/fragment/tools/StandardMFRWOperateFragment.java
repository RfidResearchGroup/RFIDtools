package com.rfidresearchgroup.fragment.tools;

import com.rfidresearchgroup.presenter.AbsTagKeysCheckPresenter;
import com.rfidresearchgroup.presenter.AbsTagReadPresenter;
import com.rfidresearchgroup.presenter.AbsTagStatePresenter;
import com.rfidresearchgroup.presenter.AbsTagWritePresenter;
import com.rfidresearchgroup.presenter.StandardTagKeysCheckPresenter;
import com.rfidresearchgroup.presenter.StandardTagReadPresenter;
import com.rfidresearchgroup.presenter.StandardTagStatePresenter;
import com.rfidresearchgroup.presenter.StandardTagWritePresenter;

public class StandardMFRWOperateFragment
        extends AbsMfOperatesFragment {

    @Override
    protected AbsTagStatePresenter getTagStatePresenter() {
        return new StandardTagStatePresenter();
    }

    @Override
    protected AbsTagKeysCheckPresenter getKeysCheckPresenter() {
        return new StandardTagKeysCheckPresenter();
    }

    @Override
    protected AbsTagReadPresenter getTagReadPresenter() {

        return new StandardTagReadPresenter();
    }

    @Override
    protected AbsTagWritePresenter getTagWritePresenter() {
        return new StandardTagWritePresenter();
    }
}