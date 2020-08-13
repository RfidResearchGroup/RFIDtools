package cn.rrg.rdv.fragment.tools;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.termux.app.TermuxService;

import cn.dxl.common.util.AppUtil;
import cn.dxl.common.util.FileUtils;
import cn.dxl.common.util.LogUtils;
import cn.dxl.common.util.RestartUtils;
import cn.rrg.rdv.R;
import cn.rrg.rdv.binder.ItemSingleTextBean;
import cn.rrg.rdv.binder.ItemTextBinder;
import cn.rrg.rdv.binder.ItemToggleBinder;
import cn.rrg.rdv.fragment.base.BaseFragment;
import cn.rrg.rdv.javabean.ItemTextBean;
import cn.rrg.rdv.javabean.ItemToggleBean;
import cn.rrg.rdv.javabean.TitleBean;
import cn.rrg.rdv.util.Commons;
import cn.rrg.rdv.util.Paths;
import cn.rrg.rdv.util.Proxmark3Installer;
import me.drakeet.multitype.Items;
import me.drakeet.multitype.MultiTypeAdapter;

import static android.app.Activity.RESULT_OK;

/*
 * 主设置活动!
 * */
public class MainSettingsFragment
        extends BaseFragment {

    private RecyclerView rvSettingsList;
    private SharedPreferences preferences;
    private MultiTypeAdapter multiTypeAdapter;
    private Items items = new Items();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.act_app_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        preferences = Commons.getPrivatePreferences();
        multiTypeAdapter = new MultiTypeAdapter();

        initViews(view);
        initList();
    }

    private void initViews(View v) {
        rvSettingsList = v.findViewById(R.id.rvSettingsList);
        rvSettingsList.setLayoutManager(new LinearLayoutManager(v.getContext()));
        rvSettingsList.setAdapter(multiTypeAdapter);

        multiTypeAdapter.register(ItemTextBean.class, new ItemTextBinder());
        multiTypeAdapter.register(ItemToggleBean.class, new ItemToggleBinder());
        multiTypeAdapter.register(TitleBean.class, new ItemSingleTextBean());

        multiTypeAdapter.setItems(items);
    }

    private void initList() {
        items.add(new TitleBean(getString(R.string.language)));

        ItemTextBean languageItem = new ItemTextBean(getString(R.string.title_app_language)) {
            @Override
            public void onClick(View view, int pos) {
                String[] languages = new String[]{"English", "中文", getString(R.string.following_system)};
                new AlertDialog.Builder(view.getContext())
                        .setTitle(R.string.tips_language_change)
                        .setItems(languages, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case 0:
                                        Commons.setLanguage("en");
                                        showRestartDialog();
                                        break;
                                    case 1:
                                        Commons.setLanguage("zh");
                                        showRestartDialog();
                                        break;
                                    case 2:
                                        Commons.setLanguage("auto");
                                        showRestartDialog();
                                        break;
                                }
                            }
                        }).show();
            }
        };
        languageItem.setSubTitle(getString(R.string.title_set_language));
        languageItem.setMessage(languageTran());
        items.add(languageItem);

        items.add(new TitleBean(getString(R.string.tips_pm3_settings)));

        ItemTextBean pm3Res = new ItemTextBean(getString(R.string.title_pm3_res_init)) {
            @Override
            public void onClick(View view, int pos) {
                pm3ResInit(this);
            }
        };
        pm3Res.setSubTitle(getString(R.string.title_pm3_res_sub_title));
        pm3Res.setMessage(Commons.isPM3ResInitialled() ? getString(R.string.initialized) : getString(R.string.uninitialized));
        items.add(pm3Res);

        // 只有SDK版本大于等于24的时候才使能PM3的高级视图
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {

            ItemToggleBean pm3AutoGo = new ItemToggleBean(getString(R.string.title_pm3_autogo_setting)) {
                @Override
                public void onChange(View view, int pos, boolean checked) {
                    Commons.setAutoGoToTerminal(checked);
                }
            };
            pm3AutoGo.setSubTitle(getString(R.string.title_sub_pm3_autogo_setting));
            pm3AutoGo.setChecked(Commons.getAutoGoToTerminal());
            items.add(pm3AutoGo);

            ItemTextBean pm3TerminalTypeItem = new ItemTextBean(getString(R.string.title_terminal_type_setting)) {
                @Override
                public void onClick(View view, int pos) {
                    String[] languages = new String[]{getString(R.string.item_full_terminal_view), getString(R.string.item_simple_terminal_view)};
                    new AlertDialog.Builder(view.getContext())
                            .setTitle(R.string.tips_terminal_select_like)
                            .setItems(languages, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Commons.setTerminalType(which);
                                    setMessage(currentTerminalType());
                                    multiTypeAdapter.notifyDataSetChanged();
                                }
                            }).show();
                }
            };
            pm3TerminalTypeItem.setSubTitle(getString(R.string.title_sub_terminal_type_setting));
            pm3TerminalTypeItem.setMessage(currentTerminalType());
            items.add(pm3TerminalTypeItem);
        }

        // PM3 work directory select
        ItemToggleBean pm3HomePathItem = new ItemToggleBean(getString(R.string.title_pm3_home)) {
            @Override
            public void onChange(View view, int pos, boolean checked) {
                Commons.setPM3ExternalWorkDirectoryEnable(checked);
                setSubTitle(Commons.updatePM3Cwd());
                setChecked(checked);
                multiTypeAdapter.notifyDataSetChanged();
            }
        };
        pm3HomePathItem.setSubTitle(Paths.PM3_CWD);
        pm3HomePathItem.setChecked(Commons.isPM3ExternalWorkDirectoryEnable());
        items.add(pm3HomePathItem);

        items.add(new TitleBean(getString(R.string.other)));

        ItemTextBean openSourceItem = new ItemTextBean(getString(R.string.title_open_source)) {
            @Override
            public void onClick(View view, int pos) {
                Uri uri = Uri.parse("https://github.com/RfidResearchGroup/RFIDtools");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        };
        openSourceItem.setMessage(getString(R.string.go2));
        openSourceItem.setSubTitle(getString(R.string.tips_opensource_welcome));
        items.add(openSourceItem);

        multiTypeAdapter.notifyDataSetChanged();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0x77 && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri != null) {
                // get file path
                String path = FileUtils.getFilePathByUri(uri);
                // save
                LogUtils.d("获取到的路径: " + path);
            }
        }
    }

    public static String getVersion(Context context) {
        if (context == null) return "unknown";
        try {
            PackageInfo pi = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return pi.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return context.getString(R.string.unknown);
        }
    }


    private String languageTran() {
        String currentLanguage = Commons.getLanguage();
        switch (currentLanguage) {
            case "en":
                return "English";
            case "zh":
                return "中文";
            case "auto":
            default:
                return getString(R.string.following_system);
        }
    }

    private void showRestartDialog() {
        Context context = getContext();
        if (context != null) {
            new AlertDialog.Builder(context)
                    .setTitle(R.string.tips)
                    .setMessage(R.string.msg_language_change_success)
                    .setPositiveButton(getString(R.string.restart), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            RestartUtils.restartAPP(context, 0);
                            //结束所有的act!
                            AppUtil.getInstance().finishAll();
                        }
                    }).show();
        }
    }

    private void pm3ResInit(ItemTextBean item) {
        Activity activity = getActivity();
        if (activity != null) {
            Proxmark3Installer.installIfNeed(activity, new Runnable() {
                @Override
                public void run() {
                    showToast(getString(R.string.finished));
                    item.setMessage(getString(R.string.initialized));
                    multiTypeAdapter.notifyDataSetChanged();
                }
            });
        }
    }

    private String currentTerminalType() {
        switch (Commons.getTerminalType()) {
            case -1:
            default:
                return getString(R.string.unselected);
            case 0:
                return getString(R.string.item_full_terminal_view);
            case 1:
                return getString(R.string.item_simple_terminal_view);
        }
    }
}
