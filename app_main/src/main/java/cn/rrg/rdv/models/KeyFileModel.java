package cn.rrg.rdv.models;

import java.io.File;
import java.io.IOException;

import cn.dxl.common.util.FileUtils;
import cn.rrg.rdv.callback.KeyFileCallbak;
import cn.rrg.rdv.util.DumpUtils;
import cn.rrg.rdv.util.Paths;
import cn.dxl.common.util.StringUtil;

/*
 *MVP中数据层，负责数据获取
 */
public class KeyFileModel {

    //从文件中读取密钥并且回调中介接口
    public static void getKeyString(String file, final KeyFileCallbak.KeyFileReadCallbak callback) {
        final String[] keyList;
        try {
            keyList = FileUtils.readLines(new File(file));
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        if (keyList == null || keyList.length <= 0) {
            callback.onReadFail();
            return;
        }
        new Thread(() -> {
            StringBuilder keyBuf = new StringBuilder();
            for (String key : keyList
            ) {
                keyBuf.append(key);
                keyBuf.append('\n');
            }
            callback.onReadSuccess(keyBuf.toString());
        }).start();
    }

    //写数据到密钥文件中并且回调中介接口
    public static void setKeyString(String keyStr, String file,
                                    final KeyFileCallbak.KeyFileWriteCallbak callback) {
        //分解编辑板密码字符串
        String[] keyArray = keyStr.split("\n");
        //遍历数组中的字符串
        for (String str : keyArray
        ) {
            //如果是注释或者空行则跳过检测,否则检测格式
            if (!DumpUtils.isAnnotaion(str) && !StringUtil.isEmpty(str)) {
                //判断输入的密钥格式是否正确，不正确直接回调而后退出循环
                if (!DumpUtils.isKeyFormat(str)) {
                    callback.onWriteFail();
                    return;
                }
            }
        }
        new Thread(() -> {
            //写文件并且回调结果
            File _tmpFile = new File(file);
            if (_tmpFile.exists() && _tmpFile.isFile()) {
                FileUtils.writeString(_tmpFile, keyStr, false);
                callback.onWriteSuccess("Success");
            } else {
                callback.onWriteFail();
            }
        }).start();
    }

    //创建密钥文件
    public static void createKeyFile(String name,
                                     final KeyFileCallbak.KeyFileCreateCallback callback) {
        File file = new File(Paths.KEY_DIRECTORY + "/" + name);
        try {
            if (file.createNewFile()) {
                callback.onCreateSuccess();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
