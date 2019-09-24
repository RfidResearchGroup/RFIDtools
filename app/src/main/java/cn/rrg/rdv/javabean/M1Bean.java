package cn.rrg.rdv.javabean;

import java.io.Serializable;

public class M1Bean implements Serializable {

    //扇区号
    private int sector;
    //数据块的数据
    private String[] datas;

    public int getSector() { 
        return sector;
    }

    public void setSector(int sector) {
        this.sector = sector;
    }

    public String[] getDatas() {
        return datas;
    }

    public void setDatas(String[] datas) {
        this.datas = datas;
    }

}
