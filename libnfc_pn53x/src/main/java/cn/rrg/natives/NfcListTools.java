package cn.rrg.natives;

import cn.rrg.console.define.ICommandTools;

public class NfcListTools implements ICommandTools {

    static {
        System.loadLibrary("nfclist");
    }

    @Override
    public native int startExecute(String cmd);

    @Override
    public native boolean isExecuting();

    @Override
    public native void stopExecute();

}
