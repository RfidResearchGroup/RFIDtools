package cn.rrg.rdv.activities.proxmark3.iceman;

import cn.rrg.console.define.ICommandTools;
import cn.rrg.natives.Proxmark3IcemanTools;
import cn.rrg.rdv.activities.proxmark3.official.Proxmark3ConsoleActivity;

/*
 * PM3冰人客户端的基础控制台!
 * */
public class Proxmark3IcemanConsoleActivity
        extends Proxmark3ConsoleActivity {

    @Override
    protected ICommandTools initCMD() {
        //替换为冰人的控制台工具对象!
        return new Proxmark3IcemanTools();
    }

}
