package com.rfidresearchgroup.mifare;

import java.io.IOException;
import java.io.Serializable;

/*
 * MifareClassic定义
 * */
public interface MifareAdapter extends Serializable {
    /*
     * 一个标准的MifareClassic
     * 是可以被链接，被验证，被读取，被写入，被增值，被减值，被重置的!
     * */

    /**
     * 重新查找获取标签!
     *
     * @return 查找结果!
     */
    boolean rescantag() throws IOException;

    /**
     * 链接标签
     *
     * @return 链接结果!
     */
    boolean connect() throws IOException;

    /**
     * 断开标签
     */
    void close() throws IOException;

    /**
     * 读取标签
     *
     * @param block 读取的块
     * @return 读取结果, 可能为null
     */
    byte[] read(int block) throws IOException;

    /**
     * 写入标签
     *
     * @param blockIndex 写入的块，
     * @param data       将被写入的数据,必须是16字节长度的Hex字符串!
     * @return 写入结果!
     */
    boolean write(int blockIndex, byte[] data) throws IOException;

    /**
     * 验证密钥A
     *
     * @param sectorIndex 被验证的块
     * @param key         用来验证的密钥
     * @return 验证结果!
     */
    boolean authA(int sectorIndex, byte[] key) throws IOException;

    /**
     * 验证密钥B
     *
     * @param sectorIndex 被验证的块
     * @param key         用来验证的密钥
     * @return 验证结果!
     */
    boolean authB(int sectorIndex, byte[] key) throws IOException;

    /**
     * 增值
     *
     * @param blockIndex 被增值的块
     * @param value      非负递增的值
     */
    void increment(int blockIndex, int value) throws IOException;

    /**
     * 增值
     *
     * @param blockIndex 被增值的块
     * @param value      非负递减的值
     */
    void decrement(int blockIndex, int value) throws IOException;

    /**
     * 恢复增值减值操作
     *
     * @param blockIndex 被恢复的块
     */
    void restore(int blockIndex) throws IOException;

    /**
     * 转移值数据到块
     *
     * @param blockIndex 被转移的块
     */
    void transfer(int blockIndex) throws IOException;

    /**
     * 获得UID
     *
     * @return UID字节数组
     */
    byte[] getUid();

    byte[] getAts();

    byte[] getAtqa();

    byte[] getSak();

    /**
     * 获得类型
     *
     * @return 1024 or 2048 or 4096
     */
    int getType();

    /**
     * 获得扇区数量
     *
     * @return 卡片支持的扇区容量
     */
    int getSectorCount();

    /**
     * 获得块数量
     *
     * @return 卡片支持的块容量
     */
    int getBlockCount();

    /**
     * 设置操作超时
     *
     * @param ms 被设置的超时
     */
    void setTimeout(int ms);

    /**
     * 获取操作超时
     *
     * @return 超时参数
     */
    int getTimeout();

    /**
     * 获取批量操作的实现！
     * 如果返回null，则不支持批量!
     */
    BatchAdapter getBatchImpl();

    /**
     * 是否是链接状态!
     *
     * @return 如果卡片已经链接（在部分实现机制上）
     * 否则你可以获取结果为true的返回值，否则为false
     */
    boolean isConnected();

    /**
     * 是否是仿真卡
     *
     * @return true为仿真
     */
    boolean isEmulated();

    /**
     * 是否是后门卡!
     *
     * @return 是否是后门卡
     */
    boolean isSpecialTag();

    /**
     * 是否是支持测试!
     */
    boolean isTestSupported();
}
