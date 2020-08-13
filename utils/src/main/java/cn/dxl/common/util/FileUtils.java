package cn.dxl.common.util;

import android.app.Application;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.loader.content.CursorLoader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.text.DecimalFormat;
import java.util.ArrayList;

import cn.dxl.common.R;

/*
 * 一系列文件操作的封装!
 * */
public class FileUtils {

    // 上下文，用于给File类获取资源!
    private static Application context = AppUtil.getInstance().getApp();

    private static DecimalFormat format = new DecimalFormat("#.00");

    public interface Size {
        long B = 1024;
        long KB = B * 1024;
        long MB = KB * 1024;
        long GB = MB * 1024;
        long TB = GB * 1024;
    }

    public interface Name {
        String B = "B";
        String KB = "KB";
        String MB = "MB";
        String GB = "GB";
        String TB = "TB";
    }

    //获得文件的输入流!
    public static FileInputStream getFis(File file) throws FileNotFoundException {
        return new FileInputStream(file);
    }

    //获得文件的输入流!
    public static InputStream getFis(Uri stream) throws FileNotFoundException {
        return context.getContentResolver().openInputStream(stream);
    }

    //获得文件的输出流!
    public static FileOutputStream getFos(File file, boolean append) throws FileNotFoundException {
        return new FileOutputStream(file, append);
    }

    public static byte[] readBytes(InputStream is, long count, long max, long min) throws IOException {
        if (max != -1 && is.available() > max) return null;
        if (min != -1 && is.available() < min) return null;
        // The length correct.
        if (count == -1) count = is.available();
        byte[] buf = new byte[(int) count];
        if (is.read(buf, 0, (int) count) != count) {
            IOUtils.close(is);
            throw new IOException("read bytes file len is error!");
        }
        IOUtils.close(is);
        return buf;
    }

    public static byte[] readBytes(File file) throws IOException {
        return readBytes(file, (int) file.length());
    }

    public static byte[] readBytes(File file, long count) throws IOException {
        FileInputStream fis = getFis(file);
        return readBytes(fis, count, -1, -1);
    }

    public static byte[] readBytes(File file, long count, long max, long min) throws IOException {
        FileInputStream fis = getFis(file);
        return readBytes(fis, count, max, min);
    }

    public static byte[] readBytes(Uri uri) throws IOException {
        return readBytes(uri, -1);
    }

    public static byte[] readBytes(Uri uri, long count) throws IOException {
        InputStream fis = getFis(uri);
        return readBytes(fis, count, -1, -1);
    }

    public static byte[] readBytes(Uri uri, long count, long max, long min) throws IOException {
        InputStream fis = getFis(uri);
        return readBytes(fis, count, max, min);
    }

    //读取文件以每行
    public static String[] readLines(File file) throws IOException {
        FileInputStream fileIs;
        ArrayList<String> resultList = new ArrayList<>();
        fileIs = new FileInputStream(file);
        String tmpBUf;
        //创建字符缓冲读取对象
        BufferedReader bReader = new BufferedReader(new InputStreamReader(fileIs));
        while ((tmpBUf = bReader.readLine()) != null) {
            resultList.add(tmpBUf);
        }
        return ArrayUtils.list2Arr(resultList);
    }

    //按匹配的行读取文件
    public static String[] readLines(File file, String regex) throws IOException {
        String[] resultArray = readLines(file);
        ArrayList<String> resultList = new ArrayList<>();
        if (resultArray == null) return new String[0];
        for (String str : resultArray) {
            if (str.matches(regex)) {
                resultList.add(str);
            }
        }
        return ArrayUtils.list2Arr(resultList);
    }

    //写入指定的字节到文件中，以覆盖或者追加的方式!
    public static void writeBytes(byte[] data, File file, boolean append) throws IOException {
        if (!file.exists()) createFile(file);
        if (!file.isFile()) return;
        LogUtils.d("写入的目标: " + file.getAbsolutePath());
        FileOutputStream fos = getFos(file, append);
        fos.write(data);
    }

    //写入指定的字节到文件中，以覆盖或者追加的方式!
    public static void writeBytes(byte[] data, FileOutputStream fos) throws IOException {
        fos.write(data);
    }

