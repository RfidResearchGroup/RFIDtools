package cn.rrg.natives;

import cn.rrg.console.define.ICommandTools;

public class Proxmark3RRGRdv4Tools implements ICommandTools {

    //加载so文件!
    static {
        System.loadLibrary("pm3rrg_rdv4");
    }

    @Override
    public native int startExecute(String cmd);

    @Override
    public native boolean isExecuting();

    @Override
    public native void stopExecute();
}
