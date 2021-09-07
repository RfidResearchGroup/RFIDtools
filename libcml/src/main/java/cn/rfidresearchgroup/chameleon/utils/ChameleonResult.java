package cn.rfidresearchgroup.chameleon.utils;

import java.util.Arrays;
import java.util.Locale;

import com.rfidresearchgroup.common.util.RegexGroupUtil;
import cn.rfidresearchgroup.chameleon.defined.ChameleonRespSet;

public class ChameleonResult {

    public String issuingCmd;
    public String cmdResponseMsg;
    public String cmdResponseData;
    public int cmdResponseCode;
    public boolean isValid;

    public ChameleonResult() {
        issuingCmd = ChameleonCMDStr.NODATA;
        cmdResponseMsg = "";
        cmdResponseData = ChameleonCMDStr.NODATA;
        cmdResponseCode = -1;
        isValid = false;
    }

    public ChameleonResult(String initCmd) {
        issuingCmd = initCmd;
        cmdResponseMsg = ChameleonCMDStr.NODATA;
        cmdResponseData = ChameleonCMDStr.NODATA;
        cmdResponseCode = -1;
        isValid = false;
    }

    public String toString() {
        return String.format(Locale.ENGLISH, "CMD(%s) => [%d] : %s", issuingCmd, cmdResponseCode, cmdResponseData);
    }

    /**
     * Determines whether the received serial byte data is a command response sent by the devices.
     *
     * @param liveLogData
     * @return boolean whether the log data is a response to an issued command
     */
    public static boolean isCommandResponse(byte[] liveLogData) {
        String[] respLine = new String(liveLogData).split("[\n\r]+");
        if (respLine.length <= 0) return false;
        String respText = respLine[0];
        if (ChameleonRespSet.RESP_CODE_TEXT_MAP.get(respText) != null)
            return true;
        respText = (new String(liveLogData)).split(":")[0];
        if (respText != null &&
                respText.length() >= 3 &&
                ChameleonRespSet.RESP_CODE_TEXT_MAP2.get(respText.substring(respText.length() - 3)) != null)
            return true;
        return false;
    }

    /**
     * Takes as input the byte array returned by the chameleon. If it is in fact a valid
     * command response, we parse it into it's component parts (storing each of them in the
     * public member functions above) and return true if the command response was valid, and
     * false otherwise.
     *
     * @param responseBytes
     * @return boolean-valued truth of whether the input is a valid command response.
     */
    public boolean processCommandResponse(byte[] responseBytes) {
        if (!isCommandResponse(responseBytes)) {
            isValid = false;
            return false;
        }
        String[] splitCmdResp = (new String(responseBytes)).split("[\n\r]+");
        cmdResponseMsg = splitCmdResp[0];
        String code = splitCmdResp[0].split(":")[0];
        if (code == null) return false;
        String codeIntStr = RegexGroupUtil.matcherGroup(code, ".*([0-9]{3}).*", 1, 0);
        if (codeIntStr == null) return false;
        cmdResponseCode = Integer.parseInt(codeIntStr);
        if (splitCmdResp.length >= 2) {
            cmdResponseData = ChameleonUtils.stringJoin("\n", Arrays.copyOfRange(splitCmdResp, 1, splitCmdResp.length));
        } else {
            cmdResponseData = ChameleonCMDStr.NODATA;
        }
        isValid = (cmdResponseCode == ChameleonRespSet.OK.toInteger())
                || (cmdResponseCode == ChameleonRespSet.OK_WITH_TEXT.toInteger()
                || cmdResponseCode == ChameleonRespSet.WAITING_FOR_MODEM.toInteger());
        return true;
    }

}
