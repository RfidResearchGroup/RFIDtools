package cn.dxl.common.util;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import cn.dxl.common.activities.CrashActivity;

public class CrashUtil {
    private static final Thread.UncaughtExceptionHandler DEFAULT_UNCAUGHT = Thread.getDefaultUncaughtExceptionHandler();
    public static final List<Activity> activities = new ArrayList<>();

    /*
     * callback for activity!
     * */
    private static final
    Application.ActivityLifecycleCallbacks callbacks4Act = new Application.ActivityLifecycleCallbacks() {
        @Override
        public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle bundle) {
            //must add activity to list!
            activities.add(activity);
        }

        @Override
        public void onActivityStarted(@NonNull Activity activity) {

        }

        @Override
        public void onActivityResumed(@NonNull Activity activity) {

        }

        @Override
        public void onActivityPaused(@NonNull Activity activity) {

        }

        @Override
        public void onActivityStopped(@NonNull Activity activity) {

        }

        @Override
        public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle bundle) {

        }

        @Override
        public void onActivityDestroyed(@NonNull Activity activity) {
            //must remove activity from list!
            activities.remove(activity);
        }
    };

    public static void register(final Application application) {
        //register callback!
        application.registerActivityLifecycleCallbacks(callbacks4Act);
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(@NonNull Thread t, @NonNull Throwable e) {
                //跳转到奔溃信息处理界面
                Intent intent = new Intent(application, CrashActivity.class);
                intent.putExtra("crash", e);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                //destroy all activity
                for (Activity act : activities) {
                    act.finish();
                }
                application.startActivity(intent);
                //调用默认的异常处理
                if (DEFAULT_UNCAUGHT != null) {
                    DEFAULT_UNCAUGHT.uncaughtException(t, e);
                }
            }
        });
    }
}
