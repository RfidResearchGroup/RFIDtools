package cn.rrg.com;

import java.io.IOException;
import java.io.Serializable;

public interface Device extends Serializable {
    //设备测试连通性!
    boolean working() throws IOException;

    //设备关闭!
    boolean close() throws IOException;
}
