package com.dxl.mfkey;

/**
 * @author DXL
 * Nonce64类比Nonce32类多了几个属性变量，
 * 其他的一致相同，此时可以继承复用!
 */
public class Nonce64 extends Nonce32 {
    private int at;
    private int[] enc;

    public Nonce64(int nt, int nr, int ar, int at) {
        super(nt, nr, ar);
        this.at = at;
    }

    public int getAt() {
        return at;
    }

    public Nonce64 setAt(int at) {
        this.at = at;
        return this;
    }

    public int[] getEnc() {
        return enc;
    }

    public Nonce64 setEnc(int[] enc) {
        this.enc = enc;
        return this;
    }
}
