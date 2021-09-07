package com.rfidresearchgroup.view;

import com.rfidresearchgroup.javabean.M1Bean;
import com.rfidresearchgroup.javabean.M1KeyBean;

public interface TagWriteView
        extends BaseMvpView, TagStateView {

    // 是否允许写厂商块，必须有这个控制，避免无意中更改UID!
    boolean isWriteManufacturerAllow();

    // 是否使用正序写卡，避免出现某些特殊的卡写入失败的情况!
    boolean isWriteSecOrderImplement();

    // 写入完成的回调!
    void onWriteFinish();

    // 在数据异常时的回调!
    void onDataInvalid();

    // 获取单扇区写的参数!
    int getSector();

    // 写该扇区某个块
    int getBlock();

    // 写入的块数据!
    String getData();

    // 在写入时需要的数据
    M1Bean[] getDatas();

    //在读取单扇区时需要的秘钥
    M1KeyBean[] getKeyBeanForOne();

    //在读取多扇区时需要的秘钥
    M1KeyBean[] getKeyBeanForAll();
}
