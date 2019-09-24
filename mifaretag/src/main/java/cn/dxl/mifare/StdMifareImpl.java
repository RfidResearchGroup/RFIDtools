package cn.dxl.mifare;

import android.nfc.Tag;
import android.nfc.tech.MifareClassic;

import java.io.IOException;

public class StdMifareImpl implements MifareAdapter {
    private MifareClassic mMfTag = null;

    /*
     * 此方法需要传入一个TAG，这个TAG将被用来初始化一个MifareClassic标签!
     * */
    public StdMifareImpl() {
        Tag tag = GlobalTag.getTag();
        if (tag != null) {
            mMfTag = MifareClassic.get(tag);
            if (mMfTag != null) {
                mMfTag.setTimeout(1000);
            }
        }
    }

    public MifareClassic getMf() {
        return mMfTag;
    }

    @Override
    public boolean rescantag() {
        Tag tag = GlobalTag.getTag();
        if (tag != null) {
            mMfTag = MifareClassic.get(tag);
            if (mMfTag != null) {
                mMfTag.setTimeout(1000);
            }
        }
        return mMfTag != null;
    }

    @Override
    public boolean connect() {
        try {
            mMfTag.close();
            mMfTag.connect();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void close() {
        try {
            if (mMfTag != null)
                mMfTag.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public byte[] read(int block) {
        try {
            if (mMfTag != null)
                return mMfTag.readBlock(block);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean write(int blockIndex, byte[] data) {
        try {
            if (mMfTag != null) {
                mMfTag.writeBlock(blockIndex, data);
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean authA(int sectorIndex, byte[] key) {
        try {
            if (mMfTag != null)
                return mMfTag.authenticateSectorWithKeyA(sectorIndex, key);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean authB(int sectorIndex, byte[] key) {
        try {
            if (mMfTag != null)
                return mMfTag.authenticateSectorWithKeyB(sectorIndex, key);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void increment(int blockIndex, int value) {
        try {
            if (mMfTag != null)
                mMfTag.increment(blockIndex, value);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void decrement(int blockIndex, int value) {
        try {
            if (mMfTag != null)
                mMfTag.decrement(blockIndex, value);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void restore(int blockIndex) {
        try {
            if (mMfTag != null)
                mMfTag.restore(blockIndex);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void transfer(int blockIndex) {
        try {
            if (mMfTag != null)
                mMfTag.transfer(blockIndex);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public byte[] getUid() {
        if (mMfTag != null)
            return mMfTag.getTag().getId();
        return null;
    }

    @Override
    public int getType() {
        if (mMfTag != null)
            return mMfTag.getType();
        return -1;
    }

    @Override
    public int getSectorCount() {
        if (mMfTag != null)
            return mMfTag.getSectorCount();
        return -1;
    }

    @Override
    public int getBlockCount() {
        if (mMfTag != null)
            return mMfTag.getBlockCount();
        return -1;
    }

    @Override
    public void setTimeout(int ms) {
        if (mMfTag != null)
            mMfTag.setTimeout(ms);
    }

    @Override
    public int getTimeout() {
        if (mMfTag != null)
            return mMfTag.getTimeout();
        return -1;
    }

    @Override
    public boolean isEmulated() {
        return false;
    }

    @Override
    public boolean isSpecialTag() {
        //自带的NFC不支持特殊的后门标签直接读写，因此直接返回false即可!
        return false;
    }

    @Override
    public boolean isTestSupported() {
        return false;
    }
}
