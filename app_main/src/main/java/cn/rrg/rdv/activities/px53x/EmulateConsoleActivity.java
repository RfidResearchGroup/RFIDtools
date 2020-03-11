package cn.rrg.rdv.activities.px53x;

import android.view.View;

import cn.rrg.natives.EmulateTools;
import cn.rrg.console.define.ICommandTools;
import cn.rrg.console.define.ICommandType;

public class EmulateConsoleActivity extends PN53XConsoleActivity {

    @Override
    protected View getCommandGUI() {
        return null;
    }

    @Override
    protected ICommandTools initCMD() {
        return new EmulateTools();
    }

    @Override
    protected ICommandType initType() {
        return null;
    }

    @Override
    protected int startTest(ICommandTools cmd) {
        mDefaultCMD = "emulate -h";
        return super.startTest(cmd);
    }

    @Override
    protected void onTestEnd() {
        showToast("Emulate停止执行!");
    }
}
