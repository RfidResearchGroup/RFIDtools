package cn.dxl.mifare;

import android.nfc.Tag;

import java.util.ArrayList;
import java.util.List;

/**
 * 全局的标签缓存!
 */
public class NfcTagListenUtils {
    private static Tag tag = null;
    private static List<OnNewTagListener> listeners = new ArrayList<>();

    public static Tag getTag() {
        return tag;
    }

    public static void setTag(Tag tag) {
        NfcTagListenUtils.tag = tag;
    }

    public interface OnNewTagListener {
        void onNewTag(Tag tag);
    }

    public static void addListener(OnNewTagListener listener) {
        listeners.add(listener);
    }

    public static void removeListener(OnNewTagListener listener) {
        listeners.remove(listener);
    }

    public static void notifyOnNewTag(Tag tag) {
        //直接通知TAG的变化!
        for (OnNewTagListener l : listeners) {
            try {
                l.onNewTag(tag);
            } catch (Exception ignored) {
            }
        }
    }
}
