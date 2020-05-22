package cn.dxl.common.util;

import android.content.Context;
import android.content.res.AssetManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

/*
 * 针对Assets进行操作的类!
 * */
public class AssetsUtil {

    private Context context;
    private AssetManager manager;

    public AssetsUtil(Context context) {
        this.context = context;
        manager = context.getAssets();
    }

    // 得到 assets指定目录下的所有文件!
    public String[] getFiles(String root) {
        String[] files = new String[0];
        try {
            files = manager.list(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (files == null || files.length == 0) return null;
        return files;
    }

    // 复制 assets中的文件到指定目录
    public boolean copyFile(String asFile, String targetPath) {
        File file = new File(targetPath);
        if (!FileUtils.createFile(file)) return false;
        boolean ret = false;
        //移动默认key文件
        AssetManager am = manager;
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

    public boolean isFile(String path) {
        try {
            IOUtils.close(manager.open(path));
            return true;
        } catch (IOException e) {
            // e.printStackTrace();
        }
        return false;
    }

    public void copyDirs(File sourcePath, File targetPath) {
        // create target path!
        final String rawPath = targetPath.getPath();
        targetPath = new File(targetPath + File.separator + sourcePath.getPath());
        FileUtils.createPaths(targetPath);
        // list files!
        String[] files = getFiles(sourcePath.getPath());
        if (files == null) {
            return;
        }
        // copy files!
        for (String fileStr : files) {
            File file = new File(sourcePath + File.separator + fileStr);
            // if is file, we need copy to target path!
            if (isFile(file.getPath())) {
                copyFile(file.getPath(), targetPath.getPath() + File.separator + file.getName());
            } else {
                copyDirs(file, new File(rawPath));
            }
        }
    }
}
