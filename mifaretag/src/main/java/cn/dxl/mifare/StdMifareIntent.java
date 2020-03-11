package cn.dxl.mifare;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.NfcManager;
import android.nfc.tech.MifareClassic;

public class StdMifareIntent {

    private NfcAdapter mAdapter = null;

    public StdMifareIntent(Context context) {
        NfcManager manager = (NfcManager) context.getSystemService(Context.NFC_SERVICE);
        //判断设备是否支持NFC！
        if (manager == null) return;
        mAdapter = manager.getDefaultAdapter();
    }

    /*
     * 获取当前操作的适配器!
     * */
    public NfcAdapter getAdapter() {
        return mAdapter;
    }

    /*
     * 注册前台
     * */
    public void enableForegroundDispatch(Activity targetAct) {
        if (mAdapter == null) return;
        //进行前台广播拦截!
        Intent intent = new Intent(targetAct,
                targetAct.getClass()).addFlags(
                Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                targetAct, 0, intent, 0);
        mAdapter.enableForegroundDispatch(targetAct, pendingIntent, null, new String[][]{
                new String[]{MifareClassic.class.getName()}});
    }

    /*
     * 解注册前台
     * */
    public void disableForegroundDispatch(Activity activity) {
        if (mAdapter == null) return;
        //解注册前台拦截
        mAdapter.disableForegroundDispatch(activity);
    }

}
