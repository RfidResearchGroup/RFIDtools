package cn.rrg.rdv.models;

import java.util.ArrayList;
import java.util.Arrays;

import cn.dxl.common.util.HexUtil;
import cn.dxl.mifare.MifareAdapter;
import cn.dxl.mifare.MfDataUtils;
import cn.rrg.rdv.callback.ReaderCallback;
import cn.rrg.rdv.javabean.M1Bean;
import cn.rrg.rdv.javabean.M1KeyBean;
import cn.rrg.rdv.util.DumpUtils;

public abstract class AbsTagReadModel extends AbsStopableTask {

    //实现读取某个扇区,如果读取成功则返回一个bean
    public M1Bean readSector(int sector, byte[] key, boolean useKeyB, ReaderCallback<M1Bean[], String> callBack) {
        MifareAdapter tag = getTag();
        M1Bean ret = null;
        //每次读取前先验证卡片是否可以链接
        tag.close();
        if (!tag.connect()) {
            //连接卡片不成功则回调上层告知!
            callBack.onTagAbnormal();
            //并且要停止当前任何动作
            stopLable = true;
            return null;
        }
        boolean auth;
        //判断应当要使用何种密钥来操做
        if (useKeyB) {
            //keyB
            auth = tag.authB(sector, key);
        } else {
            //keyA
            auth = tag.authA(sector, key);
        }
        //判断密钥验证结果且做出操作
        if (auth) {
            ret = new M1Bean();
            ret.setSector(sector);
            //验证成功，则得到当前扇区的块数量，进行遍历读取
            int blockCount = MfDataUtils.getBlockCountInSector(sector);
            //得到块开始的位置
            int startBlock = MfDataUtils.sectorToBlock(sector);
            //得到块结束的位置
            int endBlock = startBlock + blockCount;
            //建立一个数组，存放数据!
            String[] dataArrrays = new String[blockCount];
            //最后一个元素的位置!
            int last = dataArrrays.length - 1;
            //开始迭代读取!
            for (int i = startBlock, j = 0; i < endBlock; ++i, ++j) {
                //读取数据块数据并且转换为16进制字符串
                byte[] dataBytes = tag.read(i);
                String hexData;
                if (dataBytes != null) {
                    //可能会由于控制位的原因导致读取数据不成功
                    //判断读取的数据不为空之后再将转为十六进制
                    //String LOG_TAG = "AbsTagReadModel";
                    //Log.d(LOG_TAG, "读取到的数据为空,Block: " + i + " isKeyB ? = " + useKeyB);
                    hexData = HexUtil.toHexString(dataBytes);
                } else {
                    //判断是否在读尾部块
                    if (i == last) {
                        hexData = DumpUtils.NO_KEY + DumpUtils.NO_TRAIL + DumpUtils.NO_KEY;
                    } else {
                        hexData = DumpUtils.NO_DAT;
                    }
                }
                dataArrrays[j] = hexData;
            }
            //更新所有的块数据到bean中
            ret.setDatas(dataArrrays);
        }
        return ret;
    }

    //实现读取单个扇区并且回调中介者传输数据
    public void readSector(M1KeyBean keys, ReaderCallback<M1Bean[], String> callBack) {
        if (keys == null) {
            callBack.onSuccess(null);
            return;
        }
        String keyA = keys.getKeyA();
        String keyB = keys.getKeyB();
        int sector = keys.getSector();
        if (keyA.equals(DumpUtils.NO_KEY) && keyB.equals(DumpUtils.NO_KEY)) {
            return;
        }
        //密钥可用性二次验证完成，接下来读取
        M1Bean aBean = readSector(sector, HexUtil.hexStringToByteArray(keyA), false, callBack);
        M1Bean bBean = readSector(sector, HexUtil.hexStringToByteArray(keyB), true, callBack);
        //合并数据
        M1Bean ret = DumpUtils.mergeBean(aBean, bBean);
        //创建一个数组并且回调传输
        M1Bean[] rets = {ret};
        callBack.onSuccess(rets);
    }

