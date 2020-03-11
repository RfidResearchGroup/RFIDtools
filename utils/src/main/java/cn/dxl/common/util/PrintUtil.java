package cn.dxl.common.util;

import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/*
 * 用于监听文件更新并且回调各项操作!
 * */
public class PrintUtil {

    private static String LOG_TAG = PrintUtil.class.getSimpleName();
    private OnPrintLisenter mPrintLisenter = null;
    private volatile boolean isWorking = false;
    private volatile boolean isPause = false;
    private FileInputStream fis = null;
    private File mTarget;

    public PrintUtil(File target) {
        mTarget = target;
    }

    /*
     * 接口，监听到文件新行会回调!
     * */
    public interface OnPrintLisenter {
        void onPrint(String out);
    }

    /*
     * 监听标准输出的线程!
     * */
    private class PrintThread extends Thread {

        @Override
        public void run() {
            if (mTarget == null) return;
            Log.d(LOG_TAG, "控制台打印线程被执行!");
            try {
                fis = new FileInputStream(mTarget);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            while (isWorking) {
                //暂停，也就是跳过!
                if (isPause) continue;
                //开始执行监听!
                if (fis != null) {
                    //Log.d(LOG_TAG, "控制台打印线程正在运行!");
                    try {
                        //处理行数据，进行回调!
                        int byteCount = fis.available();
                        if (byteCount > 0) {
                            byte[] bytes = new byte[byteCount];
                            int len = fis.read(bytes);
                            if (len != byteCount) Log.d(LOG_TAG, "接收到的字节长度和欲接收的字节长度不一样!");
                            String str = new String(bytes, "UTF-8");
                            mPrintLisenter.onPrint(str);
                            //Log.d(LOG_TAG, "控制台打印线程: " + str);
                        }
                    } catch (IOException ioe) {
                        ioe.printStackTrace();
                    }
                }
            }
            isWorking = false;
            //在停止线程后应当释放资源!
            try {
                if (fis != null)
                    fis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Log.d(LOG_TAG, "控制台打印线程被结束!");
        }
    }

    public void setPrintLisenter(OnPrintLisenter lisenter) {
        this.mPrintLisenter = lisenter;
    }

    //开始打印线程，从文件种读取被重定向到文件的标准输出!
    public void start() {
        if (mPrintLisenter != null) {
            //检查标准输出重定向的文件!
            if (!mTarget.exists() || !mTarget.isFile()) {
                mPrintLisenter.onPrint("中转文件不存在或非文件，请检查文件读写权限!\n");
                mPrintLisenter.onPrint("标准输出重定向执行失败，无法获得运行时消息!\n");
                mPrintLisenter.onPrint("尝试打印重定向文件信息：" + mTarget.getAbsolutePath() + "\n");
                return;
            }
            //不在运行状态则开始新的线程!
            if (!isWorking) {
                //标志位改变，开始执行监听线程!
                isWorking = true;
                new PrintThread().start();
            } else {
                //当前已有在执行的线程!
                Log.d(LOG_TAG, "当前已有在执行的控制台转发监听线程！");
                if (isPause) {
                    Log.d(LOG_TAG, "当前已在执行的控制台转发监听线程被暂停，将会重新启动！");
                    isPause = false;
                }
            }
        } else {
            Log.w(LOG_TAG, "mPrintLisenter是空引用，请检查逻辑!");
        }
    }

    //停止打印线程!
    public void stop() {
        isWorking = false;
    }

    //暂停打印线程！
    public void pause() {
        isPause = true;
    }
}
