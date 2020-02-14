package cn.rrg.mfkey;

import cn.rrg.console.define.ICommandTools;

public class NativeMfKey
        implements ICommandTools {

    private ICommandTools cmd;

    public NativeMfKey(boolean is64Mode) {
        //代理模式?，底层持有或者32或者64处理的实现类!
        if (is64Mode) cmd = new NativeMfKey64();
        else cmd = new NativeMfKey32();
    }

    @Override
    public int startExecute(String cmd) {
        return this.cmd.startExecute(cmd);
    }

    @Override
    public boolean isExecuting() {
        return this.cmd.isExecuting();
    }

    @Override
    public void stopExecute() {
        this.cmd.stopExecute();
    }
}
