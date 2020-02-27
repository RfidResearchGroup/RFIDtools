package cn.rrg.rdv.activities.tools;

import cn.rrg.console.define.ICommandTools;
import cn.rrg.mfkey.NativeMfKey;

public class MfKey32ConsoleActivity extends CommonConsoleActivity {
    @Override
    protected ICommandTools initCMD() {
        return new NativeMfKey(false);
    }
}
