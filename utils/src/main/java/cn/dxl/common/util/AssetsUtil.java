package cn.dxl.common.util;

import android.content.Context;
import android.content.res.AssetManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/*
 * 针对Assets进行操作的类!
 * */
public class AssetsUtil {

    public static String[] getFiles(Context context, String root) {
        String[] files = new String[0];
        AssetManager manager = context.getAssets();
        try {
            files = manager.list(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (files == null || files.length == 0) return null;
        return files;
    }

    public static boolean copyFile(Context context, String asFile, String targetPath) {
        File file = new File(targetPath);
        if (!FileUtils.createFile(file)) return false;
        boolean ret = false;
        AssetManager am = context.getAssets();
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

    public static boolean isFile(Context context, String path) {
        AssetManager am = context.getAssets();
        try {
            IOUtils.close(am.open(path));
            return true;
        } catch (IOException e) {
            // e.printStackTrace();
        }
        return false;
    }

    public static void copyDirs(Context context, File sourcePath, File targetPath) {
        // create target path!
        final String rawPath = targetPath.getPath();
        targetPath = new File(targetPath + File.separator + sourcePath.getPath());
        FileUtils.createPaths(targetPath);
        // list files!
        String[] files = getFiles(context, sourcePath.getPath());
        if (files == null) {
            return;
        }
        // copy files!
        for (String fileStr : files) {
            File file = new File(sourcePath + File.separator + fileStr);
            // if is file, we need copy to target path!
            if (isFile(context, file.getPath())) {
                copyFile(context, file.getPath(), targetPath.getPath() + File.separator + file.getName());
            } else {
                copyDirs(context, file, new File(rawPath));
            }
        }
    }

    public static String readLines(Context context, String file) {
        StringBuilder builder = new StringBuilder();
        AssetManager am = context.getAssets();
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(am.open(file)));
            String line;
            while ((line = br.readLine()) != null) builder.append(line);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return builder.toString();
    }
}
