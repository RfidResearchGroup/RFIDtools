package cn.rrg.natives;

import cn.rrg.console.define.ICommandTools;

public class Proxmark3Tools implements ICommandTools {
    /*
     * ***************
     * 封装PM3的常用功能!
     * ***************
     * */
    static {
        System.loadLibrary("pm3");
    }

    @Override
    public native int startExecute(String cmd);

    @Override
    public native boolean isExecuting();

    @Override
    public native void stopExecute();
}
