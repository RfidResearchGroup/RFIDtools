package cn.dxl.com;

import android.net.LocalServerSocket;
import android.net.LocalSocket;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Arrays;

/**
 * @author DXL
 * Created by DXL on 2017/8/21.
 * 通信控制映射类，直接实现进程间通信!
 */
public final class LocalComBridgeAdapter implements Serializable {
    /*
     * Warning!!!
     * If your implementation does not need to be mapped to the c/c++ (only Java exists), please do not use this tool class!
     *
     * 警告!!!
     * 如果您的实现不需要映射到底层（只存在Java），请不要使用此工具类!
     * */

    // The namespace of the LocalServerSocket
    public static final String NAMESPACE = "CN.DXL.ComBridgeAdapter.2020_0413";
    // The tag of the log.
    public static final String LOG_TAG = "ComBridgeAdapter";
    // 本地套接字服务!
    private LocalServerSocket serverSocket;
    // 单例!
    private static LocalComBridgeAdapter instance;
    // 输入流!
    private InputStream mInputStream;
    // 输出流!
    private OutputStream mOutputStream;
    // 是否已经连接!
    private volatile boolean isHasClient = false;
    // 暂停转发!
    private boolean pause = false;

    private LocalComBridgeAdapter() {
        // No instantiation is required
    }

    /**
     * Data forward thread!
     * data will from a InputStream to a OutputStream!
     */
    private class WorkThread extends Thread {
        @Override
        public void run() {
            while (true) {
                try {
                    LocalSocket socket = serverSocket.accept();
                    if (isHasClient) {
                        Log.e(LOG_TAG, "The server only once online supported.");
                        Log.e(LOG_TAG, "please disconnect your previous con.");
                        continue;
                    }
                    isHasClient = true;
                    new DataThread(socket, mOutputStream, socket.getInputStream()).start();
                    new DataThread(socket, socket.getOutputStream(), mInputStream).start();
                } catch (IOException e) {
                    e.printStackTrace();
                    isHasClient = false;
                    break;
                }
            }
        }
    }

    class DataThread extends Thread {
        private LocalSocket socket;
        private OutputStream os;
        private InputStream is;
        private byte[] buffer = new byte[1024];

        DataThread(LocalSocket socket, OutputStream os, InputStream is) {
            this.os = os;
            this.is = is;
            this.socket = socket;
        }

        @Override
        public void run() {
            while (isHasClient) {
                try {
                    if (pause) {
                        Log.e(LOG_TAG, "pausing！");
                        continue;
                    }
                    if (os != null && is != null) {
                        int len = is.read(buffer);
                        if (len != -1) {
                            os.write(Arrays.copyOf(buffer, len));
                            os.flush();
                        }
                    } else {
                        throw new IOException("IO closed.");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    isHasClient = false;
                    try {
                        socket.shutdownInput();
                        socket.shutdownOutput();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                    break;
                }
            }
            isHasClient = false;
        }
    }

    @Override
    protected void finalize() throws Throwable {
        stop();
    }

    public static LocalComBridgeAdapter getInstance() {
        return instance;
    }

    public InputStream getInputStream() {
        return mInputStream;
    }

    public LocalComBridgeAdapter setInputStream(InputStream mInputStream) {
        this.mInputStream = mInputStream;
        return this;
    }

    public OutputStream getOutputStream() {
        return mOutputStream;
    }

    public LocalComBridgeAdapter setOutputStream(OutputStream mOutputStream) {
        this.mOutputStream = mOutputStream;
        return this;
    }

    public void pause() {
        pause = true;
        Log.d(LOG_TAG, "ComBridgeAdapter pause!");
    }

    public LocalComBridgeAdapter start() {
        synchronized (LocalComBridgeAdapter.class) {
            try {
                if (serverSocket != null) stop();
                serverSocket = new LocalServerSocket(NAMESPACE);
                new WorkThread().start();
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(LOG_TAG, "If you see an error message like \"Address already in use\", check that you call the stop function");
                Log.e(LOG_TAG, "如果你看到了类似Address already in use的错误消息，请检查你是否调用停止函数");
            }
        }
        Log.d(LOG_TAG, "ComBridgeAdapter start!");
        return this;
    }

    public void stop() {
        isHasClient = false;
        try {
            if (serverSocket != null)
                serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.d(LOG_TAG, "ComBridgeAdapter stop!");
    }

    static {
        instance = new LocalComBridgeAdapter();
    }
}
