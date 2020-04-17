package cn.rrg.chameleon.utils;

import com.iobridges.com.Communication;

import java.io.IOException;

import cn.dxl.common.util.RegexGroupUtil;
import cn.rrg.chameleon.javabean.ResultBean;
import cn.rrg.chameleon.defined.BasicTypesCallback;
import cn.rrg.chameleon.defined.ChameleonCMDSet;
import cn.rrg.chameleon.defined.ChameleonClick;
import cn.rrg.chameleon.defined.ChameleonRespSet;
import cn.rrg.chameleon.defined.ChameleonSlot;
import cn.rrg.chameleon.defined.IChameleonExecutor;
import cn.rrg.chameleon.defined.ResultCallback;
import cn.rrg.chameleon.executor.ExecutorImpl;

/**
 * 变色龙由底层数据到具体UI的中转工具!!!
 *
 * @author DXL
 */
public class ChameleonVCUtil {

    private static String LOG_TAG = ChameleonVCUtil.class.getSimpleName();

    //持有一个对象,进行命令执行!
    private IChameleonExecutor executor = ExecutorImpl.getInstance();

    /**
     * 当前支持的命令!
     * VERSIONMY,
     * CONFIGMY,
     * UIDMY,
     * READONLYMY,
     * UPLOADMY,
     * DOWNLOADMY,
     * RESETMY,
     * UPGRADEMY,
     * MEMSIZEMY,
     * UIDSIZEMY,
     * BUTTONMY,
     * SETTINGMY,
     * CLEARMY,
     * HELPMY,
     * RSSIMY
     */

    public void executeDataCMD(String cmdStr, int timeout, boolean xmodemMode, ResultCallback<ChameleonResult, String> callback) {
        if (cmdStr != null) {
            byte[] result = executor.requestChameleon(cmdStr, timeout, xmodemMode);
            if (result == null) {
                callback.onFaild("得到的结果集字节为空，请检查通信或命令是否正确!");
                return;
            }
            //进行结果解析!
            ChameleonResult chameleonResult = new ChameleonResult(cmdStr);
            //Log.d(LOG_TAG, chameleonResult.toString());
            if (chameleonResult.processCommandResponse(result)) {
                callback.onSuccess(chameleonResult);
            } else {
                callback.onFaild("应答值解析失败，可能是由于频繁操作的原因!");
            }
        } else {
            callback.onFaild("命令取出失败!");
        }
    }

    public void executeDataCMD(ChameleonCMDSet cmd, int timeout, boolean xmodemMode, ResultCallback<ChameleonResult, String> callback, Object... format) {
        String cmdStr = ChameleonCMDStr.getCMD4E(cmd, format);
        executeDataCMD(cmdStr, timeout, xmodemMode, callback);
    }

    public void executeNoDataCMD(String cmdStr, int timeout, boolean xmodemMode, final ResultCallback<String, String> callback) {
        executeDataCMD(cmdStr, timeout, xmodemMode, new ResultCallback<ChameleonResult, String>() {
            @Override
            public void onSuccess(ChameleonResult chameleonResult) {
                //在成功后!
                if (chameleonResult.isValid) {
                    callback.onSuccess("执行成功!");
                } else {
                    callback.onFaild("执行失败!");
                }
            }

            @Override
            public void onFaild(String s) {
                callback.onFaild(s);
            }
        });
    }

    public void executeNoDataCMD(ChameleonCMDSet cmd, int timeout, boolean xmodemMode, ResultCallback<String, String> callback) {
        String cmdStr = ChameleonCMDStr.getCMD4E(cmd);
        executeNoDataCMD(cmdStr, timeout, xmodemMode, callback);
    }

    public void getSlotNumber(final BasicTypesCallback.IntegerType type) {
        //发送命令，获取当前的插槽!
        executeDataCMD(ChameleonCMDSet.GET_ACTIVE_SLOT, 1000, false, new ResultCallback<ChameleonResult, String>() {
            @Override
            public void onSuccess(ChameleonResult chameleonResult) {
                //得到消息体!
                String no = RegexGroupUtil.matcherGroup(chameleonResult.cmdResponseData, ".*([0-9]).*", 1, 0);
                if (no != null) {
                    //进行转化并且回调!
                    type.onInt(Integer.valueOf(no));
                } else {
                    type.onInt(-1);
                }
            }

            @Override
            public void onFaild(String s) {
                type.onInt(-1);
            }
        });
    }

