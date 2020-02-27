package cn.rrg.rdv.models;

import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import cn.rrg.rdv.callback.FormatConvertCallback;
import cn.rrg.rdv.util.DumpUtils;

public class FormatConvertModel {

    private static String LOG_TAG = FormatConvertModel.class.getSimpleName();

    //提供类型检测
    public static void checkType(String path, FormatConvertCallback.TypeCheckCallback callback) {
        //构建文件
        File file = new File(path);
        //判断其存在并且是文件
        if (file.exists() && file.isFile()) {
            //判断长度
            if (file.length() > (1024 * 1024)) {
                callback.isNot();
                return;
            }
            byte[] buf = new byte[(int) file.length()];
            //建立IO
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(file);
                fis.read(buf);
                int result = DumpUtils.getType(buf);
                switch (result) {
                    case DumpUtils.TYPE_TXT:
                        callback.isTxt();
                        break;

                    case DumpUtils.TYPE_BIN:
                        callback.isBin();
                        break;

                    case DumpUtils.TYPE_NOT:
                        callback.isNot();
                        break;
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                callback.isNot();
            } catch (IOException e) {
                e.printStackTrace();
                callback.isNot();
            } finally {
                try {
                    if (fis != null)
                        fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //提供txt转bin
    public static void txt2Bin(String path, FormatConvertCallback.ConvertCallback callback) {
        //构建文件
        File file = new File(path);
        //判断其存在并且是文件
        if (file.exists() && file.isFile()) {
            //判断长度
            if (file.length() > (1024 * 1024)) {
                callback.onConvertFail("File too big.");
                return;
            }
            byte[] buf = new byte[(int) file.length()];
            //建立IO
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(file);
                fis.read(buf);
                //得到未经包装的16进制字符串!
                byte[] binData = DumpUtils.txt2Bin(buf);
                //回调到上层
                callback.onConvertSuccess(binData);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                callback.onConvertFail(e.getCause().toString());
            } catch (IOException e) {
                e.printStackTrace();
                callback.onConvertFail(e.getCause().toString());
            } finally {
                try {
                    if (fis != null)
                        fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //提供bin转txt
    public static void bin2Txt(String path, FormatConvertCallback.ConvertCallback callback) {
        //构建文件
        File file = new File(path);
        //判断其存在并且是文件
        if (file.exists() && file.isFile()) {
            //判断长度
            if (file.length() > (1024 * 1024)) {
                callback.onConvertFail("File too big.");
                return;
            }
            byte[] buf = new byte[(int) file.length()];
            //建立IO
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(file);
                fis.read(buf);
                //得到未经包装的16进制字符串!
                byte[] hexData = DumpUtils.bin2Txt(buf);
                //回调传输到上层
                callback.onConvertSuccess(hexData);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                callback.onConvertFail(e.getCause().toString());
            } catch (IOException e) {
                e.printStackTrace();
                callback.onConvertFail(e.getCause().toString());
            } finally {
                try {
                    if (fis != null)
                        fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //提供保存为txt到指定路径
    public static void save2Txt(String path, String name, byte[] data, FormatConvertCallback.SaveCallback callback) {
        File dir = new File(path);
        Log.d(LOG_TAG, "path: " + path);
        if (dir.exists() && dir.isDirectory()) {
            //保存为文本的时候需要加以修饰，修饰为指定的格式
            //保存为BIN的时候直接写入!
            File _out = new File(dir.getAbsolutePath() + "/" + name);
            Log.d(LOG_TAG, "测试输出要保存的地址: " + _out.getAbsolutePath());
            if (_out.exists()) {
                callback.onSaveFail("File exists，auto delete!");
                if (!_out.delete()) {
                    callback.onSaveFail("delete failed!");
                    return;
                }
            }
            FileOutputStream fos = null;
            try {
                String decorate = DumpUtils.decorate(data);
                if (decorate == null) {
                    callback.onSaveFail("Decorate to MCT format failed!");
                    return;
                }
                //创建文件后进行写入操作
                if (_out.createNewFile()) {
                    //写入
                    fos = new FileOutputStream(_out);
                    fos.write(decorate.getBytes());
                    callback.onSaveSuccess();
                } else {
                    callback.onSaveFail("create file failed!");
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (fos != null)
                        fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        } else {
            callback.onSaveFail("path error!");
        }
    }

    //提供保存为bin到指定路径
    public static void save2Bin(String path, String name, byte[] data, FormatConvertCallback.SaveCallback callback) {
        File dir = new File(path);
        Log.d(LOG_TAG, "path: " + path);
        if (dir.exists() && dir.isDirectory()) {
            //保存为BIN的时候直接写入!
            File _out = new File(dir.getPath() + "/" + name);
            Log.d(LOG_TAG, "测试输出要保存的地址: " + _out.getAbsolutePath());
            if (_out.exists()) {
                callback.onSaveFail("File exists，auto delete!");
                if (!_out.delete()) {
                    callback.onSaveFail("delete failed!");
                    return;
                }
            }
            FileOutputStream fos = null;
            try {
                //创建文件后进行写入操作
                if (_out.createNewFile()) {
                    //写入
                    fos = new FileOutputStream(_out);
                    fos.write(data);
                    callback.onSaveSuccess();
                } else {
                    callback.onSaveFail("File create failed!");
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (fos != null)
                        fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            callback.onSaveFail("path error");
        }
    }
}
