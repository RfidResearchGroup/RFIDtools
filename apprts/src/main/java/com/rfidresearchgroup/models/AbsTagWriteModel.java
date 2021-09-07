package com.rfidresearchgroup.models;

import android.util.Log;

import com.rfidresearchgroup.util.DumpUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;

import com.rfidresearchgroup.common.util.HexUtil;
import com.rfidresearchgroup.mifare.MifareAdapter;
import com.rfidresearchgroup.mifare.MifareClassicUtils;
import com.rfidresearchgroup.javabean.M1KeyBean;
import com.rfidresearchgroup.javabean.M1Bean;
import com.rfidresearchgroup.callback.WriterCallback;

public abstract class AbsTagWriteModel
        extends AbsStopableTask {

    private String LOG_TAG = "AbsTagWriteModel";

    //倒叙排序!
    private Comparator<M1Bean> datasComparator = new Comparator<M1Bean>() {
        @Override
        public int compare(M1Bean o1, M1Bean o2) {
            int s1 = o1.getSector();
            int s2 = o2.getSector();
            //调用对比方法，如果相等则不变动，不进行排序，如果s2 > s1则返回正值，否则返回负值!
            return Integer.compare(s2, s1);
        }
    };

    // 秘钥也要一起排序!
    private Comparator<M1KeyBean> keysComparator = new Comparator<M1KeyBean>() {
        @Override
        public int compare(M1KeyBean o1, M1KeyBean o2) {
            int s1 = o1.getSector();
            int s2 = o2.getSector();
            //调用对比方法，如果相等则不变动，不进行排序，如果s2 > s1则返回正值，否则返回负值!
            return Integer.compare(s2, s1);
        }
    };

    // 初始化标签的读写封装的持有!!!
    protected abstract MifareAdapter getTag();

    // 判断标签是否可以连接！
    private boolean isTagCantCon(MifareAdapter tag, boolean needScan) {
        try {
            if (needScan && !tag.rescantag()) {
                stopLable = true;
                return true;
            }
            if (tag.connect()) {
                return false;
            } else {
                stopLable = true;
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
            stopLable = true;
            return true;
        }
    }

    //单块写封装
    private boolean writeBlock(boolean writeZero, MifareAdapter tag, int block, byte[] key, byte[] data, boolean useKeyB) {
        if (block == 0 && !writeZero) return false;
        boolean ret = false;
        boolean auth;
        int sector = MifareClassicUtils.blockToSector(block);
        try {
            if (useKeyB) {
                auth = tag.authB(sector, key);
            } else {
                auth = tag.authA(sector, key);
            }
            if (auth) {
                //密钥验证成功后，这里需要做一下对于写卡的实现!
                ret = tag.write(block, data);
            }
        } catch (IOException e) {
            e.printStackTrace();
            stopLable = true;
        }
        return ret;
    }

    //实现写单块!
    public void writeBlock(boolean writeZero, int block, M1KeyBean keys, String data, WriterCallback callback) {
        // 先判断能不能写这个块!
        if (!writeZero) {
            callback.onFinish();
            return;
        }
        MifareAdapter tag = getTag();
        //判断给出的密钥是否可以用来验证这个块
        if (MifareClassicUtils.blockToSector(block) != keys.getSector()) {
            Log.d(LOG_TAG, "给出的密钥bean不符合输入的block值!");
            callback.onFinish();
            return;
        }
        //判断给定的块数据是否可用!
        if (!DumpUtils.isValidBlockData(data)) {
            Log.d(LOG_TAG, "给出的块数据异常，请检查您的输入!");
            callback.onFinish();
            return;
        }
        //首先验证密钥
        byte[] keyA = HexUtil.hexStringToByteArray(keys.getKeyA());
        byte[] keyB = HexUtil.hexStringToByteArray(keys.getKeyB());
        //转换数据为字节
        byte[] dataBytes = HexUtil.hexStringToByteArray(data);
        //使用密钥A尝试写入
        if (!writeBlock(writeZero, tag, block, keyA, dataBytes, false)) {
            Log.d(LOG_TAG, "尝试使用A密钥写入块: " + block + "失败了!");
        }
        //使用密钥B尝试写入
        if (!writeBlock(writeZero, tag, block, keyB, dataBytes, true)) {
            Log.d(LOG_TAG, "尝试使用A密钥写入块: " + block + "失败了!");
        }
        callback.onFinish();
    }

    //实现以单扇区写普通卡
    private void writeSector(boolean writeZero, boolean order, M1Bean data, M1KeyBean keys, WriterCallback callback) {
        MifareAdapter tag = getTag();
        //根据传入的参数来写入，写两次，AB密钥各写一次，能写一次成功就算数!
        boolean retA;
        boolean retB;
        //解析得到Hex密钥字节数组
        byte[] keyA = HexUtil.hexStringToByteArray(keys.getKeyA());
        byte[] keyB = HexUtil.hexStringToByteArray(keys.getKeyB());
        //取出数据尝试写入!
        String[] datas = data.getDatas();
        //进一步验证数据和卡片类型的映射!
        if (datas.length != MifareClassicUtils.getBlockCountInSector(data.getSector())) {
            //数据长度跟卡片对应扇区的块数量不匹配!
            Log.d(LOG_TAG, "发现了致命的问题,传入的数据块数量与标签的块数量不匹配!");
            stopLable = true;
            callback.onDataInvalid();
            return;
        }
        //TODO 倒叙写实现与正序写实现!!!
        if (order) {
            //正序写!
            int sector = data.getSector();
            int startBlobk = MifareClassicUtils.sectorToBlock(sector);
            for (int i = 0, j = startBlobk; i < datas.length; ++i, ++j) {
                //Log.d(LOG_TAG, "写入块" + j);
                //TODO 当前是扇区顺写，数据也要是倒写的，因此，我们需要进行验证流程!!
                if (!DumpUtils.isValidBlockData(datas[i])) {
                    //数据格式异常!
                    Log.d(LOG_TAG, "该索引块数据异常: " + j + "\n" + datas[i]);
                    continue;
                }
                if (!DumpUtils.isValidBlockData(datas[i])) {
                    //数据格式异常!
                    Log.d(LOG_TAG, "该索引块数据异常: " + j + "\n" + datas[i]);
                    continue;
                }
                byte[] dataBytes = HexUtil.hexStringToByteArray(datas[i]);
                //拿A密钥写这个块!
                retA = writeBlock(writeZero, tag, j, keyA, dataBytes, false);
                if (j == 0) {
                    if (isTagCantCon(tag, true)) {
                        stopLable = true;
                        callback.onTagAbnormal();
                    }
                }
                //拿B密钥写这个块!
                retB = writeBlock(writeZero, tag, j, keyB, dataBytes, true);
                if (j == 0) {
                    if (isTagCantCon(tag, true)) {
                        stopLable = true;
                        callback.onTagAbnormal();
                    }
                }
                if (!retA && !retB) {
                    Log.d(LOG_TAG, "使用AB密钥写入块" + j + "失败!");
                }
                if (isTagCantCon(tag, false)) {
                    stopLable = true;
                    callback.onTagAbnormal();
                }
            }
        } else {
            int sector = data.getSector();
            int lastBlock = MifareClassicUtils.sectorToBlock(sector) + MifareClassicUtils.getBlockCountInSector(sector) - 1;
            for (int i = datas.length - 1, j = lastBlock; i >= 0; --i, --j) {
                //Log.d(LOG_TAG, "写入块" + j);
                //TODO 当前是扇区倒写，数据也要是倒写的，因此，我们需要进行验证流程!!
                if (!DumpUtils.isValidBlockData(datas[i])) {
                    //数据格式异常!
                    Log.d(LOG_TAG, "该索引块数据异常: " + j + "\n" + datas[i]);
                    continue;
                }
                byte[] dataBytes = HexUtil.hexStringToByteArray(datas[i]);
                //当前处于每个扇区的尾部块，操作前需要进行验证,然而，改写了尾部块块后我们需要重新验证，因此，一直开放验证操作
                //拿A密钥写这个块!
                retA = writeBlock(writeZero, tag, j, keyA, dataBytes, false);
                //拿B密钥写这个块!
                retB = writeBlock(writeZero, tag, j, keyB, dataBytes, true);
                if (!retA && !retB) {
                    Log.d(LOG_TAG, "使用AB密钥写入块" + j + "失败!");
                }
                if (MifareClassicUtils.isTrailerBlock(j)) {
                    //写入了尾部块后应当重新截取密钥!
                    if (retA) {
                        keyA = HexUtil.hexStringToByteArray(datas[i].substring(0, 12));
                    }
                    //替换密钥,重新选卡!
                    if (retB) {
                        keyB = HexUtil.hexStringToByteArray(datas[i].substring(20, 32));
                    }
                    Log.d(LOG_TAG, "当前系尾部块，需要重新截取密钥: " + j);
                }
                if (isTagCantCon(tag, false)) {
                    stopLable = true;
                    Log.d(LOG_TAG, "");
                    callback.onTagAbnormal();
                }
            }
        }
        callback.onFinish();
    }

    //实现以整卡写普通卡
    public void writeSector(boolean writeZero, boolean order, M1Bean[] datas, M1KeyBean[] keys, WriterCallback callback) {
        stopLable = false;
        //判断扇区元素和密钥元素的个数是否对的上!
        if (datas.length != keys.length) {
            Log.d(LOG_TAG, "传入验证过程密钥组和将要被写入的数据扇区数量不符合，写整卡未启动，可能未写入任何数据!请检查密钥组是否完整，是否对应卡片类型!!");
            Log.d(LOG_TAG, "The number of key groups and data sectors to be written in the incoming verification process does not match." +
                    " Writing the whole card is not started and may not write any data! Please check whether the key group is complete and whether it corresponds to the card type!!");
            stopLable = true;
            callback.onFinish();
            return;
        }
        //从最后一个扇区写到头扇区,在此之前有必要排序数组!
        Arrays.sort(datas, datasComparator);
        Arrays.sort(keys, keysComparator);
        /*for (M1Bean bean : datas) {
            Log.d(LOG_TAG, "扇区: " + bean.getSector());
        }*/
        for (int i = 0; i < datas.length; ++i) {
            //判断用户是否终止了写卡!
            if (stopLable) {
                Log.d(LOG_TAG, "Write sector terminated, current sector progress: " + datas[i].getSector());
                Log.d(LOG_TAG, "写入扇区过程被停止，当前扇区写入进度: " + datas[i].getSector());
                stopLable = false;
                callback.onFinish();
                return;
            }
            writeSector(writeZero, order, datas[i], keys[i], new WriterCallback() {
                @Override
                public void onFinish() {
                    //don't need!
                }

                @Override
                public void onDataInvalid() {
                    callback.onDataInvalid();
                }

                @Override
                public void onTagAbnormal() {
                    callback.onTagAbnormal();
                }
            });
        }
        //最终无论如何都回调写入成功的接口!
        callback.onFinish();
    }

    //实现以单块写特殊卡!
    private boolean writeBlock(MifareAdapter tag, int block, String dataStr) {
        if (!DumpUtils.isValidBlockData(dataStr)) {
            //数据格式异常!
            return false;
        }
        try {
            return tag.write(block, HexUtil.hexStringToByteArray(dataStr));
        } catch (IOException e) {
            e.printStackTrace();
            stopLable = true;
        }
        return false;
    }

    //写特殊的指定块
    public void writeBlock(int block, String dataStr, WriterCallback callback) {
        MifareAdapter tag = getTag();
        try {
            if (!tag.rescantag()) {
                stopLable = true;
                callback.onTagAbnormal();
                callback.onFinish();
                Log.d(LOG_TAG, "寻找标签失败!");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            if (!tag.connect()) {
                stopLable = true;
                callback.onTagAbnormal();
                callback.onFinish();
                Log.d(LOG_TAG, "链接标签失败!");
                return;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (!tag.isSpecialTag()) {
            stopLable = true;
            callback.onTagAbnormal();
            callback.onFinish();
            Log.d(LOG_TAG, "卡片解锁失败!");
            return;
        }
        if (DumpUtils.isValidBlockData(dataStr)) {
            if (writeBlock(tag, block, dataStr)) {
                callback.onFinish();
            } else {
                Log.d(LOG_TAG, "写块" + block + "失败!");
                callback.onFinish();
            }
        } else {
            Log.d(LOG_TAG, "该索引块数据无效: " + block + "\n" + dataStr);
            callback.onFinish();
        }
    }

    //实现以单扇区写特殊卡
    private void writeSector(MifareAdapter tag, M1Bean data, WriterCallback callback) {
        try {
            if (!tag.rescantag()) {
                stopLable = true;
                callback.onTagAbnormal();
                callback.onFinish();
                Log.d(LOG_TAG, "寻找标签失败!");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        //读取，经过后门!
        try {
            if (!tag.connect()) {
                stopLable = true;
                callback.onTagAbnormal();
                callback.onFinish();
                Log.d(LOG_TAG, "链接标签失败!");
                return;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (!tag.isSpecialTag()) {
            stopLable = true;
            callback.onTagAbnormal();
            callback.onFinish();
            Log.d(LOG_TAG, "卡片解锁失败!");
            return;
        }
        String[] datas = data.getDatas();
        //验证将要被写入的数据个数是否与卡片的块个数一致
        if (datas.length != MifareClassicUtils.getBlockCountInSector(data.getSector())) {
            //不一致,需要反馈!
            Log.d(LOG_TAG, "致命的错误发生在写扇区 " + data + "，要写入的数据个数与标签支持的个数无法成功对应上!");
            callback.onFinish();
            return;
        }
        int _sector = data.getSector();
        //获得当前扇区最后一个块
        int lastBlock = MifareClassicUtils.sectorToBlock(_sector) + MifareClassicUtils.getBlockCountInSector(_sector) - 1;
        //经过必要写入前步骤判断之后，开始写入，倒着写!
        for (int i = datas.length - 1, j = lastBlock; i >= 0; --i, --j) {
            if (DumpUtils.isValidBlockData(datas[i])) {
                if (!writeBlock(tag, j, datas[i])) {
                    Log.d(LOG_TAG, "写入块: " + j + "失败!");
                }
            } else {
                Log.d(LOG_TAG, "该索引块数据无效: " + j + "\n" + datas[i]);
            }
        }
        callback.onFinish();
    }

    //实现以整卡写特殊卡
    public void writeSector(M1Bean[] datas, WriterCallback callback) {
        MifareAdapter tag = getTag();
        //先排序一下
        Arrays.sort(datas, datasComparator);
        /*for (M1Bean bean : datas) {
            Log.d(LOG_TAG, "扇区: " + bean.getSector());
        }*/
        //迭代写入!
        for (M1Bean bean : datas) {
            writeSector(tag, bean, new WriterCallback() {
                @Override
                public void onFinish() {

                }

                @Override
                public void onDataInvalid() {
                    callback.onDataInvalid();
                }

                @Override
                public void onTagAbnormal() {
                    callback.onTagAbnormal();
                }
            });
            //判断用户是否终止了写卡!
            if (stopLable) {
                Log.d(LOG_TAG, "终止了写入扇区，当前扇区进度: " + bean.getSector());
                stopLable = false;
                break;
            }
        }
        callback.onFinish();
    }
}