    public void setSlotNumber(final int no, final ResultCallback<String, String> callback) {
        if (no < 0 || no > 7) {
            callback.onFaild("切换失败,给定的数值不符合槽位设置条件!");
            return;
        }
        executeDataCMD(ChameleonCMDSet.SET_ACTIVE_SLOT, 1000, false, new ResultCallback<ChameleonResult, String>() {
            @Override
            public void onSuccess(ChameleonResult chameleonResult) {
                if (chameleonResult.isValid) {
                    //有效,说明我们的操作生效了!
                    callback.onSuccess("操作成功，新的槽位被设置为: " + (no + 1));
                } else {
                    callback.onFaild("操作失败，槽位可能设置不成功!");
                }
            }

            @Override
            public void onFaild(String s) {
                callback.onFaild(s);
            }
        }, no);
    }

    public void getVerson(final ResultCallback<String, String> callback) {
        executeDataCMD(ChameleonCMDSet.GET_VERSION, 1000, false, new ResultCallback<ChameleonResult, String>() {
            @Override
            public void onSuccess(ChameleonResult chameleonResult) {
                if (chameleonResult.isValid) {
                    //有效,说明我们的操作生效了!
                    callback.onSuccess(chameleonResult.cmdResponseData);
                } else {
                    callback.onFaild("数据体获取失败!");
                }
            }

            @Override
            public void onFaild(String s) {
                callback.onFaild(s);
            }
        });
    }

    public void resetSlotDatas(ResultCallback<String, String> callback) {
        executeNoDataCMD(ChameleonCMDSet.CLEAR_ACTIVE_SLOT, 2000, false, callback);
    }

    public void resetDevice(ResultCallback<String, String> callback) {
        executeNoDataCMD(ChameleonCMDSet.RESET_DEVICE, 2000, false, callback);
    }

    public void dowmloadDump(ResultCallback<String, String> callback) {
        executeNoDataCMD(ChameleonCMDSet.DOWNLOAD_XMODEM, 1000, true, callback);
    }

    public void uploadDump(final ResultCallback<String, String> callback) {
        String cmd = ChameleonCMDStr.getCMD4E(ChameleonCMDSet.SET_READONLY, 0);
        executeNoDataCMD(cmd, 1000, false, new ResultCallback<String, String>() {
            @Override
            public void onSuccess(String s) {
                executeNoDataCMD(ChameleonCMDSet.UPLOAD_XMODEM, 1000, true, callback);
            }

            @Override
            public void onFaild(String s) {
                callback.onFaild(s);
            }
        });
    }

    public void getMode(final BasicTypesCallback.IntegerType type) {
        executeDataCMD(ChameleonCMDSet.QUERY_CONFIG, 1000, false, new ResultCallback<ChameleonResult, String>() {
            @Override
            public void onSuccess(ChameleonResult chameleonResult) {
                //成功了，此时我们需要进行数据体处理!
                //Log.d(LOG_TAG, "当前槽位的模式: " + chameleonResult.cmdResponseData);
                // case CLOSED,MF_ULTRALIGHT,MF_CLASSIC_1K,MF_CLASSIC_4K,MF_DETECTION
                switch (chameleonResult.cmdResponseData) {
                    case ChameleonSlot.STATE_CLOSED:
                        type.onInt(0);
                        break;
                    case ChameleonSlot.STATE_UL:
                        type.onInt(1);
                        break;
                    case ChameleonSlot.STATE_1K:
                        type.onInt(2);
                        break;
                    case ChameleonSlot.STATE_4K:
                        type.onInt(3);
                        break;
                    case ChameleonSlot.STATE_DETECTION:
                        type.onInt(4);
                        break;
                    default:
                        type.onInt(-1);
                        break;
                }
            }

            @Override
            public void onFaild(String s) {
                type.onInt(-1);
            }
        });
    }

    public void setMode(String arg, final ResultCallback<String, String> callback) {
        executeDataCMD(ChameleonCMDSet.SET_CONFIG, 1000, false, new ResultCallback<ChameleonResult, String>() {
            @Override
            public void onSuccess(ChameleonResult chameleonResult) {
                callback.onSuccess("模式获取成功!");
            }

            @Override
            public void onFaild(String s) {
                callback.onFaild(s);
            }
        }, arg);
    }

