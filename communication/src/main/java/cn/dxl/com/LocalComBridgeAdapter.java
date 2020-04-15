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
    public static final String NAMESPACE = "LocalComBridgeAdapter";
    // The tag of the log.
    public static final String LOG_TAG = "LocalComBridgeAdapter";
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
    // 是否关闭监听!
    private volatile boolean listenAccept = false;
    // 暂停转发!
    private boolean pause = false;
    // 连接到转发服务的客户端!
    private LocalSocket socket;

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
            while (listenAccept) {
                try {
                    if (serverSocket != null) {
                        socket = serverSocket.accept();
                        if (isHasClient) {
                            isHasClient = false;
                            Log.e(LOG_TAG, "The server only once online supported.");
                            Log.e(LOG_TAG, "please disconnect your previous con.");
                            continue;
                        }
                        isHasClient = true;
                        Thread socket2Device = new DataThread(socket, mOutputStream, socket.getInputStream());
                        Thread device2Socket = new DataThread(socket, socket.getOutputStream(), mInputStream);
                        // start task!
                        device2Socket.start();
                        socket2Device.start();
                    } else {
                        return;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    isHasClient = false;
                    listenAccept = false;
                    Log.d(LOG_TAG, "链接线程终止!");
                    break;
                }
            }
        }
    }

    class DataThread extends Thread {
        private OutputStream os;
        private InputStream is;
        private byte[] buffer = new byte[1024 * 500];
        private LocalSocket socket;

        DataThread(LocalSocket socket, OutputStream os, InputStream is) {
            this.os = os;
            this.is = is;
            this.socket = socket;
        }

        @Override
        public void run() {
            while (isHasClient) {
                try {
                    if (socket.getFileDescriptor() == null) {
                        throw new IOException("Socket disconnected!");
                    }
                    if (pause) {
                        Log.e(LOG_TAG, "pausing！");
                        continue;
                    }
                    if (os != null && is != null) {
                        int len = is.read(buffer);
                        if (len > 0) {
                            os.write(Arrays.copyOf(buffer, len));
                        }
                    } else {
                        throw new IOException("IO closed.");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    isHasClient = false;
                    break;
                }
            }
            isHasClient = false;
        }
    }

    @Override
    protected void finalize() {
        stopClient();
        stopServer();
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

    public LocalComBridgeAdapter startServer() {
        synchronized (LocalComBridgeAdapter.class) {
            if (!listenAccept) {
                listenAccept = true;
                try {
                    if (serverSocket == null) {
                        serverSocket = new LocalServerSocket(NAMESPACE);
                    }
                    new WorkThread().start();
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e(LOG_TAG, "If you see an error message like \"Address already in use\", check that you call the stopServer function");
                    Log.e(LOG_TAG, "如果你看到了类似Address already in use的错误消息，请检查你是否调用停止函数");
                }
                Log.d(LOG_TAG, "ComBridgeAdapter start!");
            }
        }
        return this;
    }

    public void stopServer() {
        listenAccept = false;
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            serverSocket = null;
        }
    }

    public void stopClient() {
        isHasClient = false;
        try {

            if (socket != null) {
                socket.shutdownInput();
                socket.shutdownOutput();
                socket.close();
                socket = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.d(LOG_TAG, "ComBridgeAdapter stop!");
    }

    static {
        instance = new LocalComBridgeAdapter();
    }
}
