package cn.dxl.mfkey;

/**
 * 32长度随机数恢复!
 * 部分功能链式调用!
 *
 * @author DXL
 */
public class Nonce32 {
    private int nt;
    private int nr;
    private int ar;

    public Nonce32() {
        //空构造!
    }

    public Nonce32(int nt, int nr, int ar) {
        this.nt = nt;
        this.nr = nr;
        this.ar = ar;
    }

    public int getNt() {
        return nt;
    }

    public Nonce32 setNt(int nt) {
        this.nt = nt;
        return this;
    }

    public int getNr() {
        return nr;
    }

    public Nonce32 setNr(int nr) {
        this.nr = nr;
        return this;
    }

    public int getAr() {
        return ar;
    }

    public Nonce32 setAr(int ar) {
        this.ar = ar;
        return this;
    }
}
