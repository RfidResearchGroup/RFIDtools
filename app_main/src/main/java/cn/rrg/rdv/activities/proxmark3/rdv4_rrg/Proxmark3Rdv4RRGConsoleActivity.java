package cn.rrg.rdv.activities.proxmark3.rdv4_rrg;

import cn.rrg.console.define.ICommandTools;
import cn.rrg.natives.Proxmark3RRGRdv4Tools;
import cn.rrg.rdv.activities.proxmark3.official.Proxmark3ConsoleActivity;

public class Proxmark3Rdv4RRGConsoleActivity
        extends Proxmark3ConsoleActivity {

    @Override
    protected ICommandTools initCMD() {
        //替换为rdv4的工具类!
        return new Proxmark3RRGRdv4Tools();
    }
}
