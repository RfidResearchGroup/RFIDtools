package com.rfidresearchgroup.javabean;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.util.Arrays;

public class M1Bean implements Serializable {

    //扇区号
    private int sector;
    //数据块的数据
    private String[] datas;

    public M1Bean() {
    }

    public M1Bean(int sector) {
        this.sector = sector;
    }

    public M1Bean(int sector, String[] datas) {
        this.sector = sector;
        this.datas = datas;
    }

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

    @NonNull
    @Override
    public String toString() {
        return "M1Bean{" +
                "sector=" + sector +
                ", datas=" + Arrays.toString(datas) +
                '}';
    }
}
