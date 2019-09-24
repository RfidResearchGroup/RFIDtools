package cn.rrg.rdv.view;


import cn.rrg.rdv.javabean.M1Bean;
import cn.rrg.rdv.javabean.M1KeyBean;

public interface TagReadView extends BaseMvpView, TagStateView {
    //读取完成的回调
    void onReadFinish(M1Bean[] datas);

    int[] getReadeSectorSelected();

    //在读取单扇区时
    M1KeyBean[] getKeyBeanForOne();

    //在读取多扇区时
    M1KeyBean[] getKeyBeanForAll();
}
