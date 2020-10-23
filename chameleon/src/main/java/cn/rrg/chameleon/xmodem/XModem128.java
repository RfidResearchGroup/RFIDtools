package cn.rrg.chameleon.xmodem;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class XModem128 extends AbstractXModem {

    /**
     * 128字节单块传输的XModem协议实现!
     * ---------------------------------------------------------------------------
     * |     Byte1  |  Byte2   |    Byte3    |Byte4~Byte131|  Byte132   |
     * |-------------------------------------------------------------------------|
     * |Start Of Header|Packet Number|~(Packet Number)| Packet Data |  Check Sum |
     * ---------------------------------------------------------------------------
     *
     * @ref https://blog.csdn.net/jumahe/article/details/40266637
     */

    // 开始
    private byte SOH = 0x01;

    public XModem128(InputStream input, OutputStream output) {
        super(input, output);
    }

    @Override
    public boolean send(InputStream sources) throws IOException {
        // 错误包数
        int errorCount;
        // 包序号
        byte blockNumber = 0x01;
        // 校验和
        byte checkSum;
        // 读取到缓冲区的字节数量
        int nbytes;
        // 初始化数据缓冲区
        byte[] sector = new byte[mBlockSize];
        // 读取文件初始化
        while ((nbytes = sources.read(sector)) > 0) {
            // 如果最后一包数据小于128个字节，以0xff补齐
            if (nbytes < mBlockSize) {
                for (int i = nbytes; i < mBlockSize; i++) {
                    sector[i] = (byte) 0xff;
                }
            }
            // 同一包数据最多发送10次
            errorCount = 0;
            while (errorCount < mErrorMax) {
                // 控制字符 + 包序号 + 包序号的反码 + 数据 + 校验和
                write(SOH); //1、发送控制字符!
                write(blockNumber); //2、发送包序号!
                write((byte) (255 - blockNumber)); //3、发送包序号的反码!
                checkSum = calcChecksum(sector, mBlockSize); //4、计算数据的校验和
                write(sector, new byte[]{checkSum}); //5、进行数据+校验和的封包发送!
                flush(); //6、刷新缓冲区，发送数据!
                // 获取应答数据
                byte data = read();
                //Log.d(LOG_TAG, "应答数据为: " + HexUtil.toHexString(data));
                // 如果收到应答数据则跳出循环，发送下一包数据
                // 未收到应答，错误包数+1，继续重发
                if (data == mNAK) {
                    //重传!
                    ++errorCount;
                } else if (data == mCAN) {
                    //终止命令!
                    return false;
                } else if (data == mACK) {
                    //Log.d(LOG_TAG, "ACK已收到，可以进行下一轮发送!");
                    break;
                } else {
                    //重传!
                    ++errorCount;
                }
            }
            // 包序号自增
            blockNumber = (byte) ((++blockNumber) % 256);
        }
        // 所有数据发送完成后，发送结束标识
        boolean isAck = false;
        while (!isAck) {
            write(mEOT);
            isAck = read() == mACK;
        }
        return true;
    }

    @Override
    public boolean recv(OutputStream target) throws IOException {
        // 错误包数
        int errorCount = 0;
        // 包序号
        byte blocknumber = 0x01;
        // 数据
        byte data;
        // 校验和
        int checkSum;
        // 初始化数据缓冲区
        byte[] sector = new byte[mBlockSize];
        // 握手，发起传输!
        write(mNAK);
        while (true) {
            if (errorCount > mErrorMax) {
                //Log.d(LOG_TAG, "错误重试次数已达上限!");
                return false;
            }
            // 获取应答数据
            data = read();
            if (data != mEOT) {
                try {
                    // 判断接收到的是否是开始标识
                    if (data != SOH) {
                        //Log.d(LOG_TAG, "非开始标志!");
                        errorCount++;
                        continue;
                    }
                    // 获取包序号
                    data = read();
                    //Log.d(LOG_TAG, "包序号: " + data);
                    // 判断包序号是否正确
                    if (data != blocknumber) {
                        //Log.d(LOG_TAG, "包序号不正常!");
                        errorCount++;
                        continue;
                    }
                    // 获取包序号的反码
                    byte _blocknumber = read();
                    //Log.d(LOG_TAG, "包序号的反码: " + _blocknumber);
                    // 判断包序号的反码是否正确
                    if (data + _blocknumber != (byte) 255) {
                        //Log.d(LOG_TAG, "包序号的反码不正常!");
                        errorCount++;
                        continue;
                    }
                    // 获取数据
                    for (int i = 0; i < mBlockSize; i++) {
                        sector[i] = read();
                    }
                    //Log.d(LOG_TAG, "获取到的数据: " + HexUtil.toHexString(sector));
                    // 获取校验和
                    checkSum = read();
                    //Log.d(LOG_TAG, "接收到的校验和: " + checkSum);
                    // 判断校验和是否正确
                    int crc = calcChecksum(sector, mBlockSize);
                    //Log.d(LOG_TAG, "计算到的校验和: " + crc);
                    if (crc != checkSum) {
                        //Log.d(LOG_TAG, "包数据的校验不正常!");
                        errorCount++;
                        continue;
                    }
                    //Log.d(LOG_TAG, "接收一帧完成!");
                    // 发送应答
                    write(mACK);
                    // 包序号自增
                    blocknumber++;
                    // 将数据写入本地
                    target.write(sector);
                    // 错误包数归零
                    errorCount = 0;
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    // 如果出错发送重传标识
                    if (errorCount != 0) {
                        //Log.d(LOG_TAG, "错误，将发送重传标志!");
                        write(mNAK);
                    }
                }
            } else {
                break;
            }
        }
        // 发送应答
        write(mACK);
        return true;
    }

    /**
     * Calculates the checksum of the passed byte buffer.
     *
     * @param buffer
     * @param byteCount
     * @return byte checksum value
     */
    private byte calcChecksum(byte[] buffer, int byteCount) {
        byte checksum = 0;
        int bufPos = 0;
        while (byteCount-- != 0) {
            checksum += buffer[bufPos++];
        }
        return checksum;
    }
}
