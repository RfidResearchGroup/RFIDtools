package cn.rrg.natives;

import cn.rrg.console.define.ICommandTools;

public class MfocTools implements ICommandTools {
    /*
     * 加载动态库
     * */
    static {
        System.loadLibrary("mfoc");
    }

    @Override
    public native int startExecute(String cmd);

    @Override
    public native boolean isExecuting();

    @Override
    public native void stopExecute();
}
