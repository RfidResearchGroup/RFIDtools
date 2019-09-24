package cn.rrg.rdv.activities.main;

import androidx.fragment.app.Fragment;

import cn.rrg.rdv.activities.standard.AbsStandardM1Activity;
import cn.rrg.rdv.fragment.tools.Proxmark3Rdv4RRGMFRWOperateFragment;
import cn.rrg.rdv.fragment.tools.Proxmark3Rdv4RRGShowInformationFragment;

public class Proxmark3Rdv4RRGReaderMain extends AbsStandardM1Activity {
    @Override
    protected Fragment getInformatinFragment() {
        return new Proxmark3Rdv4RRGShowInformationFragment();
    }

    @Override
    protected Fragment getTagOperateFragment() {
        return new Proxmark3Rdv4RRGMFRWOperateFragment();
    }
}
