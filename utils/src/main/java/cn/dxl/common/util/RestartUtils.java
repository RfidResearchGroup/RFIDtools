package cn.dxl.common.util;

import android.content.Context;
import android.content.Intent;

import cn.dxl.common.services.RestartService;

public class RestartUtils {

    public interface OnExitAction {
        /*
         * 如果返回true，则使用System.exit终结，否则使用他自几实现自带的。
         * */
        boolean usingSystemExit();
    }

    /**
     * 此工具类用来重启APP，只是单纯的重启，不做任何处理。
     * Created by 13itch on 2016/8/5.
     */

    /**
     * 重启整个APP
     *
     * @param context 上下文
     * @param Delayed 延迟多少毫秒
     * @param action  在退出时的回调，如果回调返回true，则杀死当前的进程!
     */
    public static void restartAPP(Context context, long Delayed, OnExitAction action) {
        /**开启一个新的服务，用来重启本APP*/
        Intent intent1 = new Intent(context, RestartService.class);
        intent1.putExtra("PackageName", context.getApplicationContext().getPackageName());
        intent1.putExtra("Delayed", Delayed);
        context.startService(intent1);
        if (action != null && action.usingSystemExit()) {
            /**杀死整个进程**/
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(0);
        }
    }

    /**
     * 重启整个APP
     *
     * @param context 上下文
     * @param Delayed 延迟多少毫秒
     */
    public static void restartAPP(Context context, long Delayed) {
        Intent intent1 = new Intent(context, RestartService.class);
        intent1.putExtra("PackageName", context.getApplicationContext().getPackageName());
        intent1.putExtra("Delayed", Delayed);
        context.startService(intent1);
    }

    /***重启整个APP*/
    public static void restartAPP(Context context) {
        restartAPP(context, 2000, new OnExitAction() {
            @Override
            public boolean usingSystemExit() {
                return true;
            }
        });
    }
}
