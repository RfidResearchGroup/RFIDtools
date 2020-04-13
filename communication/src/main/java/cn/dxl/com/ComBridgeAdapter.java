package cn.dxl.com;

import android.net.LocalServerSocket;

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
    // 本地套接字服务!
    private static LocalServerSocket serverSocket;
    // 单例!
    private static ComBridgeAdapter instance;
    // 输入流!
    public InputStream mInputStream;
    // 输出流!
    public OutputStream mOutputStream;

    /*
     * 静态块初始化区域
     */
    static {
        instance = new ComBridgeAdapter();
        try {
            serverSocket = new LocalServerSocket(NAMESPACE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // No instantiation is required
    private ComBridgeAdapter() { /*This constructor can't using*/ }

    public static ComBridgeAdapter getInstance() {
        return instance;
    }

    public InputStream getInputStream() {
        return mInputStream;
    }

    public void setInputStream(InputStream mInputStream) {
        this.mInputStream = mInputStream;
    }

    public OutputStream getOutputStream() {
        return mOutputStream;
    }

    public void setOutputStream(OutputStream mOutputStream) {
        this.mOutputStream = mOutputStream;
    }


}
