package com.rfidresearchgroup.presenter;

/**
 * 魔术卡检测支持
 *
 * @author DXL
 */
public abstract class AbsMFMagicCheckPresenter
        extends BasePresenter {

    /*
     * 检测是否是魔术卡!
     * */
    protected abstract boolean checkMagic();
}
