package cn.rrg.natives;

import android.nfc.tech.MifareClassic;

import cn.dxl.common.util.HexUtil;
import cn.dxl.mifare.BatchAdapter;
import cn.dxl.mifare.MifareAdapter;

/**
 * Created by DXL on 2019/09/18.
 */
public class PM3Rdv4RRGMifare implements MifareAdapter {

    static {
        //在静态块加载对应的模块
        System.loadLibrary("pm3rrg_rdv4");
    }

    //单例模式
    private PM3Rdv4RRGMifare() {
        //can't using default constructor
        // write manufacturer allow default!
        setWriteUID(true);
    }

    //懒汉单例
    private static PM3Rdv4RRGMifare mMF = null;

    public static PM3Rdv4RRGMifare get() {
        if (mMF == null) {
            mMF = new PM3Rdv4RRGMifare();
        }
        return mMF;
    }

    //TODO 扫描卡片， 获得场内优先级最高的卡
    public native boolean scanning();

    //TODO 连接卡片，进行操作
    private native boolean con();

    //TODO 断开卡片，重新操作
    private native boolean disconnect();

    //TODO 分析卡片，获得信息
    public native byte[] getUid();

    @Override
    public byte[] getAts() {
        return new byte[0];
    }

    //TODO 分析卡片，获得信息
    public native byte[] getAtqa();

    //TODO 分析卡片，获得信息
    public native byte[] getSak();

    //TODO 分析卡片，获得信息
    public native boolean isEmulated();

    //TODO 分析卡片，获得信息
    public native int getSize();

    //TODO 分析卡片，获得信息
    public native int getSectorCount();

    //TODO 分析卡片，获得信息
    public native int getBlockCount();

    //TODO 操作卡片，解锁UID块
    public native boolean unlock();

    //TODO 操作卡片，进行封卡
    public native boolean uplock();

    //TODO 操作卡片，判断解锁结果
    public native boolean isUnlock();

    //TODO 操作卡片，是否写零块
    public native void setWriteUID(boolean trueIsWrite);

    //TODO 操作卡片，验证A秘钥块
    private native boolean authWithKeyA(int sector, byte[] keyA);

    //TODO 操作卡片，验证B秘钥块
    private native boolean authWithKeyB(int sector, byte[] keyB);

    //TODO 操作卡片，读取卡片
    private native byte[] readBlock(int block);

    //TODO 操作卡片，写入卡片
    private native boolean writeBlock(int block, byte[] data);

    @Override
    public boolean rescantag() {
        return scanning() && connect();
    }

    @Override
    public boolean connect() {
        //调用底层链接实现!
        return con();
    }

    @Override
    public void close() {
        disconnect();
    }

    @Override
    public byte[] read(int block) {
        return readBlock(block);
    }

    @Override
    public boolean write(int blockIndex, byte[] data) {
        return writeBlock(blockIndex, data);
    }

    @Override
    public boolean authA(int sectorIndex, byte[] key) {
        return authWithKeyA(sectorIndex, key);
    }

    @Override
    public boolean authB(int sectorIndex, byte[] key) {
        return authWithKeyB(sectorIndex, key);
    }

    @Override
    public void increment(int blockIndex, int value) {
        try {
            throw new Exception("暂不允许调用");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void decrement(int blockIndex, int value) {
        try {
            throw new Exception("暂不允许调用");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void restore(int blockIndex) {
        try {
            throw new Exception("暂不允许调用");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void transfer(int blockIndex) {
        try {
            throw new Exception("暂不允许调用");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getType() {
        try {
            int sakInt = Integer.valueOf(HexUtil.toHexString(getSak()));
            switch (sakInt) {
                case 0x01:
                case 0x08:
                    return MifareClassic.TYPE_CLASSIC;
                case 0x09:
                    return MifareClassic.TYPE_CLASSIC;
                case 0x10:
                    return MifareClassic.TYPE_PLUS;
                // SecLevel = SL2
                case 0x11:
                    return MifareClassic.TYPE_PLUS;
                // Seclevel = SL2
                case 0x18:
                    return MifareClassic.TYPE_CLASSIC;
                case 0x28:
                    return MifareClassic.TYPE_CLASSIC;
                //mIsEmulated = true;
                case 0x38:
                    return MifareClassic.TYPE_CLASSIC;
                //mIsEmulated = true;
                case 0x88:
                    return MifareClassic.TYPE_CLASSIC;
                // NXP-tag: false
                case 0x98:
                case 0xB8:
                    return MifareClassic.TYPE_PRO;
                default:
                    // Stack incorrectly reported a MifareClassic. We cannot handle this
                    // gracefully - we have no idea of the memory layout. Bail.
                    throw new RuntimeException(
                            "Tag incorrectly enumerated as MIFARE Classic, SAK = " + sakInt);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    @Override
    public void setTimeout(int ms) {
        try {
            throw new Exception("暂不允许调用");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getTimeout() {
        return 0;
    }

    @Override
    public BatchAdapter getBatchImpl() {
        return null;
    }

    @Override
    public boolean isConnected() {
        return false;
    }

    @Override
    public boolean isSpecialTag() {
        return unlock();
    }

    @Override
    public boolean isTestSupported() {
        return false;
    }
}