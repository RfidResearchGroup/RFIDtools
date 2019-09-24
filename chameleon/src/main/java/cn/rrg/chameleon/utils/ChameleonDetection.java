package cn.rrg.chameleon.utils;

import java.util.List;

import cn.dxl.mifare.MifareUtils;
import cn.rrg.chameleon.javabean.ResultBean;
import cn.rrg.chameleon.javabean.DetectionDatas;
import cn.rrg.mfkey.NativeMfKey32;
import cn.rrg.mfkey.Nonce32;
import cn.dxl.common.util.HexUtil;

/**
 * @author DXL
 * 对变色龙的侦测数据进行解析，由开源库中的C#源码进行更改!
 * @ref https://github.com/iceman1001/ChameleonMini-rebootedGUI/blob/master/ChameleonMiniGUI/Attacks/MfKeyAttacks.cs
 */
public class ChameleonDetection {

    /*
     * Downloaded data from devices should be 208 byte plus 2 for CRC. (210)
     *  4 bytes uid,
     *  12 empty bytes
     *  192 bytes of collected nonce
     * Layout like this::
     *
     *  byte 0 - 3 == UID
     *  byte 4 - 15 == empty
     *  --repeating 16 bytes
     *  byte 16  == keytype A/B
     *  byte 17  == sector
     *  byte 20 - 23 == NT
     *  byte 24 - 27 == NR
     *  byte 28 - 31 == AR
     *  --
     *  byte 32  == keytype A/B
     *  byte 33  == sector
     *  byte 36 - 39 == NT
     *  byte 40 - 43 == NR
     *  byte 44 - 47 == AR
     *
     *  In order to run mfkey32mobieus attack, you need two authentication tries against same sector and keytype (A/B).
     *
     */

    //private String LOG_TAG = ChameleonDetection.class.getSimpleName();

    public interface DecryptCallback {
        void onMsg(String msg);

        void onKey(ResultBean result);
    }

    private DecryptCallback mDecryptCallback;

    public ChameleonDetection(DecryptCallback callback) {
        //必须传入回调，进行消息打印!
        mDecryptCallback = callback;
    }

    public void decrypt(byte[] bytes, boolean all) {
        if (bytes == null || bytes.length <= 0) {
            mDecryptCallback.onMsg("数据校验失败!");
            return;
        }
        //进行解析!
        DetectionResult resultUtil = new DetectionResult();
        // Decrypt data,  with key 123321,  length 208
        resultUtil.setDecryptAction(new DetectionResult.DecryptAction() {
            @Override
            public byte[] doDecrypt(byte[] origin) {
                //此类解密是等长解密，直接在原有数组上操作即可，返回自身引用!
                ChameleonUtils.decryptData(origin, 123321, 208);
                return origin;
            }
        });
        //开始处理数据，进行解析!
        if (resultUtil.processResult(bytes)) {   //解析成功!
            //打印UID
            mDecryptCallback.onMsg("此段侦测数据的UID: " +
                    HexUtil.toHexString(HexUtil.toByteArray(resultUtil.getUid())));
        } else {
            return;
        }
        //尝试打印最终处理有效的结果集size
        mDecryptCallback.onMsg("有效数据个数统计为: " + resultUtil.getResult().size());
        mDecryptCallback.onMsg("最终处理的消息: \n" + resultUtil.getMessage());
        List<DetectionDatas> resultList = resultUtil.getResult();
        if (resultList.size() < 2) {
            mDecryptCallback.onMsg("无有效数据!");
            return;
        }
        if (all) {
            //进行迭代计算所有的密钥!
            for (int i = 0; i < resultList.size(); i++) {
                //取出当前索引的对象!
                DetectionDatas data1 = resultList.get(i);
                //尝试进行解密，逐个逐个进行尝试!
                Nonce32 n321 = data1.getNonce32();
                //迭代下一个数据对象!
                for (int j = i + 1; j < resultList.size(); j++) {
                    //对两个数据进行对比，判断块和密钥类型是否相同!
                    DetectionDatas data2 = resultList.get(j);
                    if (data1.getBlock() != data2.getBlock()
                            || data1.getType() != data2.getType()) {
                        //数据所属块，密钥类型不同则需要跳过!
                        continue;
                    }
                    Nonce32 n322 = data2.getNonce32();
                    //进行最终的计算!
                    String key = new NativeMfKey32().decrypt4IntParams(
                            //设置UID
                            resultUtil.getUid(),
                            //第一个随机数信息
                            n321.getNt(),
                            n321.getNr(),
                            n321.getAr(),
                            //第二个随机数信息
                            n322.getNt(),
                            n322.getNr(),
                            n322.getAr()
                    );
                    //判断解密结果!
                    if (key != null) {
                        mDecryptCallback.onMsg("解密第" + i + "." + j + "个数据成功!");
                        //如果成功，则拼装解密的结果!
                        ResultBean resultBean = new ResultBean();
                        resultBean.setId(HexUtil.toHexString(resultUtil.getUid()));
                        resultBean.setSector(MifareUtils.blockToSector(data1.getBlock()));
                        resultBean.setBlock(MifareUtils.getIndexOnSector(data1.getBlock(), resultBean.getSector()));
                        resultBean.setKeyA(data1.getType() == 1);
                        resultBean.setKey(key);
                        //拼装完成，进行解密!
                        mDecryptCallback.onKey(resultBean);
                    } else {
                        mDecryptCallback.onMsg("解密第" + i + "." + j + "个数据失败!");
                    }
                }
            }
        } else {
            DetectionDatas d1 = resultList.get(0);
            DetectionDatas d2 = resultList.get(1);
            //只进行单个嗅探结果解密!
            Nonce32 n321 = d1.getNonce32();
            Nonce32 n322 = d2.getNonce32();
            //进行最终的计算!
            String key = new NativeMfKey32().decrypt4IntParams(
                    //设置UID
                    resultUtil.getUid(),
                    //第一个随机数信息
                    n321.getNt(),
                    n321.getNr(),
                    n321.getAr(),
                    //第二个随机数信息
                    n322.getNt(),
                    n322.getNr(),
                    n322.getAr()
            );
            //判断解密结果!
            if (key != null) {
                mDecryptCallback.onMsg("解密第1个数据成功!");
                //如果成功，则拼装解密的结果!
                ResultBean resultBean = new ResultBean();
                resultBean.setId(HexUtil.toHexString(resultUtil.getUid()));
                resultBean.setSector(MifareUtils.blockToSector(d1.getBlock()));
                resultBean.setBlock(MifareUtils.getIndexOnSector(d1.getBlock(), resultBean.getSector()));
                resultBean.setKeyA(d1.getType() == 0);
                resultBean.setKey(key);
                //拼装完成，进行解密!
                mDecryptCallback.onKey(resultBean);
            } else {
                mDecryptCallback.onMsg("解密第1个数据失败!");
            }
        }

        // 24247720570804006263646566676869
        // 60074A0F9A8FAF5B85E40486FF05F904
        // 60074A0F6F69E10ED1EE50F551151A85
        // 60074A0F3E15380C723F3CDC09542CA9
        // FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF
        // FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF
        // FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF
        // 610792164F4A137BB32E224838AD2AFD
        // 610792163444C8507A029E18E473FE34
        // 610792160888969F9BBE967085004B19
        // FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF
        // FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF
        // FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF
        // 504E3130303A4F4B0D0A

        //进行数据输出!
        //Log.d(LOG_TAG, "解密后的数据: " + HexUtil.toHexString(bytes));
        return;
    }
}