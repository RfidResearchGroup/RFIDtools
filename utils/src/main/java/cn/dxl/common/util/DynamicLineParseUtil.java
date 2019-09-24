package cn.dxl.common.util;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

public class DynamicLineParseUtil extends Thread {

    private String LOG_TAG = this.getClass().getSimpleName();

    /*
     * 回调接口，当有完整的一行出现时回调!
     * */
    public interface OnNewLineLisenter {
        void onNewLine(String str);
    }

    //队列，生产消费!
    private Queue<Character> mConsoleQueue = new LinkedBlockingQueue<>();
    //回调接口!
    private OnNewLineLisenter mLisenter;
    //标志，是否暂停!
    private volatile boolean mPauseLabel = false;
    //标志，是否结束!
    private volatile boolean mCancelLabel = false;

    public DynamicLineParseUtil(OnNewLineLisenter lisenter) {
        mLisenter = lisenter;
    }

    @Override
    public void run() {
        //迭代队列
        StringBuilder sb = new StringBuilder(256);
        while (!mCancelLabel) {
            if (mPauseLabel) {
                //Log.d(LOG_TAG, "DynamicLineParseThread is pause!");
                continue;
            }
            if (mConsoleQueue.size() == 0) continue;
            if (mLisenter != null) {
                Character c;
                while ((c = mConsoleQueue.poll()) != null) {
                    //遇到的是换行符，则需要处理换行符!
                    if (c == '\n') {
                        //回调之前的结果!
                        String s = sb.toString();
                        //Log.d("****", "DLPU新行: " + s);
                        //开始回调!
                        mLisenter.onNewLine(s);
                        //清除缓存，重新建立对象!
                        sb = new StringBuilder(256);
                        //尝试通知GC
                        System.gc();
                        //直接跳过下一步的换行符添加!
                        break;
                    }
                    //否则一直追加进缓冲区
                    sb.append((char) c);
                    //Log.d("****", "DLPU: " + sb.toString());
                }
            }
        }
    }

    @Override
    public synchronized void start() {
        //处理启动相关的实现!
        mPauseLabel = false;
        if (!isAlive()) {
            super.start();
        }
    }

    public synchronized void appendText(Character txt) {
        mConsoleQueue.add(txt);
    }

    public synchronized void pause() {
        mPauseLabel = true;
    }

    public synchronized void cancel() {
        mCancelLabel = true;
    }
}
