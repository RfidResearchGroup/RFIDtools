package cn.rrg.rdv.models;

public abstract class AbsStopableTask {
    // 停止标志!
    protected boolean stopLable = false;

    public void stop() {
        stopLable = true;
    }

    public void reset() {
        stopLable = false;
    }
}
