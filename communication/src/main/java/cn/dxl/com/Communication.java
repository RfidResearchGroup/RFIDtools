package cn.dxl.com;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;

/*
 * author DXL
 * 通信接口，实现了必要的元素传递以及通信实现规定
 */
public interface Communication extends Serializable {
    /**
     * Get OutputStream implement!
     */
    OutputStream getOutput();

    /*
     * Get InputStream implement!
     * */
    InputStream getInput();
}