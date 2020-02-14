package cn.rrg.natives;

import cn.rrg.console.define.ICommandTools;

public class EmulateTools implements ICommandTools {

    static {
        System.loadLibrary("emulate");
    }
 
    @Override
    public native int startExecute(String cmd);

    @Override
    public native boolean isExecuting();

    @Override
    public native void stopExecute();
}
