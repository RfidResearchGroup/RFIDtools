package cn.rrg.rdv.presenter;

import cn.rrg.rdv.models.AbsTagKeysCheckModel;
import cn.rrg.rdv.models.StandardTagKeysCheckModel;

public class StandardTagKeysCheckPresenter
        extends TagKeysCheckPresenterImpl {

    @Override
    public AbsTagKeysCheckModel getTagKeysCheckModel() {
        // 传递秘钥文件的列表来实例化一个数据操作模型!
        return new StandardTagKeysCheckModel(this);
    }

}
