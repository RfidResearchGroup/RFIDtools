package cn.rrg.chameleon.utils;

import com.dxl.mfkey.Nonce32;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cn.dxl.mifare.MifareUtils;
import cn.rrg.chameleon.javabean.DetectionDatas;
import cn.dxl.common.util.HexUtil;

/**
 * 嗅探结果处理，是否需要解密之类的，返回字符串，截取结果!
 *
 * @author DXL
 */
public class DetectionResult {

    //private String TAG = DetectionResult.class.getSimpleName();

    //数据所属的UID
    private int uid;

    //存放解析过程中出现的消息!
    private ArrayList<String> mMsgList = new ArrayList<>();
    //存放最终的解析结果!
    private ArrayList<DetectionDatas> mResultList = new ArrayList<>();

    /**
     * 允许自定义解密的操作接口!
     */
    public interface DecryptAction {
        byte[] doDecrypt(byte[] origin);
    }

    //解密接口，可以为空，为空则不解密!
    private DecryptAction decryptAction;

    /**
     * CRC校验接口，可强制或者不强制执行此判断!
     */
    public interface CRCCheckAction {
        boolean checkCRC(byte[] sources);
    }

    /**
     * 各种set和get方法
     */
    private CRCCheckAction checkAction;

    public DecryptAction getDecryptAction() {
        return decryptAction;
    }

    public void setDecryptAction(DecryptAction decryptAction) {
        this.decryptAction = decryptAction;
    }

    public CRCCheckAction getCheckAction() {
        return checkAction;
    }

    public void setCheckAction(CRCCheckAction checkAction) {
        this.checkAction = checkAction;
    }

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    private static int typeByte2Int(byte typeByte) {
        switch (typeByte) {
            case 0x60:
                return 0;
            case 0x61:
                return 1;
            default:
                return -1;
        }
    }

    /**
     * 进行结果集处理，或者对应的
     * 1、UID
     * 2、块
     * 3、Nonce集
     *
     * @param sources 来源的字节，根据传入的action判断是否需要进行解密!
     * @return 解析结果，如果解析正常，则返回true ， 否则返回false
     */
    public boolean processResult(byte[] sources) {
        //先判断需不需要进行字节解密，如果需要，则先进行解密!
        if (decryptAction != null) {
            sources = decryptAction.doDecrypt(sources);
            if (sources == null) {
                mMsgList.add("processResult 处理数据出现了解密错误的情况!");
                return false;
            }
        }
        //然后需不需要判断进行CRC校验，如果需要，则需要进行检测，判断是否正确!
        if (checkAction != null) {
            boolean checkResult = checkAction.checkCRC(sources);
            mMsgList.add("processResult 处理数据出现了CRC校验错误的情况!");
            if (!checkResult) return false;
        }
        //然后进行真正的数据处理!
        uid = HexUtil.byte2Int(sources, 0); //转换UID，得到起始的最重要信息!
        //开始处理块验证过程中收集的nonce包!
        /*
            * Data layout
            * first 16byte is Sector0, Block0
            *
            * then comes items of 16bytes length
            *   0           auth cmd  (0x60 or 0x61)
                1           blocknumber  (0 - 0x7F)
                2,3         crc 2bytes
                4,5,6,7     NT
                8,9,10,11   NR
                12,13,14,15 AR
        */
        // Copy nonce - data into object and list
        //此处解析字节，将nonce放到对象中，总共有12个!!!
        for (int i = 0; i < 12; i++) {
            DetectionDatas dds = new DetectionDatas();
            dds.setUid(uid);
            dds.setType(typeByte2Int(sources[(i + 1) * 16]));
            if (dds.getType() == -1) {
                //密钥类型错误，可能系数据有误!
                mMsgList.add("第" + i + "个数据非正常数据!!!");
                continue;
            }
            dds.setBlock(sources[(i + 1) * 16 + 1]);
            //进行Nonce列表对象构建,存放到数列集合中!
            dds.setNonce32(new Nonce32(
                    HexUtil.byte2Int(sources, (i + 1) * 16 + 4),
                    HexUtil.byte2Int(sources, (i + 1) * 16 + 8),
                    HexUtil.byte2Int(sources, (i + 1) * 16 + 12)
            ));
            // skip sectors with 0xFF
            if (MifareUtils.validateBlock(dds.getBlock()))
                mResultList.add(dds);
        }
        //进行数据排序，按照块顺序!
        KeyComparer comparator = new KeyComparer();
        DetectionDatas[] tmpArr = mResultList.toArray(new DetectionDatas[0]);
        Arrays.sort(tmpArr, comparator);
        mResultList.clear();
        mResultList.addAll(Arrays.asList(tmpArr));
        //进行解析!
        return true;
    }

    public List<DetectionDatas> getResult() {
        return mResultList;
    }

    public String getMessage() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < mMsgList.size(); i++) {
            if (i != mMsgList.size() - 1)
                sb.append(mMsgList.get(i)).append('\n');
            else
                sb.append(mMsgList.get(i));
        }
        return sb.toString();
    }

    public List<String> getMsgList() {
        return mMsgList;
    }
}
