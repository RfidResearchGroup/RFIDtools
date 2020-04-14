package cn.dxl.com;

import android.net.LocalServerSocket;
import android.net.LocalSocket;
import android.util.Log;

import com.felhr.utils.HexData;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;

/**
 * @author DXL
 * Created by DXL on 2017/8/21.
 * 通信控制映射类，直接实现进程间通信!
 */
public final class ComBridgeAdapter implements Serializable {
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
    private static ComBridgeAdapter instance;
    // 输入流!
    private InputStream mInputStream;
    // 输出流!
    private OutputStream mOutputStream;
    // 是否已经连接!
    private volatile boolean isHasClient = false;
    // 暂停转发!
    private boolean pause = false;

    // No instantiation is required
    private ComBridgeAdapter() {
        synchronized (ComBridgeAdapter.class) {
            try {
                serverSocket = new LocalServerSocket(NAMESPACE);
                new WorkThread().start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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
                    new DataThread(mOutputStream, socket.getInputStream()).start();
                    new DataThread(socket.getOutputStream(), mInputStream).start();
                    isHasClient = true;
                } catch (IOException e) {
                    e.printStackTrace();
                    isHasClient = false;
                    break;
                }
            }
        }
    }

    @Override
    protected void finalize() {
        isHasClient = false;
    }

    class DataThread extends Thread {
        private OutputStream os;
        private InputStream is;

        DataThread(OutputStream os, InputStream is) throws IOException {
            this.os = os;
            this.is = is;
        }

        @Override
        public void run() {
            while (isHasClient) {
                try {
                    if (pause) {
                        Log.e(LOG_TAG, "暂停中！");
                        continue;
                    }
                    if (os != null && is != null) {
                        Log.d(LOG_TAG, "接收中");
                        byte b = (byte) is.read();
                        os.write(b);
                        os.flush();
                        Log.e(LOG_TAG, "数据: " + HexData.hexToString(new byte[]{b}));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    isHasClient = false;
                    break;
                }
            }
        }
    }

    public static ComBridgeAdapter getInstance() {
        return instance;
    }

    public InputStream getInputStream() {
        return mInputStream;
    }

    public ComBridgeAdapter setInputStream(InputStream mInputStream) {
        this.mInputStream = mInputStream;
        return this;
    }

    public OutputStream getOutputStream() {
        return mOutputStream;
    }

    public ComBridgeAdapter setOutputStream(OutputStream mOutputStream) {
        this.mOutputStream = mOutputStream;
        return this;
    }

    public void pause() {
        pause = true;
    }

    static {
        instance = new ComBridgeAdapter();
    }
}
