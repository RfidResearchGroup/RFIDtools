package com.rfidresearchgroup.activities.main;

import androidx.fragment.app.Fragment;

import com.rfidresearchgroup.fragment.tools.PN53XShowInformationFragment;
import com.rfidresearchgroup.fragment.tools.PN53xMFRWOperateFragment;
import com.rfidresearchgroup.activities.standard.AbsStandardM1Activity;

public class PN53XReaderMain extends AbsStandardM1Activity {

    @Override
    protected Fragment getInformatinFragment() {
        return new PN53XShowInformationFragment();
    }

    @Override
    protected Fragment getTagOperateFragment() {
        return new PN53xMFRWOperateFragment();
    }
}
