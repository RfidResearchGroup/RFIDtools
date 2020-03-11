package cn.rrg.rdv.models;

import android.app.Application;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.NfcA;
import android.text.Html;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import cn.dxl.common.util.FileUtils;
import cn.dxl.common.util.HexUtil;
import cn.dxl.common.util.AppUtil;
import cn.dxl.mifare.GlobalTag;
import cn.rrg.rdv.R;
import cn.rrg.rdv.callback.TagInformationsCallback;
import cn.rrg.rdv.util.Paths;

/*
 * 标准的信息显示的数据源!
 * */
public class StdNfcInformationsModel {

    private String LOG_TAG = "StdNfcInformationsModel";

    private Application application = AppUtil.getInstance().getApp();

    public void collect(TagInformationsCallback<CharSequence> callback) {
        //获得最新的标签对象!
        Tag tag = GlobalTag.getTag();
        int mMFCSupport;
        if (tag != null) {
            Log.d(LOG_TAG, "collect调用，卡片存在!");
            // Check for MIFARE Classic support.
            mMFCSupport = checkMifareClassicSupport(tag);
            // Get generic info and set these as text.
            // 生成通用的标签信息!
            String uid = HexUtil.toHexString(tag.getId());
            int uidLen = tag.getId().length;
            uid += " (" + uidLen + " byte";
            if (uidLen == 7) {
                uid += ", CL2";
            } else if (uidLen == 10) {
                uid += ", CL3";
            }
            uid += ")";
            NfcA nfca = NfcA.get(tag);
            // Swap ATQA to match the common order like shown here:
            // http://nfc-tools.org/index.php?title=ISO14443A
            byte[] atqaBytes = nfca.getAtqa();
            atqaBytes = new byte[]{atqaBytes[1], atqaBytes[0]};
            String atqa = HexUtil.toHexString(atqaBytes);
            // SAK in big endian.
            byte[] sakBytes = new byte[]{
                    (byte) ((nfca.getSak() >> 8) & 0xFF),
                    (byte) (nfca.getSak() & 0xFF)};
            String sak;
            // Print the first SAK byte only if it is not 0.
            if (sakBytes[0] != 0) {
                sak = HexUtil.toHexString(sakBytes);
            } else {
                sak = HexUtil.toHexString(new byte[]{sakBytes[1]});
            }
            String ats = "-";
            IsoDep iso = IsoDep.get(tag);
            if (iso != null) {
                byte[] atsBytes = iso.getHistoricalBytes();
                if (atsBytes != null && atsBytes.length > 0) {
                    ats = HexUtil.toHexString(atsBytes);
                }
            }
            // Identify tag type.
            int tagTypeResourceID = getTagIdentifier(atqa, sak, ats);
            String tagType;
            if (tagTypeResourceID == R.string.tag_unknown && mMFCSupport > -2) {
                tagType = application.getString(R.string.tag_unknown_mf_classic);
            } else {
                tagType = application.getString(tagTypeResourceID);
            }

            File template_tag_info = new File(Paths.COMMON_DIRECTORY + "/" + "template_tag_info_en.html");
            File template_mifare_info = new File(Paths.COMMON_DIRECTORY + "/" + "template_mifare_info_en.html");

            // Check for MIFARE Classic support.
            if (mMFCSupport == 0) {
                // 当前是M1卡!
                // Display MIFARE Classic info.
                // Get MIFARE info and set these as text.
                MifareClassic mfc = MifareClassic.get(tag);
                String size = "" + mfc.getSize();
                String sectorCount = "" + mfc.getSectorCount();
                String blockCount = "" + mfc.getBlockCount();

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

            } else if (mMFCSupport == -2) {
                // 当前非M1卡!
                // The tag does not support MIFARE Classic.
                // Set error message.
                try {
                    //读取本地的HTML模板文件!
                    byte[] templateBytes = FileUtils.readBytes(template_tag_info);
                    String htmlSr = new String(templateBytes);
                    //替换信息!
                    htmlSr = htmlSr.replaceAll("\\$\\{UID\\}", uid);
                    htmlSr = htmlSr.replaceAll("\\$\\{Tech\\}", "ISO/IEC 14443, Type A");
                    htmlSr = htmlSr.replaceAll("\\$\\{ATQA\\}", atqa);
                    htmlSr = htmlSr.replaceAll("\\$\\{SAK\\}", sak);
                    htmlSr = htmlSr.replaceAll("\\$\\{ATS\\}", ats);

                    callback.onInformationsShow(Html.fromHtml(htmlSr));
                } catch (IOException e) {
                    e.printStackTrace();
                    callback.onInformationsShow(application.getString(R.string.msg_temp_file_noexists));
                }
            } else {
                // There is no Tag.
                callback.onInformationsShow(application.getString(R.string.tag_not_found));
            }
        } else {
            Log.d(LOG_TAG, "collect调用，卡片不存在!");
            callback.onInformationsShow(application.getString(R.string.tag_not_found));
        }
    }