    //实现所有的扇区读取
    public void readSectors(M1KeyBean[] keyBeans, ReaderCallback<M1Bean[], String> callBack) {
        if (keyBeans == null) {
            callBack.onSuccess(null);
            return;
        }
        ArrayList<M1Bean> rets = new ArrayList<>(keyBeans.length);
        //直接迭代扇区数据
        for (M1KeyBean keys : keyBeans) {
            //取出密钥实体
            //得到AB密钥
            String keyA = keys.getKeyA();
            String keyB = keys.getKeyB();
            int sector = keys.getSector();
            if (keyA.equals(DumpUtils.NO_KEY) && keyB.equals(DumpUtils.NO_KEY)) {
                continue;
            }
            //密钥可用性二次验证完成，接下来读取
            M1Bean aBean = readSector(sector, HexUtil.hexStringToByteArray(keyA), false, callBack);
            DumpUtils.updateTrailer(aBean, keys);
            M1Bean bBean = readSector(sector, HexUtil.hexStringToByteArray(keyB), true, callBack);
            DumpUtils.updateTrailer(bBean, keys);
            //合并数据
            M1Bean ret = DumpUtils.mergeBean(aBean, bBean);
            rets.add(ret);
            //判断是否需要退出
            if (stopLable) {
                callBack.onSuccess(rets.toArray(new M1Bean[0]));
                //回调后直接退出
                stopLable = false;
                return;
            }
        }
        callBack.onSuccess(rets.toArray(new M1Bean[0]));
    }

    //实现读特殊卡以单扇区模式
    public void readSpecialTag(int sector, ReaderCallback<M1Bean[], String> callBack) {
        MifareAdapter tag = getTag();
        //读取，经过后门!
        if (!tag.connect()) {
            callBack.onTagAbnormal();
            stopLable = true;
            return;
        }
        if (!tag.isSpecialTag()) {
            callBack.onTagAbnormal();
            stopLable = true;
            return;
        }
        //建立bean对象，存放读取结果
        M1Bean ret = new M1Bean();
        //设置扇区
        ret.setSector(sector);
        //得到扇区总数量
        int blockCount = MfDataUtils.getBlockCountInSector(sector);
        //建立数据块字符串数组
        String[] datas = new String[blockCount];
        //这个扇区的第一块
        int firstBlock = MfDataUtils.sectorToBlock(sector);
        //迭代扇区中的块!
        for (int i = 0, j = firstBlock; i < blockCount; ++i, ++j) {
            //Log.d(LOG_TAG, "读取块: " + i);
            //读取后转为HEX字符串并且存放
            datas[i] = HexUtil.toHexString(tag.read(j));
            //判断是否有效
            if (datas[i] == null) {
                //使用无数据参数填充!
                datas[i] = DumpUtils.NO_DAT;
            }
            //Log.d(LOG_TAG, "测试打印: " + datas[i]);
        }
        //更新进bean中!
        ret.setDatas(datas);
        //迭代完成后将结果回调传输回到上层!
        callBack.onSuccess(new M1Bean[]{ret});
    }

    //实现以整卡模式读特殊卡
    public void readSpecialTag(ReaderCallback<M1Bean[], String> callBack) {
        MifareAdapter tag = getTag();
        ArrayList<M1Bean> datas = new ArrayList<>();
        //迭代扇区
        for (int i = 0; i < tag.getSectorCount(); ++i) {
            //调用已经封装的行为，避免代码冗余!
            readSpecialTag(i, new ReaderCallback<M1Bean[], String>() {
                @Override
                public void onSuccess(M1Bean[] m1Beans) {
                    datas.addAll(Arrays.asList(m1Beans));
                }

                @Override
                public void onTagAbnormal() {
                    callBack.onTagAbnormal();
                }


            });
            //判断是否需要停止工作
            if (stopLable) {
                stopLable = false;
                return;
            }
        }
        //回调传输到上层
        callBack.onSuccess(datas.toArray(new M1Bean[0]));
    }

    // 初始化标签的读写封装的持有!!!
    protected abstract MifareAdapter getTag();
}
