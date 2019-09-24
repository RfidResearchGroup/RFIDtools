package cn.rrg.rdv.util;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.RadioGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import cn.rrg.rdv.R;
import cn.rrg.rdv.javabean.DevBean;
import cn.dxl.common.util.MyArrays;

/**
 * Created by DXL on 2017/10/27.
 */
public class Commons {

    //短称路径
    public static final String LOG_TAG = Commons.class.getSimpleName();

    private Commons() {
    }

    //呼叫QQ
    public static void callQQ(Context context, String qq, Runnable onFaild) {
        //这里的228451878是自己指定的QQ号码，可以自己更换
        String url = "mqqwpa://im/chat?chat_type=wpa&uin=" + qq;
        try {
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        } catch (Exception e) {
            onFaild.run();
        }
    }

    //打开浏览器，链接到指定的链接
    public static void openUrl(Context context, String url) {
        try {
            Uri uri = Uri.parse(url);
            Intent intent = new Intent();
            intent.setAction("android.intent.action.VIEW");
            intent.setData(uri);
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //根据单选返回地址!
    public static String getCustomerPath(RadioGroup group) {
        if (group == null) return Paths.DUMP_DIRECTORY;
        int id = group.getCheckedRadioButtonId();
        String path;
        if (id == R.id.rdoBtnWriteDataDefaultPath) {
            path = Paths.DUMP_DIRECTORY;
        } else if (id == R.id.rdoBtnWriteDataMCTPath) {
            path = Paths.MCT_DUMP_DIRECTORY;
        } else if (id == R.id.rdoBtnWriteDataWecatPath) {
            path = Paths.WECAT_DIRECTORY;
        } else if (id == R.id.rdoBtnWriteDataSdcardPath) {
            path = Paths.EXTERNAL_STORAGE_DIRECTORY;
        } else if (id == R.id.rdoBtnWriteDataQQPath) {
            path = Paths.QQ_DIRECTORY;
        } else if (id == R.id.rdoBtnWriteDataMToolsPath) {
            return Paths.MTools_DUMP_DIRECTORY;
        } else if (id == R.id.rdoBtnWriteDataPM3Path) {
            return Paths.PM3_DIRECTORY;
        } else {
            path = Paths.DUMP_DIRECTORY;
        }
        return path;
    }

    //移除设备对象从集合中!
    public static boolean removeDevByList(DevBean devBean, List<DevBean> list) {
        if (devBean != null) {
            String name = devBean.getDevName();
            String addr = devBean.getMacAddress();
            if (name == null) return false;
            for (int i = 0; i < list.size(); i++) {
                DevBean tmpBean = list.get(i);
                if (tmpBean == null) return false;
                String n = tmpBean.getDevName();
                String a = tmpBean.getMacAddress();
                if (n == null) return false;
                if (n.equals(name) && a.equals(addr)) {
                    list.remove(tmpBean);
                    return true;
                }
            }
        } else {
            return false;
        }
        return true;
    }

    //该设备实体之中的字段是否是空的!
    public static boolean isDevBeanDataNotNull(DevBean devBean) {
        if (devBean == null) return false;
        if (devBean.getDevName() == null) return false;
        return devBean.getMacAddress() != null;
    }

    //判断两个设备是否是一致的
    public static boolean equalDebBean(DevBean a, DevBean b) {
        if (a == b) return true;
        if (a == null || b == null) return false;

        if (isDevBeanDataNotNull(a) && isDevBeanDataNotNull(b)) {
            return a.getDevName().equals(b.getDevName())
                    && a.getMacAddress().equals(b.getMacAddress());
        }
        return false;
    }

    //从蓝牙适配器中取出历史连接的设备列表!
    public static DevBean[] getDevsFromBTAdapter(BluetoothAdapter btAdapter) {
        ArrayList<DevBean> devList = new ArrayList<>();
        Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();
        if (pairedDevices == null) return null;
        if (pairedDevices.size() > 0) {
            ArrayList<BluetoothDevice> tmpList = new ArrayList<>(pairedDevices);
            for (int i = 0; i < tmpList.size(); ++i) {
                devList.add(new DevBean(tmpList.get(i).getName(),
                        tmpList.get(i).getAddress()));
            }
        } else {
            return null;
        }
        return MyArrays.list2Arr(devList);
    }

    //设备是否是USB设备!
    public static boolean isUsbDevice(String address) {
        if (address == null) return false;
        //这三种mac是开发者定义的用于区分USB设备和蓝牙设备的特征符！
        switch (address) {
            case "00:00:00:00:00:00":
            case "00:00:00:00:00:01":
            case "00:00:00:00:00:02":
                return true;
        }
        return false;
    }
}
