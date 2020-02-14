package cn.rrg.rdv.javabean;

/*
 * 这个bean是用来存放验证完成后的key
 */
public class M1KeyBean {
    //扇区号
    private int sector;

    //ab密钥
    private String keyA; 
    private String keyB;

    public int getSector() {
        return sector;
    }

    public void setSector(int sector) {
        this.sector = sector;
    }

    public String getKeyA() {
        return keyA;
    }

    public void setKeyA(String keyA) {
        this.keyA = keyA;
    }

    public String getKeyB() {
        return keyB;
    }

    public void setKeyB(String keyB) {
        this.keyB = keyB;
    }
}
