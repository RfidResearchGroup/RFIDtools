package cn.dxl.common.util;

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

import androidx.core.content.FileProvider;

import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;

/*
 * 一系列文件操作的封装!
 * */
public class FileUtil {

    //获得文件的输入流!
    public static FileInputStream getFis(File file) throws FileNotFoundException {
        return new FileInputStream(file);
    }

    //获得文件的输出流!
    public static FileOutputStream getFos(File file, boolean append) throws FileNotFoundException {
        return new FileOutputStream(file, append);
    }

    //读取全部字节从文件中!!!
    public static byte[] readBytes(File file) throws IOException {
        FileInputStream fis = getFis(file);
        byte[] buf = new byte[(int) file.length()];
        if (fis.read(buf) != file.length()) {
            close(fis);
            throw new IOException("read bin file len is error!");
        }
        //谨记关闭流!
        close(fis);
        return buf;
    }

    //读取全部的字节从流中!
    public static byte[] readBytes(FileInputStream fis) throws IOException {
        byte[] buf = new byte[fis.available()];
        if (fis.read(buf) != buf.length) throw new IOException("read bin file len is error!");
        return buf;
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
        return MyArrays.list2Arr(resultList);
    }

    //按行读取密钥文件
    public static String[] readLines(File keyFile, String regex) throws IOException {
        String[] resultArray = readLines(keyFile);
        ArrayList<String> resultList = new ArrayList<>();
        if (resultArray == null) return new String[0];
        for (String str : resultArray) {
            if (str.matches(regex)) {
                resultList.add(str);
            }
        }
        return MyArrays.list2Arr(resultList);
    }

    //写入指定的字节到文件中，以覆盖或者追加的方式!
    public static void writeBytes(byte[] data, File file, boolean append) throws IOException {
        FileOutputStream fos = getFos(file, append);
        fos.write(data);
    }

    //写入指定的字节到文件中，以覆盖或者追加的方式!
    public static void writeBytes(byte[] data, FileOutputStream fos) throws IOException {
        fos.write(data);
    }

    //使用字符串内容覆盖文件内容
    public static void writeString(File file, String str, boolean append) {
        try {
            writeBytes(str.getBytes(), file, append);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //关闭文件输入流!
    public static void close(InputStream is) {
        if (is != null) {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //关闭文件输出流!
    public static void close(OutputStream os) {
        if (os != null) {
            try {
                //刷新缓冲区后
                os.flush();
                //再关闭!
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //关闭套接字
    public static void close(Socket socket) {
        if (socket != null) {
            try {
                //再关闭!
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
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

    //删除文件夹目录下所有的文件
    public static void delete(File dir) {
        if (dir.isDirectory()) {
            if (dir.delete()) {
                return;
            }
            String[] files = dir.list();
            if (files == null) {
                dir.delete();
                return;
            }
            //非空目录，迭代删除所有的文件
            for (String file : files)
                delete(new File(dir.getAbsolutePath() + File.separator + file));
            //然后进行目录删除!
            delete(dir);
        } else {
            dir.delete();
        }
    }

    // 調用系統方法分享文件
    public static void shareFile(Context context, File file) {
        if (null != file && file.exists()) {
            Intent share = new Intent(Intent.ACTION_SEND);
            share.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(
                    context, //上下文
                    getFileProviderName(context), //内容提供者ID
                    file //要被分享的文件!
                    )
            );
            share.setType(getMimeType(file.getAbsolutePath()));//此处可发送多种文件
            share.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            context.startActivity(Intent.createChooser(share, "分享文件"));
        } else {
            Toast.makeText(context, "分享的文件不存在!", Toast.LENGTH_SHORT).show();
        }
    }

    public final static String getFileProviderName(Context context) {
        return context.getPackageName() + ".fileprovider";
    }

    public static String getFilePathByUri(Context context, Uri uri) {
        String path = null;
        // 以 file:// 开头的
        if (ContentResolver.SCHEME_FILE.equals(uri.getScheme())) {
            path = uri.getPath();
            return path;
        }
        // 以/storage开头的也直接返回
        if (isOtherDocument(uri)) {
            path = uri.getPath();
            return path;
        }
        // 以 content:// 开头的，比如 content://media/extenral/images/media/17766
        if (ContentResolver.SCHEME_CONTENT.equals(uri.getScheme()) && Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            Cursor cursor = context.getContentResolver().query(uri, new String[]{MediaStore.Images.Media.DATA}, null, null, null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                    if (columnIndex > -1) {
                        path = cursor.getString(columnIndex);
                    }
                }
                cursor.close();
            }
            return path;
        }
        // 4.4及之后的 是以 content:// 开头的，比如 content://com.android.providers.media.documents/document/image%3A235700
        if (ContentResolver.SCHEME_CONTENT.equals(uri.getScheme()) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (DocumentsContract.isDocumentUri(context, uri)) {
                if (isExternalStorageDocument(uri)) {
                    // ExternalStorageProvider
                    final String docId = DocumentsContract.getDocumentId(uri);
                    final String[] split = docId.split(":");
                    final String type = split[0];
                    if ("primary".equalsIgnoreCase(type)) {
                        path = Environment.getExternalStorageDirectory() + "/" + split[1];
                        return path;
                    }
                } else if (isDownloadsDocument(uri)) {
                    // DownloadsProvider
                    //下载内容提供者时应当判断下载管理器是否被禁用
                    int stateCode = context.getPackageManager().getApplicationEnabledSetting("com.android.providers.downloads");
                    if (stateCode != 0 && stateCode != 1) {
                        return null;
                    }
                    final String id = DocumentsContract.getDocumentId(uri);
                    final Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
                    path = getDataColumn(context, contentUri, null, null);
                    return path;
                } else if (isMediaDocument(uri)) {
                    // MediaProvider
                    final String docId = DocumentsContract.getDocumentId(uri);
                    final String[] split = docId.split(":");
                    final String type = split[0];
                    Uri contentUri = null;
                    if ("image".equals(type)) {
                        contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                    } else if ("video".equals(type)) {
                        contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                    } else if ("audio".equals(type)) {
                        contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                    }
                    final String selection = "_id=?";
                    final String[] selectionArgs = new String[]{split[1]};
                    path = getDataColumn(context, contentUri, selection, selectionArgs);
                    return path;
                }
            }
        }
        return null;
    }

    //判断文件名是否有效!
    public static boolean isValidFileName(String name) {
        return !StringUtil.isEmpty(name) && !name.contains("/");
    }

    //获得so库的地址!
    public static String getNativePath(Context context) {
        String ss = context.getApplicationInfo().nativeLibraryDir;
        if (ss == null)
            ss = context.getFilesDir().getPath() + "/lib";
        return ss;
    }

    private static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } catch (IllegalArgumentException iae) {
            iae.printStackTrace();
        } finally {
            if (cursor != null)
                cursor.close();
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
            if (!file.exists())
                if (file.createNewFile()) return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static File[] strings2Files(String[] fileStrs) {
        ArrayList<File> tmp = new ArrayList<>();
        for (String s : fileStrs) {
            tmp.add(new File(s));
        }
        return tmp.toArray(new File[0]);
    }
}
