package com.rfidresearchgroup.mifare;

import java.io.IOException;

/**
 * MifareClassic S50 & S70 batch verity and RW!
 *
 * @author DXL
 * @version 1.0
 */
public interface BatchAdapter {
    /**
     * 读取标签
     *
     * @param block 读取的起始块
     *              如果all为true，则将会读取该扇区所有块，此时参数一就是扇区的起始块,
     *              如果all为false，则将会只读单个块，此时参数一就是块!
     * @return 读取结果, 可能为null
     */
    byte[][] read(int block, boolean isKeyA, byte[] key, boolean isReadSector) throws IOException;

    /**
     * 写入标签
     *
     * @param sector 写入的扇区，
     * @param data   将被写入的数据,单项必须是16字节长度的Hex字符串
     * @return 写入结果!
     */
    boolean write(int sector, boolean isKeyA, byte[] key, byte[] data) throws IOException;

    /**
     * 验证标签，使用一定长度的秘钥组
     *
     * @param sector    需要验证的扇区号!
     * @param keysGroup 被用来验证的秘钥组!
     * @param isKeyA    是否是验证秘钥A的
     * @return 验证成功的秘钥!
     */
    byte[] verity(int sector, byte[][] keysGroup, boolean isKeyA) throws IOException;

    /**
     * 获取批量操作的超时!
     */
    int getTimeout();

    /**
     * 设置批量操作的超时!
     */
    void setTimeout(int timeout);
}
