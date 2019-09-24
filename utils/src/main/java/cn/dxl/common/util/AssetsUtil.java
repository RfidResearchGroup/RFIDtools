package cn.dxl.common.util;

import android.content.Context;
import android.content.res.AssetManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/*
 * 针对Assets进行操作的类!
 * */
public class AssetsUtil {

    private Context context;

    public AssetsUtil(Context context) {
        this.context = context;
    }

    //判断assets文件是否存在!
    public boolean isFileExists(String fileName) {
        InputStream is = null;
        boolean ret = false;
        try {
            is = context.getResources().getAssets().open(fileName);
            ret = true;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (is != null)
                    is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return ret;
    }

    //得到assets指定目录下的所有文件!
    public String[] getFiles(String root) {
        String[] files = new String[0];
        try {
            files = context.getAssets().list(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (files == null || files.length == 0) return null;
        return files;
    }

    //移动assets中的文件到指定目录
    public boolean moveFile(String asFile, String targetPath) {
        File file = new File(targetPath);
        if (!file.exists() || !file.isFile()) {
            try {
                if (!file.createNewFile()) {
                    return false;
                }
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        boolean ret = false;
        //移动默认key文件
        AssetManager am = context.getResources().getAssets();
        InputStream is = null;
        FileOutputStream fos = null;
        try {
            is = am.open(asFile);
            fos = new FileOutputStream(file);
            int len = is.available();
            for (int i = 0; i < len; ++i) {
                fos.write((byte) is.read());
            }
            ret = true;
        } catch (IOException e) {
            e.printStackTrace();
            try {
                if (is != null) is.close();
                if (fos != null) fos.close();
                ret = false;
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        } finally {
            try {
                if (is != null) is.close();
                if (fos != null) fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return ret;
    }
}
