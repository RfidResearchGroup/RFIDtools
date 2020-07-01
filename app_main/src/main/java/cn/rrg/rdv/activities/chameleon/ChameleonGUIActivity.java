package cn.rrg.rdv.activities.chameleon;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatSpinner;

import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.iobridges.com.Communication;
import com.proxgrind.xmodem.XModem128;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import cn.dxl.common.util.FileUtils;
import cn.dxl.common.util.IOUtils;
import cn.dxl.common.widget.FilesSelectorDialog;
import cn.rrg.chameleon.defined.BasicTypesCallback;
import cn.rrg.chameleon.defined.ChameleonCMDSet;
import cn.rrg.chameleon.utils.ChameleonCMDStr;
import cn.rrg.chameleon.defined.ChameleonClick;
import cn.rrg.chameleon.utils.ChameleonResult;
import cn.rrg.chameleon.defined.ChameleonSlot;
import cn.rrg.chameleon.utils.ChameleonVCUtil;
import cn.rrg.chameleon.executor.ExecutorImpl;
import cn.rrg.chameleon.javabean.ResultBean;
import cn.rrg.chameleon.defined.ResultCallback;
import cn.rrg.rdv.activities.main.BaseActivity;
import cn.rrg.rdv.activities.tools.ChameleonSoltAliasesActivity;
import cn.rrg.rdv.application.Properties;
import cn.rrg.rdv.R;
import cn.rrg.rdv.callback.DumpCallback;
import cn.rrg.rdv.models.DumpModel;
import cn.dxl.common.widget.FillParentWidthDialog;
import cn.rrg.rdv.util.Commons;
import cn.rrg.rdv.util.Paths;
import cn.dxl.common.util.RestartUtils;
import cn.dxl.common.util.SpinnerInitState;
import cn.dxl.common.util.StringUtil;
import cn.dxl.common.widget.ToastUtil;
import cn.dxl.common.util.HexUtil;
import cn.dxl.common.implement.ItemSelectedListenerImpl;
import cn.dxl.common.util.ViewUtil;

/**
 * @author DXL
 * Chameleon View!
 */
