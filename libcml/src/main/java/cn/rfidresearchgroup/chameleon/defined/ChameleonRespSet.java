package cn.rfidresearchgroup.chameleon.defined;

import java.util.HashMap;
import java.util.Map;


/**
 * <h1>Serial Response Code</h1>
 * The class SerialRespCode contains extended enum definitions of the possible response
 * codes returned by the devices. Also provides helper methods.
 */

public enum ChameleonRespSet {

    /**
     * List of the status codes and their corresponding text descriptions
     * (taken almost verbatim from the ChameleonMini source code).
     */
    OK(100),
    OK_WITH_TEXT(101),
    WAITING_FOR_MODEM(110),
    TRUE(121),
    FALSE(120),
    UNKNOWN_COMMAND(200),
    INVALID_COMMAND_USAGE(201),
    INVALID_PARAMETER(202),
    TIMEOUT(203);

    /**
     * Integer value associated with each enum value.
     */
    private int responseCode;

    /**
     * Constructor
     *
     * @param rcode
     */
    ChameleonRespSet(int rcode) {
        responseCode = rcode;
    }

    /**
     * Stores a maps of integer-valued response codes to their corresponding enum value.
     */
    private static final Map<Integer, ChameleonRespSet> RESP_CODE_MAP = new HashMap<>();

    static {
        for (ChameleonRespSet respCode : values()) {
            int rcode = respCode.toInteger();
            Integer aRespCode = Integer.valueOf(rcode);
            RESP_CODE_MAP.put(aRespCode, respCode);
        }
    }

    /**
     * Lookup table of String response codes prefixing command return data sent by the devices.
     *
     * @ref ChameleonIO.isCommandResponse
     */
    public static final Map<String, ChameleonRespSet> RESP_CODE_TEXT_MAP = new HashMap<>();
    public static final Map<String, ChameleonRespSet> RESP_CODE_TEXT_MAP2 = new HashMap<>();

    /*
     * init response code mapping
     * */
    static {
        for (ChameleonRespSet respCode : values()) {
            String rcode = String.valueOf(respCode.toInteger());
            String rcodeText = respCode.name().replace("_", " ");
            RESP_CODE_TEXT_MAP.put(rcode + ":" + rcodeText, respCode);
            RESP_CODE_TEXT_MAP2.put(rcode, respCode);
        }
    }

    /**
     * Retrieve the integer-valued response code associated with the enum value.
     *
     * @return int response code
     */
    public int toInteger() {
        return responseCode;
    }

    /**
     * Lookup the enum value by its associated integer response code value.
     *
     * @param rcode
     * @return SerialRespCode enum value associated with the integer code
     */
    public static ChameleonRespSet lookupByResponseCode(int rcode) {
        return RESP_CODE_MAP.get(rcode);
    }
}
