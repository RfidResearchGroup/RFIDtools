package cn.rrg.rdv.models;

import java.io.File;
import java.io.IOException;

import cn.dxl.common.util.FileUtils;
import cn.rrg.rdv.callback.FileReadLineCallback;

public class FileReadLineModel {

    /*
     * 读取文件以行，以回调结束!
     * */
    public static void readFile(File file, FileReadLineCallback callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String[] ret = FileUtils.readLines(file);
                    callback.onReadFinish(ret);
                } catch (IOException e) {
                    e.printStackTrace();
                    callback.onReadFail(e.getMessage());
                }
            }
        }).start();
    }
}
