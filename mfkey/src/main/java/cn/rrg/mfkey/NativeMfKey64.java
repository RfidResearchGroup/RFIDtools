package cn.rrg.mfkey;

import cn.rrg.console.define.ICommandTools;

public class NativeMfKey64 implements ICommandTools {
    static {
        System.loadLibrary("mfkey64");
    }

    @Override
    public native int startExecute(String cmd);

    @Override
    public native boolean isExecuting();

    @Override
    public native void stopExecute();
}
