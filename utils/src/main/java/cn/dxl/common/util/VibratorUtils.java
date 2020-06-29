package cn.dxl.common.util;

import android.content.Context;
import android.os.VibrationEffect;
import android.os.Vibrator;

/**
 * 震动工具!
 *
 * @author DXL
 */
public class VibratorUtils {

    //进行抖动!
    public static void runOneAsDelay(Context context, int delay) {
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null && vibrator.hasVibrator()) { //有振动器
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                VibrationEffect vibrationEffect = VibrationEffect.createOneShot(delay, VibrationEffect.DEFAULT_AMPLITUDE);
                vibrator.vibrate(vibrationEffect);
            } else {
                vibrator.vibrate(delay);
            }
        }
    }
}
