package com.rfidresearchgroup.activities.main;

import androidx.fragment.app.Fragment;

import com.rfidresearchgroup.fragment.tools.StandardMFRWOperateFragment;
import com.rfidresearchgroup.fragment.tools.StandardShowInformationFragment;
import com.rfidresearchgroup.activities.standard.AbsStandardM1Activity;

public class GeneralNfcDeviceMain extends AbsStandardM1Activity {
    @Override
    protected Fragment getInformatinFragment() {
        return new StandardShowInformationFragment();
    }

    @Override
    protected Fragment getTagOperateFragment() {
        return new StandardMFRWOperateFragment();
    }
}