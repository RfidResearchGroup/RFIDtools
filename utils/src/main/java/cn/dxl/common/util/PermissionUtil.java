package cn.dxl.common.util;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import java.util.ArrayList;

/**
 * 权限操作工具!
 *
 * @author DXL
 */
public class PermissionUtil {
    private static final String LOG_TAG = PermissionUtil.class.getSimpleName();
    //需要检查的权限!
    private String[] permissions;
    //丢失的权限!
    private String[] permissionLose;

    //上下文!
    private Context context;
    //回调!
    private Callback callback;

    //权限请求的时候的返回值!
    private int requestCode = 0x665;

    public PermissionUtil(Context context) {
        this.context = context;
    }

    /**
     * 检查权限，进行判断!
     */
    public void checks() {
        //在开始检查权限之前的操作!
        callback.onStartChecks(this);
        //在检查的时候的回调!
        boolean isAllVaild = true;
        if (permissions == null) {
            Log.d(LOG_TAG, "传入的初始权限为空!");
            return;
        }
        for (String per : permissions) {
            //迭代检查权限!
            if (!check(per)) {
                isAllVaild = false;
            }
        }
        ArrayList<String> list = new ArrayList<>();
        //所有的权限都正常时的回调!!
        if (isAllVaild) {
            callback.onPermissionNormal(this);
        } else {
            //先迭代进行权限丢失的处理!
            for (String per : permissions) {
                //迭代请求权限!
                if (!check(per)) {
                    //如果检查到的权限无法通过处理，则进行其他操作!
                    callback.whatPermissionLose(per, this);
                    list.add(per);
                }
            }
            //缓存丢失的权限!
            permissionLose = ArrayUtils.list2Arr(list);
            //如果检查到的权限无法通过处理，则进行其他操作!
            callback.onPermissionLose(this);
        }
        //在检查完毕之后的回调!
        callback.onEndChecks();
    }

    public boolean check(String per) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return context.checkSelfPermission(per) == PackageManager.PERMISSION_GRANTED;
        }
        return context.checkCallingOrSelfPermission(per) == PackageManager.PERMISSION_GRANTED;
    }

    public void request(String per) {
        if (context instanceof Activity) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                ((Activity) context).requestPermissions(new String[]{per}, requestCode);
            }
        }
    }

    public String[] getPermissions() {
        return permissions;
    }

    public void setPermissions(String[] permissions) {
        this.permissions = permissions;
    }

    public int getRequestCode() {
        return requestCode;
    }

    public void setRequestCode(int requestCode) {
        this.requestCode = requestCode;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public Callback getCallback() {
        return callback;
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public String[] getPermissionLose() {
        return permissionLose;
    }

    public interface Callback {
        /**
         * 在权限开始检查的时候的的回调!
         *
         * @param util 工具类对象!
         */
        void onStartChecks(PermissionUtil util);

        /**
         * 在权限丢失时的回调!
         *
         * @param util 工具类对象!
         */
        void onPermissionLose(PermissionUtil util);

        /**
         * 在权限正常的时候的回调!
         *
         * @param util 工具类对象!
         */
        void onPermissionNormal(PermissionUtil util);

        /**
         * 在权限丢失时的请求回调!
         *
         * @param per  丢失的权限!
         * @param util 工具类对象!
         */
        void whatPermissionLose(String per, PermissionUtil util);

        /**
         * 在权限开始检查的时候的的回调!
         */
        void onEndChecks();
    }
}
