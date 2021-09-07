package com.rfidresearchgroup.fragment.tools;

import com.rfidresearchgroup.presenter.AbsTagInformationsPresenter;
import com.rfidresearchgroup.presenter.StandardTagInformationsPresenter;

public class StandardShowInformationFragment extends AbsShowInformationFragment {
    @Override
    protected AbsTagInformationsPresenter getPresenter() {
        //使用的是标准的设备获得到的标签信息!
        return new StandardTagInformationsPresenter();
    }
}
