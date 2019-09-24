package cn.rrg.rdv.models;

import java.io.File;
import java.io.IOException;

import cn.rrg.rdv.callback.DumpCallback;
import cn.rrg.rdv.util.DumpUtils;
import cn.dxl.common.util.FileUtil;

/*
 * 负责dump底层数据操作,mvc中属于M层
 */
public class DumpModel {

    /*
     * log tag
     */
    private static final String LOG_TAG = DumpModel.class.getSimpleName();

    /*
     * 从选取的文件中得到的数组
     * */
    public static void showContents(File txtFile, DumpCallback callback) {
        //得到读取到的字节数组
        byte[] bs = new byte[0];
        try {
            bs = FileUtil.readBytes(txtFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (bs == null) {
            callback.onFileException();
            return;
        }
        String[] ss;
        //调用方法解析文本文件为无修饰的字符串
        ss = DumpUtils.undecorate(bs);
        if (ss != null) {
            callback.showContents(ss);
            return;
        }
        //调用方法解析二进制文件为字符串!
        byte[] tmpDataHexBytes = DumpUtils.bin2Txt(bs);
        if (tmpDataHexBytes != null) {
            ss = DumpUtils.splitDump(new String(tmpDataHexBytes));
            callback.showContents(ss);
            return;
        }
        //无法解析，报错提醒!
        callback.onFormatNoSupport();
    }

    /*
     *  直接接受传过来的字符串数组
     * */
    public static void showContents(String[] datas, DumpCallback callback) {
        if (datas != null) {
            callback.showContents(datas);
        }
    }

    /*
     * 保存内容
     */
    public static void saveDumpModify(DumpCallback callback, String[] src, File dump) {
        if (dump != null) {
            StringBuilder sb = new StringBuilder();
            if (src == null) {
                callback.onFormatNoSupport();
                return;
            }
            for (int i = 0; i < src.length; ++i) {
                //检查格式
                if (!DumpUtils.isBlockData(src[i])) {
                    callback.onFormatNoSupport();
                    return;
                }
                if (i == (src.length - 1)) {
                    //结尾，不需要换行
                    sb.append(src[i]);
                } else {
                    //需要换行!
                    sb.append(src[i]).append('\n');
                }
            }
            /*
             * 进行新文件的建立,或者与老文件的映射
             * */
            //当前判断，如果文件不存在，则创建文件!
            if (!dump.exists()) {
                try {
                    if (!dump.createNewFile()) {
                        callback.onFileException();
                        return;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
            }
            //包装后再保存
            String decorate = DumpUtils.decorate(sb.toString().getBytes());
            if (decorate == null) {
                callback.onFormatNoSupport();
                return;
            }
            //将数组写进文件中
            FileUtil.writeString(dump, decorate, false);
            callback.onSuccess();
        } else {
            callback.onFileException();
        }
    }
}
