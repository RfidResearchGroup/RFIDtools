package com.proxgrind.com;

import android.hardware.usb.UsbEndpoint;
import android.net.LocalServerSocket;
import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;

/**
 * @author DXL
 * Created by DXL on 2017/8/21.
 * Communication control mapping implementation,
 * the realization of inter process communication!
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
    // con thread status!
    private volatile boolean isConThreadRunning = false;
    // device thread status!
    private volatile boolean isDeviceDataThreadRunning = false;
    // server action timeout, default is 1000ms!
    private int timeout = 1000;
    // current namespace
    private String namespace;
    // is server stop
    private volatile boolean isServerStopAction = false;
    // The buffer max for the device IO, default is 16384
    private int deviceBufferMax = 16384;

    private LocalComBridgeAdapter() {   /* No instantiation is required */ }

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
                        isConThreadRunning = true;
                        LocalSocket socketInternal = serverSocket.accept();
                        if (isServerStopAction) throw new IOException("Server closed.");
                        Log.d(LOG_TAG, "服务套接字连接成功，将会开启一个客户端线程!");
                        mInputStreamFromSocket = socketInternal.getInputStream();
                        mOutputStreamFromSocket = socketInternal.getOutputStream();
                        socket = socketInternal;
                        isHasClient = true;
                        new SocketDataThread().start();
                    } else {
                        return;
                    }
                } catch (IOException e) {
                    // e.printStackTrace();
                    isHasClient = false;
                    listenAccept = false;
                    Log.w(LOG_TAG, "Connection thread abort!");
                    break;
                }
            }
            isConThreadRunning = false;
            Log.d(LOG_TAG, "ConServerThread结束");
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
            Log.d(LOG_TAG, "SocketDataThread执行");
            while (true) {
                try {
                    int available = mInputStreamFromSocket.available();
                    if (available > 0) {
                        int len = mInputStreamFromSocket.read(buffer);
                        // Log.d(LOG_TAG, "mInputStreamFromSocket.read()->len: " + len);
                        if (len > 0) {
                            mOutputStreamFromDevice.write(buffer, 0, len);
                            mOutputStreamFromDevice.flush();
                            // Log.d(LOG_TAG, "SocketDataThread数据传输: " + HexUtil.toHexString(buffer, 0, len));
                        }
                        if (len == -1) {
                            throw new IOException("Socket already disconnected.");
                        }
                    }
                } catch (IOException e) {
                    // e.printStackTrace();
                    break;
                } catch (Exception e) {
                    // e.printStackTrace();
                }
            }
            Log.d(LOG_TAG, "SocketDataThread结束");
        }
    }

    /**
     * Data from device to socket client transfer
     * default case, it is always worked at runtime!
     */
    private class DeviceDataThread extends Thread {
        // 在某些机型上，这个值是无效的，会导致bulkTransfer()卡死
        // private byte[] buffer = new byte[1024 * 500];
        // 因此我们需要使用Usb规定的最大值
        private final byte[] buffer = new byte[deviceBufferMax];

        DeviceDataThread() {
            setPriority(MAX_PRIORITY);
        }

        @Override
        public void run() {
            Log.d(LOG_TAG, "DeviceDataThread执行");
            isDeviceDataThreadRunning = true;
            while (forwardWork) {
                if (isHasClient) { // 有客户端的时候才接收数据!
                    try {
                        int len = mInputStreamFromDevice.read(buffer);
                        if (len > 0) {
                            // Log.d(LOG_TAG, "mInputStreamFromDevice.read()->len: " + len);
                            mOutputStreamFromSocket.write(buffer, 0, len);
                            mOutputStreamFromSocket.flush();
                            // Log.d(LOG_TAG, "DeviceDataThread数据传输: " + HexUtil.toHexString(buffer));
                        }
                    } catch (Exception e) {
                        // Empty
                        // e.printStackTrace();
                    }
                }
            }
            Log.d(LOG_TAG, "DeviceDataThread结束");
        }
    }

    public static LocalComBridgeAdapter getInstance() {
        synchronized (LOCK) {
            /*
             * It is a single instance tools
             * you can't instantiation than for once.
             * */
            if (instance == null) instance = new LocalComBridgeAdapter();
        }
        return instance;
    }

    /**
     * get inputStream from external device
     *
     * @return the inputStream of device
     */
    public InputStream getInputStream() {
        return mInputStreamFromDevice;
    }

    /**
     * set inputStream from external device
     * The adapter will automatically read bytes from this input stream
     * and forward them to the client with socket
     *
     * @param mInputStream the inputStream from external device implement!
     * @return this
     */
    public LocalComBridgeAdapter setInputStream(InputStream mInputStream) {
        this.mInputStreamFromDevice = mInputStream;
        return this;
    }

    /**
     * get outputStream from external device
     *
     * @return the outputStream of device
     */
    public OutputStream getOutputStream() {
        return mOutputStreamFromDevice;
    }

    /**
     * set outputStream from external device
     * The adapter will automatically read bytes from the socket client
     * and write them to this output stream
     *
     * @param mOutputStream the outputStream from external device implement!
     * @return this
     */
    public LocalComBridgeAdapter setOutputStream(OutputStream mOutputStream) {
        this.mOutputStreamFromDevice = mOutputStream;
        return this;
    }

    /**
     * Turn on server on a namespace
     * default namespace is {@link #NAMESPACE_DEFAULT }
     * namespace only once instance at runtime
     * namespace is unique
     *
     * @param namespace the namespace is also used when clients connect
     * @return this
     */
    public LocalComBridgeAdapter startServer(String namespace) {
        synchronized (LOCK) {
            if (!listenAccept) {
                listenAccept = true;
                isConThreadRunning = false;
                isServerStopAction = false;
                try {
                    if (serverSocket == null) {
                        serverSocket = new LocalServerSocket(namespace);
                        this.namespace = namespace;
                    }
                    // 创建一个客户端连接线程
                    new ConServerThread().start();
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e(LOG_TAG, "If you see an error message like \"Address already in use\", check that you call the stopServer function");
                }
                // wait con thread running...
                long startTime = System.currentTimeMillis();
                while (!isConThreadRunning) {
                    // Log.w(LOG_TAG, "Waiting for LocalComBridgeAdapter server start......");
                    if (System.currentTimeMillis() - startTime > timeout) {
                        Log.w(LOG_TAG, "LocalComBridgeAdapter server start timeout.");
                        return this;
                    }
                }
            } else {
                Log.w(LOG_TAG, "LocalComBridgeAdapter already start!");
            }
            if (!forwardWork) {
                forwardWork = true;
                isDeviceDataThreadRunning = false;
                // 创建一个数据转发线程
                new DeviceDataThread().start();
                // wait device data thread running...
                long startTime = System.currentTimeMillis();
                while (!isDeviceDataThreadRunning) {
                    // Log.w(LOG_TAG, "Waiting for LocalComBridgeAdapter server start......");
                    if (System.currentTimeMillis() - startTime > timeout) {
                        Log.w(LOG_TAG, "LocalComBridgeAdapter server start timeout.");
                        return this;
                    }
                }
            }
            // if task is start successfully, we need sleep 100ms, ensure all resource is readied.
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Log.d(LOG_TAG, "LocalComBridgeAdapter start!");
        }
        return this;
    }

    /**
     * Stop server listen!
     * After the service is stopped,
     * the client will not be able to connect.
     * In general, please do not shut down the service.
     *
     * @return this
     */
    public LocalComBridgeAdapter stopServer() {
        synchronized (LOCK) {
            listenAccept = false;
            isServerStopAction = true;
            isDeviceDataThreadRunning = false;
            if (serverSocket != null) {
                try {
                    simClient2CloseServer();
                    serverSocket.close();
                } catch (IOException ignored) {
                }
                serverSocket = null;
            }
            long startTime = System.currentTimeMillis();
            while (isConThreadRunning) {
                // Log.w(LOG_TAG, "Waiting for LocalComBridgeAdapter server stop......");
                if (System.currentTimeMillis() - startTime > timeout) {
                    Log.w(LOG_TAG, "LocalComBridgeAdapter server stop timeout.");
                    return this;
                }
            }
            Log.d(LOG_TAG, "LocalComBridgeAdapter server stop!");
        }
        return this;
    }

    // Simulate the behavior of requesting a connection to close it
    private void simClient2CloseServer() {
        LocalSocket localSocket = new LocalSocket();
        try {
            localSocket.connect(new LocalSocketAddress(namespace));
            localSocket.shutdownInput();
            localSocket.shutdownOutput();
            localSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Stop client connection.
     * Calling this function will close the client link from C/C++ or java or any...
     * please call when you need to connect to a new client.
     *
     * @return this
     */
    public LocalComBridgeAdapter stopClient() {
        synchronized (LOCK) {
            isHasClient = false;
            try {
                if (socket != null) {
                    // close socket
                    socket.shutdownInput();
                    socket.shutdownOutput();
                    socket.close();
                    mInputStreamFromSocket.close();
                    mOutputStreamFromSocket.close();
                    socket = null;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            Log.d(LOG_TAG, "LocalComBridgeAdapter client stop!");
        }
        return this;
    }

    /**
     * The timeout fro start and stop server!
     *
     * @return timeout ms.
     */
    public int getTimeout() {
        return timeout;
    }

    /**
     * The timeout for start and stop server!
     *
     * @param timeout timeout ms.
     * @return this
     */
    public LocalComBridgeAdapter setTimeout(int timeout) {
        this.timeout = timeout;
        return this;
    }

    /**
     * Get buffer of device
     *
     * @return buffer size max
     */
    public int getDeviceBufferMax() {
        return deviceBufferMax;
    }

    /**
     * Set buffer size max of the device.
     * if you are UsbDevice, the buffer size recommend use default!
     * or less than 16384, see {@link android.hardware.usb.UsbDeviceConnection#bulkTransfer(UsbEndpoint, byte[], int, int)}
     *
     * @param deviceBufferMax the new size of buffer
     * @return this
     */
    public LocalComBridgeAdapter setDeviceBufferMax(int deviceBufferMax) {
        this.deviceBufferMax = deviceBufferMax;
        return this;
    }

    /**
     * Destroy all task and recovery all resources
     * Warning!! this action only can run on app exit!
     * It will destroy server and client, and adapter single instance!
     * so, it can run on application exit only.
     */
    public void destroy() {
        stopClient();
        stopServer();
        forwardWork = false;
        instance = null;
    }
}