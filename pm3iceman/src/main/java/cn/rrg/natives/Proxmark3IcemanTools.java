package cn.rrg.natives;

import cn.rrg.console.define.ICommandTools;

public class Proxmark3IcemanTools implements ICommandTools {

    static {
        System.loadLibrary("pm3iceman");
    }

    @Override
    public native int startExecute(String cmd);

    @Override
    public native boolean isExecuting();

    @Override
    public native void stopExecute();
}
