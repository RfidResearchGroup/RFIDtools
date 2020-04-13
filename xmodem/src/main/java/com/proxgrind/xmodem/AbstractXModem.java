package com.proxgrind.xmodem;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import cn.dxl.com.Communication;


/**
 * @author DXL
 * 抽象XModem，定义协议的基本特性，通用动作!
 */
public abstract class AbstractXModem {

    protected String LOG_TAG = this.getClass().getSimpleName();

    // 结束
    protected byte mEOT = 0x04;
    // 应答
    protected byte mACK = 0x06;
    // 重传
    protected byte mNAK = 0x15;
    // 无条件结束
    protected byte mCAN = 0x18;

    // 以128字节块的形式传输数据
    protected int mBlockSize = 128;
    // 最大错误（无应答）包数
    protected int mErrorMax = 10;

    // 通信接口,用于读取串口数据
    private Communication mCom;

    public AbstractXModem(Communication com) {
        this.mCom = com;
    }

    /**
     * @param sources 字节来源，可以是InputStream的任何有效实现!
     * @return 传输结果!
     */
    public abstract boolean send(InputStream sources) throws IOException;

    /**
     * @param target 字节去向，可以是OutputStream的任何有效实现!
     * @return 传输结果!
     */
    public abstract boolean recv(OutputStream target) throws IOException;

    /**
     * 刷新数据,进行剩余字节发送!!
     *
     * @throws IOException 异常
     */
    protected void flush() throws IOException {
        mCom.flush();
    }

    /**
     * 获取数据,每次在超时值内读取一个字节!
     *
     * @return 数据
     * @throws IOException 异常
     */
    protected byte read(int timeout) throws IOException {
        byte[] b = new byte[1];
        mCom.read(b, 0, 1, timeout);
        return b[0];
    }

    /**
     * 发送数据
     *
     * @param data 数据
     * @return 发送成功的字节长度!
     * @throws IOException 异常
     */
    protected int write(byte data, int timeout) throws IOException {
        byte[] b = {data};
        return mCom.write(b, 0, 1, timeout);
    }

    /**
     * 发送数据
     *
     * @param dataByte 数据
     * @param checkSum 校验和
     * @return 发送成功的字节
     * @throws IOException 异常
     */
    protected int write(byte[] dataByte, byte[] checkSum, int timeout) throws IOException {
        //分配缓冲区!
        ByteBuffer bb = ByteBuffer
                .allocate(dataByte.length + checkSum.length)
                .order(ByteOrder.BIG_ENDIAN)
                .put(dataByte)   //提交数据!
                .put(checkSum);  //提交校验!
        byte[] array = bb.array();
        return mCom.write(array, 0, array.length, timeout);
    }

    /**
     * 取消传输。使停止!
     *
     * @param timeout 超时值!
     */
    public void cancel(int timeout) throws IOException {
        write(mCAN, timeout);
    }
}
