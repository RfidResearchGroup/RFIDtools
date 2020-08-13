package cn.rrg.rdv.util;

import android.app.Activity;
import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.widget.RadioGroup;

import com.termux.app.TermuxService;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cn.dxl.common.util.AppUtil;
import cn.dxl.common.util.FileUtils;
import cn.rrg.rdv.R;
import cn.rrg.rdv.activities.tools.DumpEditActivity;
import cn.rrg.rdv.application.Properties;
import cn.rrg.rdv.javabean.DevBean;
import cn.dxl.common.util.ArrayUtils;

/**
 * Created by DXL on 2017/10/27.
 */
public class Commons {

    public static final String LOG_TAG = Commons.class.getSimpleName();
    public static String PM3_CLIENT_VERSION = "RRG/Iceman// 2020/05/06";
    public static Application application = AppUtil.getInstance().getApp();

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
            String addr = devBean.getMacAddress();
            for (int i = 0; i < list.size(); i++) {
                DevBean tmpBean = list.get(i);
                if (tmpBean == null) return false;
                String a = tmpBean.getMacAddress();
                if (a.equals(addr)) {
                    list.remove(tmpBean);
                    return true;
                }
            }
        } else {
            return false;
        }
        return true;
    }

    //判断两个设备是否是一致的
    public static boolean equalDebBean(DevBean a, DevBean b) {
        if (a == b) return true;
        if (a == null || b == null) return false;
        return a.getMacAddress().equals(b.getMacAddress());
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
        return ArrayUtils.list2Arr(devList);
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

    public static void gotoDumpEdit(Activity activity, File file) {
        if (activity == null || file == null) return;
        activity.startActivity(
                new Intent(activity, DumpEditActivity.class)
                        .putExtra("file", file.getAbsolutePath())
        );
    }

    public static File createInternalDump(String name) {
        File ret = new File(Paths.DUMP_DIRECTORY + File.separator + name);
        FileUtils.createFile(ret);
        return ret;
    }

    public static File createInternalKey(String name) {
        File ret = new File(Paths.KEY_DIRECTORY + File.separator + name);
        FileUtils.createFile(ret);
        return ret;
    }

    public static File getInternalPath() {
        return application.getFilesDir();
    }

    public static File getInternalPath(String sub) {
        File file = new File(getInternalPath().getPath() + File.separator + sub);
        FileUtils.createFile(file);
        return file;
    }

    public static SharedPreferences getPrivatePreferences() {
        return AppUtil.getInstance()
                .getApp()
                .getSharedPreferences("Main", Activity.MODE_PRIVATE);
    }

    public static String getLanguage() {
        return getPrivatePreferences()
                .getString(Properties.k_app_language, "auto");
    }

    public static void setLanguage(String language) {
        getPrivatePreferences().edit()
                .putString(Properties.k_app_language, language).apply();
    }

    public static boolean isPM3ResInitialled() {
        File pm3Path = new File(TermuxService.HOME_PATH + File.separator + Paths.PM3_PATH);
        String[] list = pm3Path.list();
        return list != null && list.length > 0;
    }

    public static File createTmpFile(String fileName) {
        File file = new File(application.getCacheDir(), fileName);
        FileUtils.delete(file);
        FileUtils.createFile(file);
        return file;
    }

    public static Set<String> getKeyFilesSelected() {
        return getPrivatePreferences()
                .getStringSet(Properties.k_common_rw_keyfile_selected, new HashSet<>());
    }

    public static void addKeyFileSelect(String path) {
        Set<String> old = getKeyFilesSelected();
        old.add(path);
        getPrivatePreferences()
                .edit()
                .putStringSet(Properties.k_common_rw_keyfile_selected, old)
                .apply();
    }

    public static void delKeyFileSelected(String path) {
        Set<String> old = getKeyFilesSelected();
        old.remove(path);
        getPrivatePreferences()
                .edit()
                .putStringSet(Properties.k_common_rw_keyfile_selected, old)
                .apply();
    }

    public static String getABISupported(boolean has64So) {
        if (has64So && Build.SUPPORTED_64_BIT_ABIS.length > 0)
            return Build.SUPPORTED_64_BIT_ABIS[0];
        if (!has64So && Build.SUPPORTED_32_BIT_ABIS.length > 0)
            return Build.SUPPORTED_32_BIT_ABIS[0];
        if (Build.SUPPORTED_64_BIT_ABIS.length > 0)
            return Build.SUPPORTED_64_BIT_ABIS[0];
        if (Build.SUPPORTED_32_BIT_ABIS.length > 0)
            return Build.SUPPORTED_32_BIT_ABIS[0];
        return null;
    }

    public static String getPM3ClientPath() {
        return TermuxService.HOME_PATH +
                File.separator +
                Paths.PM3_PATH +
                File.separator +
                Paths.PM3_PATH +
                "_" +
                getABISupported(true);
    }

    public static boolean isPM3ClientDecompressed() {
        return new File(getPM3ClientPath()).exists();
    }

    public static boolean isElfDecompressed() {
        return new File(Paths.PM3_IMAGE_OS_FILE).exists() &&
                new File(Paths.PM3_IMAGE_BOOT_FILE).exists();
    }

    public static void setAutoGoToTerminal(boolean auto) {
        getPrivatePreferences().edit()
                .putBoolean(Properties.k_auto_goto_terminal, auto)
                .apply();
    }

    public static boolean getAutoGoToTerminal() {
        return getPrivatePreferences().getBoolean(Properties.k_auto_goto_terminal, false);
    }

    /**
     * Set terminal type
     *
     * @param type 0 = full terminal view
     *             1= simple terminal view
     */
    public static void setTerminalType(int type) {
        getPrivatePreferences().edit()
                .putInt(Properties.k_terminal_type, type)
                .apply();
    }

    public static int getTerminalType() {
        return getPrivatePreferences()
                .getInt(Properties.k_terminal_type, -1);
    }

    public static void setPM3ExternalWorkDirectoryEnable(boolean enable) {
        getPrivatePreferences().edit()
                .putBoolean(Properties.k_pm3_externl_cwd_enable, enable)
                .apply();
    }

    public static boolean isPM3ExternalWorkDirectoryEnable() {
        return getPrivatePreferences()
                .getBoolean(Properties.k_pm3_externl_cwd_enable, false);
    }

    public static String updatePM3Cwd() {
        // init pm3 cwd
        if (Commons.isPM3ExternalWorkDirectoryEnable()) {
            TermuxService.PM3_CWD = Paths.PM3_CWD_FINAL;
            Paths.PM3_CWD = TermuxService.PM3_CWD;
            new File(Paths.PM3_CWD).mkdirs();
        } else {
            Paths.PM3_CWD = TermuxService.HOME_PATH;
            TermuxService.PM3_CWD = null;
        }
        return Paths.PM3_CWD;
    }
}
