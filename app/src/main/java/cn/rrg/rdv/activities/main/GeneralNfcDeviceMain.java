package cn.rrg.rdv.activities.main;

import androidx.fragment.app.Fragment;

import cn.rrg.rdv.activities.standard.AbsStandardM1Activity;
import cn.rrg.rdv.fragment.tools.StandardMFRWOperateFragment;
import cn.rrg.rdv.fragment.tools.StandardShowInformationFragment;

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