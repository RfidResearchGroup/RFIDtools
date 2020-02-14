package cn.rrg.rdv.activities.main;

import androidx.fragment.app.Fragment;

import cn.rrg.rdv.activities.standard.AbsStandardM1Activity;
import cn.rrg.rdv.fragment.tools.PN53XShowInformationFragment;
import cn.rrg.rdv.fragment.tools.PN53xMFRWOperateFragment;

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
