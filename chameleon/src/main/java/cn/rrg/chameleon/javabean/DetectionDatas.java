package cn.rrg.chameleon.javabean;

import com.dxl.mfkey.Nonce32;

public class DetectionDatas {
    //数据对应的UID
    private int uid;
    //数据对应的块
    private int block;
    //密钥类型! 0 -> A | 1 -> B | -1 -> ERROR
    private int type;
    //数据对应的通信数据!
    private Nonce32 nonce32;

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public int getBlock() {
        return block;
    }

    public void setBlock(int block) {
        this.block = block;
    }

    public Nonce32 getNonce32() {
        return nonce32;
    }

    public void setNonce32(Nonce32 nonce32) {
        this.nonce32 = nonce32;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}