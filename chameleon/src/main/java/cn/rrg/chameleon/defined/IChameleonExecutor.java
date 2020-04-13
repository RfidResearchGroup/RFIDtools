package cn.rrg.chameleon.defined;

import java.io.IOException;

import cn.dxl.com.Communication;

/**
 * 接口，定义变色龙的执行器的功能!
 *
 * @author DXL
 */
public interface IChameleonExecutor {
    boolean initExecutor(Communication com);

    byte[] requestChameleon(String at, int timeout, boolean xmodemMode);

    byte[] requestChameleon(int timeout, int length);

    void requestChameleon(String at) throws IOException;

    Communication getCom();

    int clear(int timeout);
}
