package cn.dxl.common.util;

import java.io.Closeable;
import java.io.IOException;

public class IOUtils {
    public static boolean close(Closeable closeable) {
        try {
            closeable.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}