    // 写入指定的字节到文件中，适配SAF
    public static void writeBytes(byte[] data, Uri target) throws IOException {
        OutputStream os = context.getContentResolver().openOutputStream(target);
        if (os != null) {
            os.write(data);
            os.close();
        }
    }

    // 写入指定的字节到文件中，适配SAF
    public static void writeBytes(File source, Uri target) throws IOException {
        writeBytes(readBytes(Uri.fromFile(source)), target);
    }

    //使用字符串内容覆盖文件内容
    public static void writeString(File file, String str, boolean append) {
        try {
            writeBytes(str.getBytes(), file, append);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 根据文件后缀名获得对应的MIME类型。
    private static String getMimeType(String filePath) {
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        String mime = "*/*";
        if (filePath != null) {
            try {
                mmr.setDataSource(filePath);
                mime = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE);
            } catch (IllegalStateException e) {
                return mime;
            } catch (IllegalArgumentException e) {
                return mime;
            } catch (RuntimeException e) {
                return mime;
            }
        }
        return mime;
    }

    public static boolean isFile(String file) {
        return new File(file).isFile();
    }

    //删除文件夹目录下所有的文件
    public static boolean delete(File dir) {
        if (dir.isDirectory()) {
            if (dir.delete()) {
                return false;
            }
            String[] files = dir.list();
            if (files == null) {
                return dir.delete();
            }
            //非空目录，迭代删除所有的文件
            for (String file : files)
                delete(new File(dir.getAbsolutePath() + File.separator + file));
            //然后进行目录删除!
            return delete(dir);
        } else {
            return dir.delete();
        }
    }

    public static boolean move(File file, File path) {
        String pathStr = path.getAbsolutePath();
        pathStr = pathStr.endsWith(File.separator) ? pathStr : pathStr + File.separator;
        String targetFile = pathStr + file.getName();
        File tmp = new File(targetFile);
        createFile(tmp);
        if (tmp.exists() && tmp.isFile()) {
            // 开始写入!
            return file.renameTo(new File(targetFile));
        }
        return true;
    }

    public static boolean copy(Uri source, File target) {
        return copy(source, Uri.fromFile(target));
    }

    public static boolean copy(Uri source, Uri target) {
        try {
            byte[] data = readBytes(source);
            writeBytes(data, target);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean copy(File source, File target) {
        if (target == null || source == null) return false;
        FileChannel inputChannel = null;
        FileChannel outputChannel = null;
        try {
            File parentFile = target.getParentFile();
            if (parentFile != null) {
                createPaths(parentFile);
            }
            createFile(target);
            inputChannel = new FileInputStream(source).getChannel();
            outputChannel = new FileOutputStream(target).getChannel();
            outputChannel.transferFrom(inputChannel, 0, inputChannel.size());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            IOUtils.close(inputChannel);
            IOUtils.close(outputChannel);
        }
        return true;
    }

    public static boolean copy(FileDescriptor descriptor, FileDescriptor target) {
        if (target == null || descriptor == null) return false;
        FileChannel inputChannel = null;
        FileChannel outputChannel = null;
        try {
            inputChannel = new FileInputStream(descriptor).getChannel();
            outputChannel = new FileOutputStream(target).getChannel();
            outputChannel.transferFrom(inputChannel, 0, inputChannel.size());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            IOUtils.close(inputChannel);
            IOUtils.close(outputChannel);
        }
        return true;
    }

    public static boolean copy(byte[] rawData, File target) {
        try {
            writeBytes(rawData, target, false);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 調用系統方法分享文件
    public static void shareFile(File file) {
        LogUtils.d("shareFile path: " + (file != null ? file.toString() : null));
        if (null != file && file.exists()) {
            Intent share = new Intent(Intent.ACTION_SEND);
            share.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(
                    context, //上下文
                    getFileProviderName(), //内容提供者ID
                    file //要被分享的文件!
                    )
            );
            share.setType(getMimeType(file.getAbsolutePath()));//此处可发送多种文件
            share.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            //context.startActivity(Intent.createChooser(share, "分享文件")); 会崩溃!
            context.startActivity(share);
        } else {
            Toast.makeText(context, "分享的文件不存在!", Toast.LENGTH_SHORT).show();
        }
    }

    public final static String getFileProviderName() {
        return context.getPackageName() + ".fileprovider";
    }

    public static String getFilePathByUri(Uri uri) {
        // 以 file:// 开头的
        if (ContentResolver.SCHEME_FILE.equals(uri.getScheme())) {
            return uri.getPath();
        }
        // 以/storage开头的也直接返回
        if (isOtherDocument(uri)) {
            return uri.getPath();
        }
        // 版本兼容的获取！
        String path = getFilePathByUri_BELOWAPI11(uri);
        if (path != null) {
            LogUtils.d("getFilePathByUri_BELOWAPI11获取到的路径为：" + path);
            return path;
        }
        path = getFilePathByUri_API11to18(uri);
        if (path != null) {
            LogUtils.d("getFilePathByUri_API11to18获取到的路径为：" + path);
            return path;
        }
        path = getFilePathByUri_API19(uri);
        LogUtils.d("getFilePathByUri_API19获取到的路径为：" + path);
        return path;
    }

    private static String getFilePathByUri_BELOWAPI11(Uri uri) {
        // 以 content:// 开头的，比如 content://media/extenral/images/media/17766
        try {
            if (ContentResolver.SCHEME_CONTENT.equals(uri.getScheme())) {
                String path = null;
                String[] projection = new String[]{MediaStore.Images.Media.DATA};
                Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        try {
                            int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                            if (columnIndex > -1) {
                                path = cursor.getString(columnIndex);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    cursor.close();
                }
                return path;
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }

    private static String getFilePathByUri_API11to18(Uri contentUri) {
        String result = null;
        try {
            String[] projection = {MediaStore.Images.Media.DATA};

            CursorLoader cursorLoader = new CursorLoader(context, contentUri, projection, null, null, null);
            Cursor cursor = cursorLoader.loadInBackground();
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    try {
                        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                        result = cursor.getString(column_index);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                cursor.close();
            }
        } catch (Exception e) {
            return null;
        }
        return result;
    }

    private static String getFilePathByUri_API19(Uri uri) {
        // 4.4及之后的 是以 content:// 开头的，比如 content://com.android.providers.media.documents/document/image%3A235700
        // content://com.tencent.mtt.fileprovider/QQBrowser/tencent/MobileQQ/photo/_-176177031_Screenshot_20200225_213619_com.tencent.mobileqq_1582637779000_wifi_0.jpg
        if (ContentResolver.SCHEME_CONTENT.equals(uri.getScheme()) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (DocumentsContract.isDocumentUri(context, uri)) {
                if (isExternalStorageDocument(uri)) {
                    // ExternalStorageProvider
                    String docId = DocumentsContract.getDocumentId(uri);
                    String[] split = docId.split(":");
                    String type = split[0];
                    if ("primary".equalsIgnoreCase(type)) {
                        if (split.length > 1) {
                            return Environment.getExternalStorageDirectory() + "/" + split[1];
                        } else {
                            return Environment.getExternalStorageDirectory() + "/";
                        }
                        // This is for checking SD Card
                    }
                } else if (isDownloadsDocument(uri)) {
                    //下载内容提供者时应当判断下载管理器是否被禁用
                    int stateCode = context.getPackageManager().getApplicationEnabledSetting("com.android.providers.downloads");
                    if (stateCode != 0 && stateCode != 1) {
                        return null;
                    }
                    String id = DocumentsContract.getDocumentId(uri);
                    // 如果出现这个RAW地址，我们则可以直接返回!
                    if (id.startsWith("raw:")) {
                        return id.replaceFirst("raw:", "");
                    }
                    if (id.contains(":")) {
                        String[] tmp = id.split(":");
                        if (tmp.length > 1) {
                            id = tmp[1];
                        }
                    }
                    Uri contentUri = Uri.parse("content://downloads/public_downloads");
                    LogUtils.d("测试打印Uri: " + uri);
                    try {
                        contentUri = ContentUris.withAppendedId(contentUri, Long.parseLong(id));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    String path = getDataColumn(contentUri, null, null);
                    if (path != null) return path;
                    // 兼容某些特殊情况下的文件管理器!
                    String fileName = getFileNameByUri(context, uri, true);
                    if (fileName != null) {
                        path = Environment.getExternalStorageDirectory().toString() + "/Download/" + fileName;
                        return path;
                    }
                } else if (isMediaDocument(uri)) {
                    // MediaProvider
                    String docId = DocumentsContract.getDocumentId(uri);
                    String[] split = docId.split(":");
                    String type = split[0];
                    Uri contentUri = null;
                    if ("image".equals(type)) {
                        contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                    } else if ("video".equals(type)) {
                        contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                    } else if ("audio".equals(type)) {
                        contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                    }
                    String selection = "_id=?";
                    String[] selectionArgs = new String[]{split[1]};
                    return getDataColumn(contentUri, selection, selectionArgs);
                }
            }
        }
        return null;
    }

    public static String getFileNameByUri(Context context, Uri uri, boolean needPath) {
        String relativePath = getFileRelativePathByUri_API18(context, uri);
        if (relativePath == null) relativePath = "";
        try (Cursor cursor = context.getContentResolver().query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME);
                String result = cursor.getString(index);
                return needPath ? relativePath + result : result;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String getFileRelativePathByUri_API18(Context context, Uri uri) {
        final String[] projection;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            projection = new String[]{
                    MediaStore.MediaColumns.RELATIVE_PATH
            };
            try (Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.RELATIVE_PATH);
                    return cursor.getString(index);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private static String getDataColumn(Uri uri, String selection, String[] selectionArgs) {
        final String column = MediaStore.Images.Media.DATA;
        final String[] projection = {column};
        try (Cursor cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    private static boolean isOtherDocument(Uri uri) {
        // 以/storage开头的也直接返回
        if (uri != null && uri.getPath() != null) {
            String path = uri.getPath();
            if (path.startsWith("/storage")) {
                return true;
            }
            if (path.startsWith("/external_files")) {
                return true;
            }
        }
        return false;
    }

    private static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    private static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    public static boolean createFile(File file) {
        try {
            File tmpParent = file.getParentFile();
            if (tmpParent != null && !tmpParent.exists()) {
                createPaths(tmpParent);
            }
            if (!file.exists())
                if (file.createNewFile()) return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean createPaths(File file) {
        if (!file.exists())
            return file.mkdirs();
        return false;
    }

    public static File[] names2Files(String[] fileStrs) {
        ArrayList<File> tmp = new ArrayList<>();
        for (String s : fileStrs) {
            tmp.add(new File(s));
        }
        return tmp.toArray(new File[0]);
    }

    public static String[] files2Names(File[] files) {
        ArrayList<String> ret = new ArrayList<>();
        if (files != null) {
            for (File f : files) {
                ret.add(f.getName());
            }
        }
        return ret.toArray(new String[0]);
    }

    public static File getAppFilesDir() {
        if (context != null)
            return context.getFilesDir();
        return null;
    }

    public static File getAppFilesDir(String sub) {
        File path = context.getFileStreamPath(sub);
        createPaths(path);
        return path;
    }

    public static File newFile(File dir, String name) {
        createPaths(dir);
        File ret = new File(dir.getAbsoluteFile() + File.separator + name);
        createFile(ret);
        return ret;
    }

    public static boolean isValidFileName(String name) {
        return !StringUtil.isEmpty(name) && !name.contains("/");
    }

    public static String getNativePath() {
        String ss = context.getApplicationInfo().nativeLibraryDir;
        if (ss == null)
            ss = context.getFilesDir().getPath() + "/lib";
        return ss;
    }

    public static String getFileCountIfFolder(File[] files) {
        if (files == null) files = new File[0];
        return files.length + " " + context.getString(R.string.item);
    }

    public static String getFileLengthIfFile(@NonNull File file) {
        long length = file.length();
        String fileLength;
        if (length < Size.B) {
            fileLength = length + Name.B;
        } else if (length < Size.KB) {
            fileLength = format.format(length * 1.0 / Size.B) + Name.KB;
        } else if (length < Size.MB) {
            fileLength = format.format(length * 1.0 / Size.KB) + Name.MB;
        } else if (length < Size.GB) {
            fileLength = format.format(length * 1.0 / Size.MB) + Name.GB;
        } else {
            fileLength = format.format(length * 1.0 / Size.GB) + Name.TB;
        }
        return fileLength;
    }
}
