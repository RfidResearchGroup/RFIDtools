package cn.dxl.crapto1;

/*
 * 此class用于crapto1算法的回滚计算!
 * */
public class Crapto1 {

    static {
        //加载动态库!
        System.loadLibrary("crapto1");
    }

    private String mHexId = null;
    private String mHexTC = null;
    private String mHexRC = null;
    private String mHexRR = null;
    private String mHexTR = null;

    public Crapto1 setUid(String hexID) {
        mHexId = hexID;
        return this;
    }

    public Crapto1 setTagChallenge(String hexTC) {
        mHexTC = hexTC;
        return this;
    }

    public Crapto1 setReaderChallenge(String hexRC) {
        mHexRC = hexRC;
        return this;
    }

    public Crapto1 setReaderResponse(String hexRR) {
        mHexRR = hexRR;
        return this;
    }

    public Crapto1 setTagResponse(String hexTR) {
        mHexTR = hexTR;
        return this;
    }

    public String finalKey() {
        //非空判定!
        if (mHexId == null || mHexTC == null ||
                mHexRC == null || mHexRR == null || mHexTR == null)
            return null;
        //格式判定!
        if (invailV(mHexId) || invailV(mHexTC)
                || invailV(mHexRC) || invailV(mHexRR) || invailV(mHexTR)) {
            return null;
        }
        return finalKeyNative(mHexId, mHexTC, mHexRC, mHexRR, mHexTR);
    }

    private boolean invailV(String v) {
        return v.length() != 8 || !v.matches("[A-Za-z0-9]+");
    }

    private native String finalKeyNative(String id, String tc, String rc, String rr, String tr);
}
