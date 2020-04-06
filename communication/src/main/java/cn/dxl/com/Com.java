package cn.dxl.com;

import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;

import cn.dxl.common.posixio.Communication;
import cn.rrg.com.Device;

/**
 * @author DXL
 * Created by DXL on 2017/8/21.
 * 通信控制映射类!
 */
public final class Com implements Serializable {

    /*
     * Warning!!!
     * For standardization and security,
     * you should not change any functions and variables defined by the native keyword,
     * or you may have a big problem!
     * If your implementation does not need to be mapped to the c/c++ (only Java exists), please do not use this tool class!
     *
     * 警告!!!
     * 为了规范性和安全性，你不应当更改任何native关键字定义的函数与变量，否则可能会出现大问题！
     * 如果您的实现不需要映射到底层（只存在Java），请不要使用此工具类!
     * */

    // Buffer
    private static ByteBuffer recv_buffer;
    private static ByteBuffer send_buffer;
    // Communication interface, implement from development.
    private static Communication mCommunication = null;
    //Buffer size!
    private final static int BUFFER_SIZE = 1024 * 1024;

    /*
     * 静态块初始化区域
     */
    static {
        System.loadLibrary("commapping");
        //数据缓冲区长度,16384字节
        send_buffer = ByteBuffer.allocateDirect(BUFFER_SIZE);
        recv_buffer = ByteBuffer.allocateDirect(BUFFER_SIZE);
        //初始化缓冲区
        initBuffer(send_buffer, recv_buffer);
    }

    // No instantiation is required
    private Com() { /*This constructor can't using*/ }

    /**
     * init the communication, use {@link Device }
     *
     * @param communication base communication implement!
     * @param dev           the device instance!
     * @return init communication, true is successful or false is error!sss
     */
    public static boolean initCom(Communication communication, Device dev) {
        //初始化通信实现
        mCommunication = communication;
        //判断端口是否正常初始化
        try {
            return dev.working();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * write byte from {@link #send_buffer} to devices!
     *
     * @return write result, finish byte count.
     * @throws IOException IOException in write error.
     */
    private static int write() throws IOException {
        if (mCommunication == null) return -1;
        //加上偏移值
        //try {
        return mCommunication.write(send_buffer.array(),
                send_buffer.arrayOffset(), syncLength() +
                        send_buffer.arrayOffset(), syncTimeout());
        //} catch (IOException e) {
        //e.printStackTrace();
        //}
    }

    /**
     * read byte from devices to{@link #recv_buffer}!
     *
     * @return read result, all byte count.
     * @throws IOException IOException in read error.
     */
    private static int read() throws IOException {
        if (mCommunication == null) return -1;
        //try {
        return mCommunication.read(recv_buffer.array(),
                recv_buffer.arrayOffset(), syncLength() +
                        recv_buffer.arrayOffset(), syncTimeout());
        //} catch (IOException e) {
        //e.printStackTrace();
        //}
    }

    /**
     * flush data, clear buffer!
     */
    private static void flush() throws IOException {
        if (mCommunication == null) return;
        //try {
        mCommunication.flush();
        //} catch (IOException e) {
        //e.printStackTrace();
        //}
    }

    /**
     * close devices, clear buffer and break communication!
     * TODO don't close, because some communication can't close() in c layer.
     */
    private static void close() throws IOException {
        //try {
        //communication.close();
        //} catch (IOException e) {
        //e.printStackTrace();
        //}
    }

    /*
     * author DXL
     * some communication native function
     * you can't direct invoke， because that are auto invoke in inner static layer!
     */

    /**
     * inner invoking, init buffer mapping from java to c layer!
     *
     * @param recv_buffer read data buffer, the data is from java to native layer!
     * @param send_buffer write data buffer, the data is from native layer to java!
     */
    private static native int initBuffer(ByteBuffer send_buffer, ByteBuffer recv_buffer);

    /**
     * get length from native layer!
     *
     * @return read or write length!
     */
    private static native int syncLength();

    /**
     * get timeout from native layer!
     *
     * @return read or write timeout!
     */
    private static native int syncTimeout();
}
