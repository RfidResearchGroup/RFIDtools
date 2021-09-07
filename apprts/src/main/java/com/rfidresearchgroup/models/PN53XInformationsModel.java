package com.rfidresearchgroup.models;

import android.nfc.tech.MifareClassic;
import android.util.Log;

import com.rfidresearchgroup.rfidtools.R;
import com.rfidresearchgroup.util.Paths;

import java.io.File;
import java.io.IOException;

import com.rfidresearchgroup.common.application.App;
import com.rfidresearchgroup.common.util.AppUtil;
import com.rfidresearchgroup.common.util.FileUtils;
import com.rfidresearchgroup.common.util.HexUtil;
import com.rfidresearchgroup.natives.SpclMf;
import com.rfidresearchgroup.callback.TagInformationsCallback;

public class PN53XInformationsModel {
    private String LOG_TAG = "PN53XInformationsModel";
    private SpclMf spclMf = SpclMf.get();
    private App application = AppUtil.getInstance().getApp();

    public void collect(TagInformationsCallback<CharSequence> callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (spclMf.scanning() && spclMf.connect()) {
                    byte[] uidBytes = spclMf.getUid();
                    String uid = "unknown";
                    if (uidBytes != null)
                        uid = HexUtil.toHexString(uidBytes);
                    String size = "" + spclMf.getSize();
                    String atqa = HexUtil.toHexString(spclMf.getAtqa());
                    String sak = HexUtil.toHexString(spclMf.getSak());
                    String sectorCount = "" + spclMf.getSectorCount();
                    String blockCount = "" + spclMf.getBlockCount();
                    String ats = "-";
                    String tagType = application.getString(R.string.tag_unknown_mf_classic);

                    File template_mifare_info = new File(Paths.COMMON_DIRECTORY + "/" + "template_mifare_info_en.html");

                    try {
                        //读取本地的HTML模板文件!
                        byte[] templateBytes = FileUtils.readBytes(template_mifare_info);
                        String htmlSr = new String(templateBytes);
                        //替换信息!
                        htmlSr = htmlSr.replaceAll("\\$\\{UID\\}", uid);
                        htmlSr = htmlSr.replaceAll("\\$\\{Tech\\}", "ISO/IEC 14443, Type A");
                        htmlSr = htmlSr.replaceAll("\\$\\{ATQA\\}", atqa);
                        htmlSr = htmlSr.replaceAll("\\$\\{SAK\\}", sak);
                        htmlSr = htmlSr.replaceAll("\\$\\{ATS\\}", ats);

                        htmlSr = htmlSr.replaceAll("\\$\\{TM\\}", tagType);
                        htmlSr = htmlSr.replaceAll("\\$\\{MS\\}", size + " byte");
                        htmlSr = htmlSr.replaceAll("\\$\\{BS\\}", MifareClassic.BLOCK_SIZE + " byte");
                        htmlSr = htmlSr.replaceAll("\\$\\{SC\\}", sectorCount);
                        htmlSr = htmlSr.replaceAll("\\$\\{BC\\}", blockCount);

                        callback.onInformationsShow(htmlSr);
                    } catch (IOException e) {
                        e.printStackTrace();
                        callback.onInformationsShow("Template file no exists!");
                    }
                } else {
                    Log.d(LOG_TAG, "collect调用，卡片不存在!");
                    callback.onInformationsShow(application.getString(R.string.tag_not_found));
                }
            }
        }).start();
    }
}