    public static int checkMifareClassicSupport(Tag tag) {
        if (tag == null) {
            // Error.
            return -3;
        }
        if (Arrays.asList(tag.getTechList()).contains(
                MifareClassic.class.getName())) {
            // Device and tag support MIFARE Classic.
            return 0;

            // This is no longer valid. There are some devices (e.g. LG's F60)
            // that have this system feature but no MIFARE Classic support.
            // (The F60 has a Broadcom NFC controller.)
        /*
        } else if (context.getPackageManager().hasSystemFeature(
                "com.nxp.mifare")){
            // Tag does not support MIFARE Classic.
            return -2;
        */

        } else {
            // Check if device does not support MIFARE Classic.
            // For doing so, check if the SAK of the tag indicate that
            // it's a MIFARE Classic tag.
            // See: https://www.nxp.com/docs/en/application-note/AN10834.pdf
            NfcA nfca = NfcA.get(tag);
            byte sak = (byte) nfca.getSak();
            if ((sak >> 1 & 1) == 1) {
                // RFU.
                return -2;
            } else {
                if ((sak >> 3 & 1) == 1) { // SAK bit 4 = 1?
                    if ((sak >> 4 & 1) == 1) { // SAK bit 5 = 1?
                        // MIFARE Classic 4k
                        // MIFARE SmartMX 4K
                        // MIFARE PlusS 4K SL1
                        // MIFARE PlusX 4K SL1
                        return -1;
                    } else {
                        if ((sak & 1) == 1) { // SAK bit 1 = 1?
                            // MIFARE Mini
                            return -1;
                        } else {
                            // MIFARE Classic 1k
                            // MIFARE SmartMX 1k
                            // MIFARE PlusS 2K SL1
                            // MIFARE PlusX 2K SL2
                            return -1;
                        }
                    }
                } else {
                    // Some MIFARE tag, but not Classic or Classic compatible.
                    return -2;
                }
            }

            // Old MIFARE Classic support check. No longer valid.
            // Check if the ATQA + SAK of the tag indicate that it's a MIFARE Classic tag.
            // See: http://www.nxp.com/documents/application_note/AN10833.pdf
            // (Table 5 and 6)
            // 0x28 is for some emulated tags.
            /*
            NfcA nfca = NfcA.get(tag);
            byte[] atqa = nfca.getAtqa();
            if (atqa[1] == 0 &&
                    (atqa[0] == 4 || atqa[0] == (byte)0x44 ||
                     atqa[0] == 2 || atqa[0] == (byte)0x42)) {
                // ATQA says it is most likely a MIFARE Classic tag.
                byte sak = (byte)nfca.getSak();
                if (sak == 8 || sak == 9 || sak == (byte)0x18 ||
                                            sak == (byte)0x88 ||
                                            sak == (byte)0x28) {
                    // SAK says it is most likely a MIFARE Classic tag.
                    // --> Device does not support MIFARE Classic.
                    return -1;
                }
            }
            // Nope, it's not the device (most likely).
            // The tag does not support MIFARE Classic.
            return -2;
            */
        }
    }

    /**
     * Get (determine) the tag type resource ID from ATQA + SAK + ATS.
     * If no resource is found check for the tag type only on ATQA + SAK
     * (and then on ATQA only).
     *
     * @param atqa The ATQA from the tag.
     * @param sak  The SAK from the tag.
     * @param ats  The ATS from the tag.
     * @return The resource ID.
     */
    private int getTagIdentifier(String atqa, String sak, String ats) {
        String prefix = "tag_";
        ats = ats.replace("-", "");

        // First check on ATQA + SAK + ATS.
        int ret = application.getResources().getIdentifier(
                prefix + atqa + sak + ats, "string", application.getPackageName());

        if (ret == 0) {
            // Check on ATQA + SAK.
            ret = application.getResources().getIdentifier(
                    prefix + atqa + sak, "string", application.getPackageName());
        }

        if (ret == 0) {
            // Check on ATQA.
            ret = application.getResources().getIdentifier(
                    prefix + atqa, "string", application.getPackageName());
        }

        if (ret == 0) {
            // No match found return "Unknown".
            return R.string.tag_unknown;
        }
        return ret;
    }
}
