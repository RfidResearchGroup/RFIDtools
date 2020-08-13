package cn.rrg.rdv.activities.proxmark3.rdv4_rrg;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.Nullable;

import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cn.rrg.freo.IORedirector;
import cn.rrg.rdv.R;
import cn.rrg.rdv.adapter.EasyBtnAdapter;
import cn.rrg.rdv.javabean.EasyCMDEntry;
import cn.rrg.rdv.util.EasyBtnUtil;
import cn.dxl.common.widget.DoubleInputDialog;
import cn.dxl.common.util.ArrayUtils;
import cn.dxl.common.widget.ToastUtil;
import cn.dxl.common.util.ViewUtil;
import cn.rrg.rdv.util.Paths;

public class Proxmark3Rdv4RRGRedTeamConsoleActivity
        extends Proxmark3Rdv4RRGConsoleActivity {

    private GridView gridViewShowEasyButton;
    private EasyBtnAdapter adapter;
    private List<EasyCMDEntry> easyCMDEntries = new ArrayList<>();

    //构造一个简单的对话框，用于提示需要的操作!
    private AlertDialog operaDialog;
    //显示添加按钮!
    private TextView txtAddEasyBtn;
    //缓存当前要操作的项目!
    private int operaPosition;
    //配置文件操作工具
    private EasyBtnUtil easyBtnUtil = new EasyBtnUtil();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        btnGui.setText(getString(R.string.console_dynamic_btn));
        //隐藏不需要的区域!
        skBarConsoleViewScale.setProgress(10);
        //设置pm3的输入提示
        edtInputCmd.setHint(getString(R.string.pm3_hint));

        //不能让用户关闭通讯!
        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopPM3Client4KeyBorad();
            }
        });

        initViews();
        initActions();

        // 更改工作目录!
        IORedirector.chdir(Paths.PM3_CWD);
    }

    private void initViews() {
        //先初始化视图!
        guiView = ViewUtil.inflate(this, R.layout.easy_button_container);

        gridViewShowEasyButton = findViewById(R.id.gridViewShowEasyButton);
        adapter = new EasyBtnAdapter(this, R.layout.item_easy__btn, easyCMDEntries);
        gridViewShowEasyButton.setAdapter(adapter);

        //添加数据!
        EasyCMDEntry[] buttons = getCMDs();
        if (buttons != null) {
            easyCMDEntries.addAll(Arrays.asList(getCMDs()));
            adapter.notifyDataSetChanged();
        }
        txtAddEasyBtn = findViewById(R.id.txtAddEasyBtn);
    }

    private void initActions() {
        //列表项单击时执行相应的命令!
        gridViewShowEasyButton.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //点击了对应项时，取出对应的实体，进行命令发送!
                EasyCMDEntry entry = easyCMDEntries.get(position);
                if (entry != null) {
                    String cmd = entry.getCommand();
                    if (cmd != null) {
                        //先提交命令到输入框
                        putCMDAndClosePop(cmd);
                    }
                }
            }
        });
        //列表项长按时显示相关的操作项!
        gridViewShowEasyButton.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                operaDialog.show();
                operaPosition = position;
                return true;
            }
        });
        //构建一个用于显示操作的对话框!
        operaDialog = new AlertDialog.Builder(this).setItems(new String[]{getString(R.string.delete), getString(R.string.modify)}, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final EasyCMDEntry entry = easyCMDEntries.get(operaPosition);
                if (which == 0) {
                    new AlertDialog.Builder(context).setTitle(R.string.warning).setMessage(getString(R.string.enter_delete)
                            + "\n\nItem name: " + entry.getCmdName())
                            .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    //做出删除操作！
                                    easyCMDEntries.remove(operaPosition);
                                    adapter.notifyDataSetChanged();
                                    //TODO 本地配置删除!
                                    if (easyBtnUtil.deleteButton(0, operaPosition)) {
                                        ToastUtil.show(context, getString(R.string.success), false);
                                    } else {
                                        ToastUtil.show(context, getString(R.string.failed), false);
                                    }
                                }
                            }).setNegativeButton(getString(R.string.no), null).show();
                } else {
                    //做出修改操作，先取出原来的数据!
                    DoubleInputDialog inputDialog = new DoubleInputDialog(context);
                    inputDialog.setTitle(R.string.button_edit);
                    inputDialog.getHint1().setText(R.string.title_input);
                    inputDialog.getHint2().setText(R.string.command_input);
                    //设置到视图上!
                    inputDialog.getEdtInput1().setText(entry.getCmdName());
                    inputDialog.getEdtInput2().setText(entry.getCommand());
                    //然后设置操作回调并且显示!
                    inputDialog.setSaveListener(new DoubleInputDialog.OnSaveListener() {
                        @Override
                        public void onSave(String[] content) {
                            //操作成功，进行内存更新!
                            entry.setCmdName(content[0]);
                            entry.setCommand(content[1]);
                            adapter.notifyDataSetChanged();
                            //TODO 然后进行本地更新!
                            if (easyBtnUtil.updateButton(0, operaPosition, entry.getCmdName(), entry.getCommand())) {
                                //现在的话已经更新成功
                                ToastUtil.show(context, getString(R.string.success), false);
                            } else {
                                ToastUtil.show(context, getString(R.string.failed), false);
                            }
                        }
                    }).show();
                }
            }
        }).create();
        //设置添加按钮的动作!
        txtAddEasyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //做出增加操作，构建一个实体用于保存!
                EasyCMDEntry entry = new EasyCMDEntry();
                DoubleInputDialog inputDialog = new DoubleInputDialog(context);
                inputDialog.setTitle(R.string.btn_add);
                inputDialog.getHint1().setText(R.string.title_input);
                inputDialog.getHint2().setText(R.string.command_input);
                //然后设置操作回调并且显示!
                inputDialog.setSaveListener(new DoubleInputDialog.OnSaveListener() {
                    @Override
                    public void onSave(String[] content) {
                        //操作成功，进行内存更新!
                        entry.setCmdName(content[0]);
                        entry.setCommand(content[1]);
                        easyCMDEntries.add(entry);
                        adapter.notifyDataSetChanged();
                        //TODO 然后进行本地更新!
                        if (easyBtnUtil.inertButton(0, entry.getCmdName(), entry.getCommand())) {
                            //现在的话已经更新成功
                            ToastUtil.show(context, getString(R.string.success), false);
                        } else {
                            ToastUtil.show(context, getString(R.string.failed), false);
                        }
                    }
                }).show();
            }
        });
    }

    private EasyCMDEntry[] getCMDs() {
        //默认先拿0组的按钮，以后做多组分页的时候再做其他组的处理!
        List<EasyCMDEntry> btns = easyBtnUtil.getButtons(0);
        if (btns == null) return null;
        return ArrayUtils.list2Arr(btns);
    }
}
