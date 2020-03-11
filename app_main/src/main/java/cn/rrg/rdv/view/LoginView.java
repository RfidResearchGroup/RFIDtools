package cn.rrg.rdv.view;

import cn.dxl.common.util.PrintUtil;

public interface LoginView extends BaseMvpView, PrintUtil.OnPrintLisenter {
    void onInitFinish();

    void onInitFail();
}
