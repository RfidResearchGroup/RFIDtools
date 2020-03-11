package cn.rrg.rdv.models;

import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import cn.dxl.common.util.FileUtils;
import cn.dxl.common.util.HexUtil;
import cn.dxl.common.util.LogUtils;
import cn.dxl.mifare.MifareAdapter;
import cn.dxl.mifare.MifareUtils;
import cn.rrg.rdv.callback.KeysAuthCallback;
import cn.rrg.rdv.javabean.M1KeyBean;
import cn.rrg.rdv.util.DumpUtils;

public abstract class AbsTagKeysCheckModel extends AbsStopableTask {

    public interface KeyFilesCallback {
        File[] getKeyFiles();
    }

    private KeyFilesCallback callback;

    // 解析秘钥后进行保存!
    private ArrayList<String> keyList = new ArrayList<>();

    public abstract MifareAdapter getTag();

    public AbsTagKeysCheckModel(KeyFilesCallback callback) {
        this.callback = callback;
    }

    public int getSectorCount() {
        return getTag().getSectorCount();
    }

    // 检查所有的扇区!
    public void checkAllByAllKeys(KeysAuthCallback callback) {
        Log.d("AbsTagKeysCheckModel", "checkAllByAllKyes() 开始检索秘钥!!");
        // 先初始化秘钥队列!
        if (initKeys(callback)) {
            // 如果初始化成功，至少有一个秘钥，则我们进行相关的验证处理!
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Log.d("AbsTagKeysCheckModel", "initKeys提取文件中的秘钥成功，开始迭代!!");
                    authkeysInAllSector(callback);
                }
            }).start();
        } else {
            Log.d("AbsTagKeysCheckModel", "初始化秘钥队列失败!");
        }
    }

    // 只检查一个扇区!
    public void checkOneByAllKeys(int sectorIndex, KeysAuthCallback callback) {

    }

    // 检查一个扇区用指定的秘钥! TODO 暂时不做这个!
    public void checkOneByCustomerKey(int sectorIndex, M1KeyBean keyBean, KeysAuthCallback callback) {
    }

    protected boolean initKeys(KeysAuthCallback callback) {
        keyList.clear();
        // 初始化秘钥文件!
        // 持有秘钥文件，从view层传递过来!
        File[] keyFiles = this.callback.getKeyFiles();
        if (keyFiles == null) return false;
        //读出所有的密钥
        for (File f : keyFiles) {
            //读取出所有的密钥
            String[] _keyList;
            try {
                _keyList = FileUtils.readLines(f, "[A-Fa-f0-9]{12}");
            } catch (IOException e) {
                e.printStackTrace();
                continue;
            }
            if (_keyList == null) {
                continue;
            }
            if (_keyList.length <= 0) {
                continue;
            }
            //迭代加载密钥到字符串构建器
            for (String _key : _keyList) {
                if (!keyList.contains(_key)) {
                    keyList.add(_key);
                }
            }
        }
        //一个密钥至少12个hex字符，此处判断是否至少有一个密钥!
        if (keyList.size() >= 1) {
            //回调到P层
            return true;
        } else {
            callback.onKeysInvalid();
            return false;
        }
    }

    private void callTagAbnormalAndstop(KeysAuthCallback callback) {
        //连接卡片不成功则回调上层告知!
        callback.onTagAbnormal();
        //并且要停止当前任何动作
        stopLable = true;
        LogUtils.d("callTagAbnormalAndstop() 停止了操作!");
    }

    //实现验证某个扇区的密钥，根据参数，可以做某些操作
    private M1KeyBean authKeys(MifareAdapter tag, int sector, KeysAuthCallback callBack) {
        //主要实现的是针对某个扇区
        // 进行多个密钥遍历验证
        M1KeyBean ret = new M1KeyBean();
        //初始化结果bean
        ret.setSector(sector);
        ret.setKeyA(DumpUtils.NO_KEY);
        ret.setKeyB(DumpUtils.NO_KEY);
        for (int i = 0; i < keyList.size(); i++) {
            //结束标志!
            if (stopLable) {
                return ret;
            }
            //然后检测是否全部验证通过
            if (!ret.getKeyA().equals(DumpUtils.NO_KEY) && !ret.getKeyB().equals(DumpUtils.NO_KEY)) {
                //如果已经全部鉴权成功就直接返回bean
                return ret;
            }
            //从数列中得到密钥
            byte[] tmpKey = HexUtil.hexStringToByteArray(keyList.get(i));
            //TODO 每次验证之前先尝试链接卡片!
            try {
                if (tag.connect()) {
                    //尝试用来验证密钥A
                    callBack.onKeys(keyList.get(i));
                    if (tag.authA(sector, tmpKey)) {
                        //如果验证成功则在bean中记录
                        ret.setKeyA(HexUtil.toHexString(tmpKey));
                    }
                    //尝试将此密钥用来鉴权密钥B
                    if (tag.authB(sector, tmpKey)) {
                        //鉴权成功则将密钥更新进bean中
                        ret.setKeyB(HexUtil.toHexString(tmpKey));
                    }
                } else {
                    callTagAbnormalAndstop(callBack);
                    return ret;
                }
            } catch (IOException e) {
                e.printStackTrace();
                callTagAbnormalAndstop(callBack);
                return ret;
            }
        }
        return ret;
    }

    //实现验证所有的扇区
    private void authkeysInAllSector(KeysAuthCallback callBack) {
        // 初始化标签
        MifareAdapter stdMf = getTag();
        //迭代验证扇区
        int sectors = stdMf.getSectorCount();
        ArrayList<M1KeyBean> _kList = new ArrayList<>();
        for (int i = 0; i < sectors; i++) {
            // 进行进度通知!
            callBack.onAuth((sectors - i) - 1);
            // 进行结果添加!
            _kList.add(authKeys(stdMf, i, callBack));
            //在进入后直接返回
            if (stopLable) {
                Log.d("AbsTagKeysCheckModel", "stopLable触发，自动停止!");
                stopLable = false;
                callBack.onResults(_kList.toArray(new M1KeyBean[0]));
                try {
                    stdMf.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return;
            }
        }
        //在验证完成后回调接口传输验证或者成功的，或者失败的keyBean到上层,等待上层做下一步操作!
        callBack.onResults(_kList.toArray(new M1KeyBean[0]));
        try {
            stdMf.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        stopLable = false;
    }

    //实现验证单个的扇区
    private void authkeysInSector(int sector, KeysAuthCallback callBack) {
        // 初始化标签
        MifareAdapter stdMf = getTag();
        if (!MifareUtils.validateSector(sector)) {
            //不是有效的扇区，可能是出现了BUG
            callBack.onResults(null);
            return;
        }
        M1KeyBean[] keys = new M1KeyBean[1];
        keys[0] = authKeys(stdMf, sector, callBack);
        if (stopLable) {
            stopLable = false;
            callBack.onResults(keys);
            return;
        }
        callBack.onResults(keys);
    }

}