    public void getClick(final BasicTypesCallback.IntegerType type) {
        executeDataCMD(ChameleonCMDSet.GET_BUTTON_CLICK, 1000, false, new ResultCallback<ChameleonResult, String>() {
            @Override
            public void onSuccess(ChameleonResult chameleonResult) {
                //成功了，此时我们需要进行数据体处理!
                //Log.d(LOG_TAG, "当前单击事件: " + cr.cmdResponseData);
                // case CLOSED,RANDOM_UID,UID_LEFT_INCREMENT,UID_RIGHT_INCREMENT,UID_LEFT_DECREMENT,UID_RIGHT_DECREMENT,SWITCHCARD
                switch (chameleonResult.cmdResponseData) {
                    case ChameleonClick.CLICK_SWITCHCARD:
                        type.onInt(0);
                        break;
                    case ChameleonClick.CLICK_RANDOM_UID:
                        type.onInt(1);
                        break;
                    case ChameleonClick.CLICK_UID_LEFT_INCREMENT:
                        type.onInt(2);
                        break;
                    case ChameleonClick.CLICK_UID_RIGHT_INCREMENT:
                        type.onInt(3);
                        break;
                    case ChameleonClick.CLICK_UID_LEFT_DECREMENT:
                        type.onInt(4);
                        break;
                    case ChameleonClick.CLICK_UID_RIGHT_DECREMENT:
                        type.onInt(5);
                        break;
                    case ChameleonClick.CLICK_CLOSED:
                        type.onInt(6);
                        break;
                    default:
                        type.onInt(-1);
                        break;
                }
            }

            @Override
            public void onFaild(String s) {
                type.onInt(-1);
            }
        });
    }

    public void setClick(String arg, final ResultCallback<String, String> callback) {
        executeDataCMD(ChameleonCMDSet.SET_BUTTON_CLICK, 1000, false, new ResultCallback<ChameleonResult, String>() {
            @Override
            public void onSuccess(ChameleonResult chameleonResult) {
                callback.onSuccess("按键动作设置成功!");
            }

            @Override
            public void onFaild(String s) {
                callback.onFaild(s);
            }
        }, arg);
    }

    public void getSize(final BasicTypesCallback.IntegerType type) {
        executeDataCMD(ChameleonCMDSet.GET_MEMORY_SIZE, 1000, false, new ResultCallback<ChameleonResult, String>() {
            @Override
            public void onSuccess(ChameleonResult chameleonResult) {
                switch (chameleonResult.cmdResponseData) {
                    case "0":
                        type.onInt(0);
                        break;
                    case "64":
                        type.onInt(64);
                        break;
                    case "1024":
                        type.onInt(1024);
                        break;
                    case "4096":
                        type.onInt(4096);
                        break;
                }
            }

            @Override
            public void onFaild(String s) {
                type.onInt(-1);
            }
        });
    }

    public void getUID(final ResultCallback<String, String> callback) {
        executeDataCMD(ChameleonCMDSet.QUERY_UID, 1000, false, new ResultCallback<ChameleonResult, String>() {
            @Override
            public void onSuccess(ChameleonResult chameleonResult) {
                callback.onSuccess(chameleonResult.cmdResponseData);
            }

            @Override
            public void onFaild(String s) {
                callback.onFaild(s);
            }
        });
    }

    public void setUID(String hexUID, final ResultCallback<String, String> callback) {
        executeDataCMD(ChameleonCMDSet.SET_UID, 1000, false, new ResultCallback<ChameleonResult, String>() {
            @Override
            public void onSuccess(ChameleonResult chameleonResult) {
                if (chameleonResult.cmdResponseCode == ChameleonRespSet.OK.toInteger()) {
                    //Log.d(LOG_TAG, chameleonResult.toString());
                    callback.onSuccess("UID设置成功!");
                } else {
                    callback.onSuccess("UID设置失败!");
                }
            }

            @Override
            public void onFaild(String s) {
                callback.onFaild("UID设置失败!");
            }
        }, hexUID);
    }

    public void getHelp(final ResultCallback<String, String> callback) {
        executeDataCMD(ChameleonCMDSet.HELP, 1000, false, new ResultCallback<ChameleonResult, String>() {
            @Override
            public void onSuccess(ChameleonResult chameleonResult) {
                callback.onSuccess(chameleonResult.cmdResponseData);
            }

            @Override
            public void onFaild(String s) {
                callback.onFaild(s);
            }
        });
    }

    public void detectMFDecrypt(final ResultCallback<ResultBean, String> callback, boolean all) {
        String cmdStr = ChameleonCMDStr.getCMD4E(ChameleonCMDSet.DETECTION);
        try {
            //执行命令
            executor.requestChameleon(cmdStr);
            //接收指定长度的数据
            Communication com = executor.getCom();
            byte[] buf = new byte[218];
            //开始接收!
            com.getInput().read(buf, 0, buf.length);
            //开始进行解密!
            new ChameleonDetection(new ChameleonDetection.DecryptCallback() {
                @Override
                public void onMsg(String msg) {
                    callback.onFaild(msg);
                }

                @Override
                public void onKey(ResultBean result) {
                    callback.onSuccess(result);
                }
            }).decrypt(buf, all);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