public class ChameleonGUIActivity
        extends BaseActivity
        implements View.OnClickListener {

    private Button btnChameleon_Information_Dowmload;
    private Button btnChameleon_UID_SET;
    private EditText edtChameleon_UID_INPUT;
    private Button btnChameleon_Datas_Dowmload;
    private Button btnChameleon_Datas_Upload;
    private Button btnChameleon_Datas_Reset;
    private Button btnChameleon_Device_Reset;
    private Button btnChameleon_Sniff_Decrypt;
    private Button btnChameleon_Sniff_Decrypt_One;
    private Button btnChameleon_Sniff_Decrypt_Log;
    private Button btnChameleon_Version_Show;
    private Button btnChameleon_Slot_Aliases;

    private AppCompatSpinner spChameleon_Slot_Select;
    private AppCompatSpinner spChameleon_Mode_Select;
    private AppCompatSpinner spChameleon_Click_Select;
    private TextView txtChameleon_Size_Show;

    private EditText edtChameleon_Command_Input;
    private Button btnChameleon_Command_Send;
    private Button btnChameleon_Command_Show;
    private View dialogConsoleView;
    private TextView txtShowConsoleMsg;
    private FillParentWidthDialog fpDialog;

    //VC中转对象!
    private ChameleonVCUtil util;
    //上下文
    private Context context = this;
    //回调接口的实例!
    private ResultCallback<String, String> msgCallbacl = new ResultCallback<String, String>() {
        @Override
        public void onSuccess(String s) {
            showToast(s);
        }

        @Override
        public void onFaild(String s) {
            showToast(s);
        }
    };
    //任务进行中的进度对话框!
    private AlertDialog progressDialog;
    //是否是一键设置导致的更新!
    private boolean isOneKeyGetInfo = false;
    //解密过程中展现的日志!
    private AlertDialog mMsgDialog;
    //日志的ListView适配器
    private ArrayAdapter<String> mMsgAdapter;
    //保存嗅探结果!
    private List<ResultBean> resultBeanList = new ArrayList<>();
    //spinner选择状态
    private SpinnerInitState spinnerInitState = new SpinnerInitState();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setContentView(R.layout.act_chameleon_main);
        super.onCreate(savedInstanceState);

        initViews();
        initActions();

        util = new ChameleonVCUtil();

        fpDialog = new FillParentWidthDialog(this);
        fpDialog.setView(dialogConsoleView);

        progressDialog = new AlertDialog.Builder(this).create();
        progressDialog.setView(ViewUtil.inflate(this, R.layout.dialog_working_msg));
        progressDialog.setTitle(getString(R.string.working));
        progressDialog.setCancelable(false);

        ListView msgListView = new ListView(this);
        mMsgAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        msgListView.setAdapter(mMsgAdapter);
        mMsgDialog = new AlertDialog.Builder(this).create();
        mMsgDialog.setView(msgListView);
        mMsgDialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.ok), (DialogInterface.OnClickListener) null);
        mMsgDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.clear), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mMsgAdapter.clear();
                mMsgAdapter.notifyDataSetChanged();
            }
        });
        mMsgDialog.setButton(DialogInterface.BUTTON_NEUTRAL, getString(R.string.save), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (resultBeanList.size() == 0) {
                    showToast(getString(R.string.msg_not_data));
                    return;
                }
                String uid = edtChameleon_UID_INPUT.getText().toString();
                if (!HexUtil.isHexString(uid)) uid = "chameleon.txt";
                File keyFile = new File(Paths.KEY_DIRECTORY + "/" + uid + "txt");
                if (!keyFile.exists()) {
                    try {
                        if (!keyFile.createNewFile()) {
                            showToast(getString(R.string.msg_create_chameleon_keyfile_err));
                            return;
                        }
                        //进行迭代，追加写入!
                        FileOutputStream fos = FileUtils.getFos(keyFile, true);
                        for (ResultBean bean : resultBeanList) {
                            String key = bean.getKey() + "\n";
                            fos.write(key.getBytes());
                        }
                        //谨记关闭输出流!
                        IOUtils.close(fos);
                        showToast(getString(R.string.finish));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        //第一次创建的时候加载一下信息!
        btnChameleon_Information_Dowmload.performClick();
    }

    private void initViews() {
        dialogConsoleView = ViewUtil.inflate(this, R.layout.dialog_msg_layout);
        btnChameleon_Information_Dowmload = findViewById(R.id.btnChameleon_Information_Dowmload);
        btnChameleon_Slot_Aliases = findViewById(R.id.btnChameleon_Slot_Aliases);
        btnChameleon_UID_SET = findViewById(R.id.btnChameleon_UID_Set);
        edtChameleon_UID_INPUT = findViewById(R.id.edtChameleon_UID_Input);
        btnChameleon_Datas_Dowmload = findViewById(R.id.btnChameleon_Datas_Dowmload);
        btnChameleon_Datas_Upload = findViewById(R.id.btnChameleon_Datas_Upload);
        btnChameleon_Datas_Reset = findViewById(R.id.btnChameleon_Datas_Reset);
        btnChameleon_Device_Reset = findViewById(R.id.btnChameleon_Device_Reset);
        btnChameleon_Sniff_Decrypt = findViewById(R.id.btnChameleon_Sniff_Decrypt);
        btnChameleon_Sniff_Decrypt_One = findViewById(R.id.btnChameleon_Sniff_Decrypt_One);
        btnChameleon_Sniff_Decrypt_Log = findViewById(R.id.btnChameleon_Sniff_Decrypt_Log);
        btnChameleon_Version_Show = findViewById(R.id.btnChameleon_Version_Show);
        txtChameleon_Size_Show = findViewById(R.id.txtChameleon_Size_Show);
        btnChameleon_Command_Show = findViewById(R.id.btnChameleon_Command_Show);
        edtChameleon_Command_Input = dialogConsoleView.findViewById(R.id.edtChameleon_Command_Input);
        btnChameleon_Command_Send = dialogConsoleView.findViewById(R.id.btnChameleon_Command_Send);
        txtShowConsoleMsg = dialogConsoleView.findViewById(R.id.txtDialogShowMsg);

        spChameleon_Slot_Select = findViewById(R.id.spChameleon_Slot_Select);
        spChameleon_Mode_Select = findViewById(R.id.spChameleon_Mode_Select);
        spChameleon_Click_Select = findViewById(R.id.spChameleon_Click_Select);
    }

    private void initActions() {
        btnChameleon_Information_Dowmload.setOnClickListener(this);
        btnChameleon_UID_SET.setOnClickListener(this);
        btnChameleon_Datas_Dowmload.setOnClickListener(this);
        btnChameleon_Datas_Upload.setOnClickListener(this);
        btnChameleon_Datas_Reset.setOnClickListener(this);
        btnChameleon_Sniff_Decrypt.setOnClickListener(this);
        btnChameleon_Sniff_Decrypt_One.setOnClickListener(this);
        btnChameleon_Sniff_Decrypt_Log.setOnClickListener(this);
        btnChameleon_Command_Send.setOnClickListener(this);
        btnChameleon_Command_Show.setOnClickListener(this);
        btnChameleon_Version_Show.setOnClickListener(this);
        btnChameleon_Device_Reset.setOnClickListener(this);
        btnChameleon_Slot_Aliases.setOnClickListener(this);

        spChameleon_Slot_Select.setOnItemSelectedListener(new ItemSelectedListenerImpl() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (spinnerInitState.isNotInitialized(spChameleon_Slot_Select)) return;
                if (!isOneKeyGetInfo) setSlotNum(position);
            }
        });
        spChameleon_Mode_Select.setOnItemSelectedListener(new ItemSelectedListenerImpl() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (spinnerInitState.isNotInitialized(spChameleon_Mode_Select)) return;
                if (!isOneKeyGetInfo) setMode(position);
            }
        });
        spChameleon_Click_Select.setOnItemSelectedListener(new ItemSelectedListenerImpl() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (spinnerInitState.isNotInitialized(spChameleon_Click_Select)) return;
                if (!isOneKeyGetInfo) setClick(position);
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnChameleon_Information_Dowmload:
                getInfo();
                break;

            case R.id.btnChameleon_UID_Set:
                setUid();
                break;

            case R.id.btnChameleon_Datas_Dowmload:
                download();
                break;

            case R.id.btnChameleon_Datas_Upload:
                upload();
                break;

            case R.id.btnChameleon_Datas_Reset:
                reset();
                break;

            case R.id.btnChameleon_Sniff_Decrypt:
                decrypt(true);
                break;

            case R.id.btnChameleon_Sniff_Decrypt_One:
                decrypt(false);
                break;

            case R.id.btnChameleon_Command_Send:
                sendAT();
                break;

            case R.id.btnChameleon_Command_Show:
                //通信对话框显示!!!
                fpDialog.show();
                break;

            case R.id.btnChameleon_Version_Show:
                showVer();
                break;

            case R.id.btnChameleon_Device_Reset:
                new AlertDialog.Builder(this)
                        .setTitle(R.string.tips)
                        .setMessage(getString(R.string.msg_chameleon_reboot))
                        .setNegativeButton(R.string.cancel, null)
                        .setPositiveButton(getString(R.string.reset), (dialog, which) -> {
                            util.resetDevice(new ResultCallback<String, String>() {
                                @Override
                                public void onSuccess(String s) {
                                    dialog.dismiss();
                                    RestartUtils.restartAPP(context, 1000, () -> true);
                                }

                                @Override
                                public void onFaild(String s) {
                                    showToast(s);
                                }
                            });
                        }).show();
                break;

            case R.id.btnChameleon_Sniff_Decrypt_Log:
                if (mMsgAdapter.getCount() == 0) {
                    showToast(getString(R.string.msg_no_history));
                } else {
                    mMsgDialog.show();
                }
                break;

            case R.id.btnChameleon_Slot_Aliases:
                startActivity(new Intent(this, ChameleonSoltAliasesActivity.class));
                break;

            default:
                //what are you doing???
                break;
        }
    }

    private void setUid() {
        //UID设置!
        String uidInput = edtChameleon_UID_INPUT.getText().toString();
        if (StringUtil.isEmpty(uidInput) || !StringUtil.isHexStr(uidInput)) {
            showToast(getString(R.string.msg_uid_input_err));
        } else {
            showProgressDialog();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    util.setUID(uidInput, msgCallbacl);
                    dismissProgressDialog();
                }
            }).start();
        }
    }

    private void getInfo() {
        new Thread(() -> {
            showProgressDialog();
            isOneKeyGetInfo = true;
            //槽位信息下载!
            util.getSlotNumber(new BasicTypesCallback.IntegerTypeEntry() {
                @Override
                public void onInt(int i) {
                    //判断是否有效!
                    if (i == -1) {
                        showToast(getString(R.string.msg_slot_get_failed));
                    } else {
                        //进行槽位UI设置!
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                spChameleon_Slot_Select.setSelection(i);
                            }
                        });
                    }
                }
            });
            //模式信息下载!
            util.getMode(new BasicTypesCallback.IntegerTypeEntry() {
                @Override
                public void onInt(int i) {
                    if (i == -1) {
                        showToast(getString(R.string.msg_mode_get_failed));
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                spChameleon_Mode_Select.setSelection(i);
                            }
                        });
                    }
                }
            });
            //按钮单击信息下载
            util.getClick(new BasicTypesCallback.IntegerTypeEntry() {
                @Override
                public void onInt(int i) {
                    if (i == -1) {
                        showToast(getString(R.string.msg_get_click_err));
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                spChameleon_Click_Select.setSelection(i);
                            }
                        });
                    }
                }
            });
            //内存大小下载!
            util.getSize(new BasicTypesCallback.IntegerTypeEntry() {
                @Override
                public void onInt(int i) {
                    if (i == -1) {
                        showToast(getString(R.string.tips_size_get_failed));
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                txtChameleon_Size_Show.setText(String.valueOf(i));
                            }
                        });
                    }
                }
            });
            //UID下载!
            util.getUID(new ResultCallback<String, String>() {
                @Override
                public void onSuccess(String s) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            edtChameleon_UID_INPUT.setText(s);
                        }
                    });
                }

                @Override
                public void onFaild(String s) {
                    showToast(s);
                }
            });
            isOneKeyGetInfo = false;
            dismissProgressDialog();
        }).start();
    }

    private void download() {
        new Thread(() -> {
            //数据下载!
            showProgressDialog();
            util.dowmloadDump(new ResultCallback<String, String>() {
                @Override
                public void onSuccess(String s) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            String[] sizeList = {"MF_1K", "MF_4K", "MF_UL"};
                            new AlertDialog.Builder(context)
                                    .setTitle(R.string.tips_download_size_select)
                                    .setCancelable(false)
                                    .setSingleChoiceItems(sizeList, 0, null)
                                    .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            which = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
                                            int size = 1024;
                                            switch (which) {
                                                case 0:
                                                    size = 1024;
                                                    break;
                                                case 1:
                                                    size = 4096;
                                                    break;
                                                case 2:
                                                    size = 320;
                                                    break;
                                            }
                                            downloadFinallly(size);
                                        }
                                    }).show();
                        }
                    });
                }

                @Override
                public void onFaild(String s) {
                    showToast(s);
                    dismissProgressDialog();
                }
            });
        }).start();
    }

    private void downloadFinallly(int size) {
        //开始执行下载，下载到本地!
        Communication com = ExecutorImpl.getInstance().getCom();
        File file = new File(Paths.DUMP_DIRECTORY + "/" + "Chameleon.bin");
        if (!file.exists()) {
            try {
                boolean b = file.createNewFile();
                if (!b) {
                    showToast(getString(R.string.failed));
                    dismissProgressDialog();
                    return;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                //文件处理完成，进行接下来的下载!
                XModem128 modem = new XModem128(com.getInput(), com.getOutput());
                try {
                    ByteArrayOutputStream bos = new ByteArrayOutputStream(1024 * 64);
                    boolean b = modem.recv(bos);
                    if (b) {
                        showToast(getString(R.string.successful));
                    } else {
                        showToast(getString(R.string.failed));
                    }
                    byte[] result = Arrays.copyOfRange(bos.toByteArray(), 0, size);
                    FileUtils.writeBytes(result, file, false);
                    Commons.gotoDumpEdit(ChameleonGUIActivity.this, file);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                dismissProgressDialog();
            }
        }).start();
    }

    private void upload() {
        new Thread(() -> {
            showProgressDialog();
            //开启数据上载!
            util.uploadDump(new ResultCallback<String, String>() {
                @Override
                public void onSuccess(String s) {
                    dismissProgressDialog();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //文件处理完成，进行接下来的下载!
                            Communication com = ExecutorImpl.getInstance().getCom();
                            XModem128 modem = new XModem128(com.getInput(), com.getOutput());
                            new FilesSelectorDialog.Builder(context)
                                    .setTitle(R.string.tips_data_select)
                                    .setCancelable(false)
                                    .setCanDismiss(false)
                                    .setPathOnLoad(Paths.DUMP_DIRECTORY)
                                    .setOnSelectListener(file -> {
                                        new Thread(new Runnable() {
                                            @Override
                                            public void run() {
                                                showProgressDialog();
                                                try {
                                                    DumpModel.showContents(file, new DumpCallback() {
                                                        @Override
                                                        public void showContents(String[] contents) {
                                                            try {
                                                                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                                                                for (String block : contents) {
                                                                    bos.write(HexUtil.hexStringToByteArray(block));
                                                                }
                                                                ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
                                                                if (modem.send(bis)) {
                                                                    showToast(getString(R.string.success));
                                                                } else {
                                                                    showToast(getString(R.string.failed));
                                                                }
                                                            } catch (IOException ioe) {
                                                                ioe.printStackTrace();
                                                            }
                                                        }

                                                        @Override
                                                        public void onFileException() {
                                                            showToast(getString(R.string.file_err));
                                                        }

                                                        @Override
                                                        public void onFormatNoSupport() {
                                                            showToast(getString(R.string.format_no_support));
                                                        }

                                                        @Override
                                                        public void onSuccess() {

                                                        }
                                                    });
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }
                                                dismissProgressDialog();
                                            }
                                        }).start();
                                    })
                                    .setOnNoSelectListener(new FilesSelectorDialog.OnNoSelectListener() {
                                        @Override
                                        public void noSelect() {
                                            cancelXmodem(modem);
                                            showToast("没有选择文件，将关闭通道!");
                                            dismissProgressDialog();
                                        }
                                    }).create().show();
                        }
                    });
                }

                @Override
                public void onFaild(String s) {
                    showToast("进入上载模式失败!");
                    dismissProgressDialog();
                }
            });
        }).start();
    }

    private void cancelXmodem(XModem128 modem) {
        try {
            modem.cancel();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendAT() {
        //命令发送!
        String cmdInput = edtChameleon_Command_Input.getText().toString();
        if (StringUtil.isEmpty(cmdInput)) {
            showToast("输入异常!");
        } else {
            if (cmdInput.equals(ChameleonCMDStr.getCMD4E(ChameleonCMDSet.UPLOAD_XMODEM))
                    || cmdInput.equals(ChameleonCMDStr.getCMD4E(ChameleonCMDSet.DOWNLOAD_XMODEM))) {
                showToast("警告!");
                txtShowConsoleMsg.append("当前控制台仅支持普通命令，无法进行xmodem链接，请不要输入相关命令!!!\r\r");
                return;
            }
            new Thread(new Runnable() {
                @Override
                public void run() {
                    showProgressDialog();
                    util.executeDataCMD(cmdInput, 2000, false, new ResultCallback<ChameleonResult, String>() {
                        @Override
                        public void onSuccess(ChameleonResult chameleonResult) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    txtShowConsoleMsg.append(chameleonResult.toString() + "\n");
                                }
                            });
                            dismissProgressDialog();
                        }

                        @Override
                        public void onFaild(String s) {
                            showToast("执行失败!");
                            dismissProgressDialog();
                        }
                    });
                }
            }).start();
        }
    }

    private void reset() {
        showProgressDialog();
        //数据重置!
        new Thread(new Runnable() {
            @Override
            public void run() {
                util.resetSlotDatas(msgCallbacl);
                dismissProgressDialog();
            }
        }).start();
    }

    private void decrypt(boolean all) {
        showProgressDialog();
        //先清除历史数据
        resultBeanList.clear();
        mMsgAdapter.clear();
        mMsgAdapter.notifyDataSetChanged();
        //侦测解密!
        new Thread(new Runnable() {
            @Override
            public void run() {
                //开始解密，调用工具类!
                util.detectMFDecrypt(new ResultCallback<ResultBean, String>() {
                    @Override
                    public void onSuccess(ResultBean resultBean) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                resultBeanList.add(resultBean);
                                mMsgAdapter.add(resultBean.toString());
                                mMsgAdapter.notifyDataSetChanged();
                            }
                        });
                    }

                    @Override
                    public void onFaild(String s) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mMsgAdapter.add(s);
                                mMsgAdapter.notifyDataSetChanged();
                            }
                        });
                    }
                }, all);
                //暂时显示对话框!
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mMsgDialog.show();
                    }
                });
                dismissProgressDialog();
            }
        }).start();
    }

    private void showVer() {
        showProgressDialog();
        new Thread(new Runnable() {
            @Override
            public void run() {
                StringBuilder verInfo = new StringBuilder();
                verInfo.append("版本: ");
                util.getVerson(new ResultCallback<String, String>() {
                    @Override
                    public void onSuccess(String s) {
                        verInfo.append(s);
                    }

                    @Override
                    public void onFaild(String s) {
                        verInfo.append(s);
                    }
                });
                verInfo.append('\n');
                verInfo.append("支持的命令: \n");
                util.getHelp(new ResultCallback<String, String>() {
                    @Override
                    public void onSuccess(String s) {
                        String[] cmdList = s.split(",");
                        for (int i = 0; i < cmdList.length; ++i) {
                            verInfo.append(String.format(Locale.CHINA, "%02d", i + 1))
                                    .append('、')
                                    .append(cmdList[i])
                                    .append('\n');
                        }
                    }

                    @Override
                    public void onFaild(String s) {
                        verInfo.append(s);
                    }
                });
                verInfo.append('\n');
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        new AlertDialog.Builder(context)
                                .setTitle("基本固件信息!")
                                .setMessage(verInfo.toString())
                                .show();
                    }
                });
                dismissProgressDialog();
            }
        }).start();
    }

    private void setSlotNum(int pos) {
        showProgressDialog();
        new Thread(new Runnable() {
            @Override
            public void run() {
                util.setSlotNumber(pos, msgCallbacl);
                //卡槽设置完毕后自动下载全部的信息!
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        btnChameleon_Information_Dowmload.performClick();
                    }
                });
                dismissProgressDialog();
            }
        }).start();
    }

    private void setMode(int pos) {
        showProgressDialog();
        new Thread(new Runnable() {
            @Override
            public void run() {
                String mode = ChameleonSlot.STATE_DETECTION;
                //非首次执行(自动)则进行命令发送，切换模式!
                switch (pos) {
                    case 0:
                        mode = ChameleonSlot.STATE_CLOSED;
                        break;
                    case 1:
                        mode = ChameleonSlot.STATE_UL;
                        break;
                    case 2:
                        mode = ChameleonSlot.STATE_1K;
                        break;
                    case 3:
                        mode = ChameleonSlot.STATE_4K;
                        break;
                    case 4:
                        mode = ChameleonSlot.STATE_DETECTION;
                        break;
                }
                util.setMode(mode, msgCallbacl);
                dismissProgressDialog();
            }
        }).start();
    }

    private void setClick(int pos) {
        showProgressDialog();
        new Thread(new Runnable() {
            @Override
            public void run() {
                String click = ChameleonClick.CLICK_SWITCHCARD;
                switch (pos) {
                    case 0:
                        click = ChameleonClick.CLICK_SWITCHCARD;
                        break;
                    case 1:
                        click = ChameleonClick.CLICK_RANDOM_UID;
                        break;
                    case 2:
                        click = ChameleonClick.CLICK_UID_LEFT_INCREMENT;
                        break;
                    case 3:
                        click = ChameleonClick.CLICK_UID_RIGHT_INCREMENT;
                        break;
                    case 4:
                        click = ChameleonClick.CLICK_UID_LEFT_DECREMENT;
                        break;
                    case 5:
                        click = ChameleonClick.CLICK_UID_RIGHT_DECREMENT;
                        break;
                    case 6:
                        click = ChameleonClick.CLICK_CLOSED;
                        break;
                }
                util.setClick(click, msgCallbacl);
                dismissProgressDialog();
            }
        }).start();
    }

    private void updateAliases() {
        //先缓存当前选择的卡槽位置!!!
        int pos = spChameleon_Slot_Select.getSelectedItemPosition();
        String[] aliases;
        if (Properties.v_chameleon_aliases_status) {
            //如果使用别名，则加载储存中保存的别名!
            aliases = Properties.v_chameleon_aliases;
        } else {
            //否则加载xml中固化的别名!
            aliases = getResources().getStringArray(R.array.chameleon_slot_list);
        }
        //动态设置!
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, aliases);
        spChameleon_Slot_Select.setAdapter(spinnerAdapter);
        spChameleon_Slot_Select.setSelection(pos);
        //动态设置之后将会更新卡槽状态，但是我们可以设置不不更新实际设备状态!
        spinnerInitState.setNotInitialized(spChameleon_Slot_Select);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //更新别名!
        updateAliases();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void showToast(String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ToastUtil.show(context, msg, false);
            }
        });
    }

    private void showProgressDialog() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressDialog.show();
            }
        });
    }

    private void dismissProgressDialog() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressDialog.dismiss();
            }
        });
    }
}