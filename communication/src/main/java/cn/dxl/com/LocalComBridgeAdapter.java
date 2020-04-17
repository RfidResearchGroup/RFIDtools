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
    public static final String NAMESPACE_DEFAULT = "DXL.COM.ASL";
    // The tag of the log.
    private final String LOG_TAG = "LocalComBridgeAdapter";
    // 本地套接字服务!
    private LocalServerSocket serverSocket;
    // 单例!
    private static LocalComBridgeAdapter instance;
    // 设备输入流!
    private volatile InputStream mInputStreamFromDevice;
    // 设备输出流!
    private volatile OutputStream mOutputStreamFromDevice;
    // 客户端输入流
    private volatile InputStream mInputStreamFromSocket;
    // 客户端输出流
    private volatile OutputStream mOutputStreamFromSocket;
    // 是否已经连接!
    private volatile boolean isHasClient = false;
    // 是否关闭监听!
    private volatile boolean listenAccept = false;
    // 连接到转发服务的客户端!
    private LocalSocket socket;
    // 设备数据转发线程是否可以工作
    private volatile boolean forwardWork = false;
    // connection lock!
    private static final Object LOCK = new Object();

    private LocalComBridgeAdapter() {   // No instantiation is required
        if (!forwardWork) {
            forwardWork = true;
            // 创建一个数据转发线程
            new DeviceDataThread().start();
        }
    }

    /**
     * Data forward thread!
     * data will from a InputStream to a OutputStream!
     */
    private class ConServerThread extends Thread {
        @Override
        public void run() {
            while (listenAccept) {
                try {
                    if (serverSocket != null) {
                        Log.d(LOG_TAG, "服务套接字堵塞等待连接中!");
                        LocalSocket socketInternal = serverSocket.accept();
                        mInputStreamFromSocket = socketInternal.getInputStream();
                        mOutputStreamFromSocket = socketInternal.getOutputStream();
                        socket = socketInternal;
                        isHasClient = true;
                        new SocketDataThread().start();
                    } else {
                        return;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    isHasClient = false;
                    listenAccept = false;
                    Log.w(LOG_TAG, "Connection thread abort!");
                    break;
                }
            }
        }
    }

    /**
     * Data from socket to device transfer
     * need client connect and stable communication!
     */
    private class SocketDataThread extends Thread {
        private byte[] buffer = new byte[1024 * 500];

        SocketDataThread() {
            setPriority(MAX_PRIORITY);
        }

        @Override
        public void run() {
            while (true) {
                try {
                    int len = mInputStreamFromSocket.read(buffer);
                    if (len > 0) {
                        mOutputStreamFromDevice.write(Arrays.copyOf(buffer, len));
                        mOutputStreamFromDevice.flush();
                    }
                    if (len == -1) {
                        throw new IOException("Socket already disconnected.");
                    }
                } catch (IOException e) {
                    // e.printStackTrace();
                    break;
                } catch (Exception e) {
                    // e.printStackTrace();
                }
            }
        }
    }

    /**
     * Data from device to socket client transfer
     * default case, it is always worked at runtime!
     */
    private class DeviceDataThread extends Thread {
        private byte[] buffer = new byte[1024 * 500];

        DeviceDataThread() {
            setPriority(MAX_PRIORITY);
        }

        @Override
        public void run() {
            while (forwardWork) {
                if (isHasClient) { // 有客户端的时候才接收数据!
                    try {
                        int len = mInputStreamFromDevice.read(buffer);
                        if (len > 0) {
                            mOutputStreamFromSocket.write(Arrays.copyOf(buffer, len));
                            mOutputStreamFromSocket.flush();
                        }
                    } catch (Exception ignored) {
                        // Empty
                    }
                }
            }
        }
    }

    @Override
    protected void finalize() {
        stopClient();
        stopServer();
        forwardWork = false;
    }

    public static LocalComBridgeAdapter getInstance() {
        synchronized (LocalComBridgeAdapter.class) {
            /*
             * It is a single instance tools
             * you can't instantiation than for once.
             * */
            if (instance == null) instance = new LocalComBridgeAdapter();
        }
        return instance;
    }

    public InputStream getInputStream() {
        return mInputStreamFromDevice;
    }

    public LocalComBridgeAdapter setInputStream(InputStream mInputStream) {
        this.mInputStreamFromDevice = mInputStream;
        return this;
    }

    public OutputStream getOutputStream() {
        return mOutputStreamFromDevice;
    }

    public LocalComBridgeAdapter setOutputStream(OutputStream mOutputStream) {
        this.mOutputStreamFromDevice = mOutputStream;
        return this;
    }

    public LocalComBridgeAdapter startServer(String namespace) {
        synchronized (LOCK) {
            if (!listenAccept) {
                listenAccept = true;
                try {
                    if (serverSocket == null) {
                        serverSocket = new LocalServerSocket(namespace);
                    }
                    // 创建一个客户端连接线程
                    new ConServerThread().start();
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e(LOG_TAG, "If you see an error message like \"Address already in use\", check that you call the stopServer function");
                }
                Log.w(LOG_TAG, "LocalComBridgeAdapter start!");
            }
        }
        return this;
    }

    public LocalComBridgeAdapter stopServer() {
        synchronized (LOCK) {
            listenAccept = false;
            if (serverSocket != null) {
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                serverSocket = null;
            }
            Log.w(LOG_TAG, "LocalComBridgeAdapter server stop!");
        }
        return this;
    }

    public LocalComBridgeAdapter stopClient() {
        synchronized (LOCK) {
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
            Log.d(LOG_TAG, "LocalComBridgeAdapter client stop!");
        }
        return this;
    }
}