package cn.rrg.rdv.activities.px53x;

import android.view.View;

import cn.rrg.console.define.ICommandTools;
import cn.rrg.console.define.ICommandType;
import cn.rrg.natives.NfcListTools;
import cn.rrg.rdv.R;
import cn.rrg.rdv.implement.EntryICommandType;

public class NfcListConsoleActivity extends PN53XConsoleActivity {
    @Override
    protected View getCommandGUI() {
        return null;
    }

    @Override
    protected ICommandTools initCMD() {
        return new NfcListTools();
    }

    @Override
    protected ICommandType initType() {
        return new EntryICommandType();
    }

    @Override
    protected int startTest(ICommandTools cmd) {
        mDefaultCMD = "nfclist -v";
        return super.startTest(cmd);
    }

    @Override
    protected void onTestEnd() {
        showToast(getString(R.string.finish));
    }
}
