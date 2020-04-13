package cn.dxl.com;

import java.io.Closeable;
import java.io.IOException;
import java.io.Serializable;

/*
 * author DXL
 * 通信接口，实现了必要的元素传递以及通信实现规定
 */
public interface Communication extends Serializable, Closeable {

    /**
     * @param sendMsg data buffer, transfer data from here.
     * @param offset  the data from buffer valid offset.
     * @param length  the data length from buffer,buffer may be 1024bytes, but valid data length is 512bytes.
     * @param timeout if write need timeout, use me.
     * @return the length of write finished.
     * @throws IOException throw a IOException if have some problem.
     */
    int write(byte[] sendMsg, int offset, int length, int timeout) throws IOException;

    /**
     * @param recvMsg data buffer, read data to here.
     * @param offset  read data to buffer from offset started.
     * @param length  the length of data need read.
     * @param timeout if read need timeout at data length no enough, use me.
     * @return the length of read finished.
     * @throws IOException throw a IOException if have some problem.
     */
    int read(byte[] recvMsg, int offset, int length, int timeout) throws IOException;

    /**
     * flush outputstream.
     *
     * @throws IOException throw a IOException if have some problem.
     */
    void flush() throws IOException;

    /**
     * close io stream
     *
     * @throws IOException throw a IOException if have some problem.
     */
    void close() throws IOException;
}