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

    private AssetManager manager;

    public AssetsUtil(Context context) {
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
        try (FileOutputStream fos = new FileOutputStream(file)) {
            byte[] buffer = new byte[1024 * 1024];
            is = am.open(asFile);
            int len;
            while ((len = is.read(buffer)) != -1) {
                fos.write(buffer, 0, len);
                fos.flush();
            }
            ret = true;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (is != null) is.close();
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
