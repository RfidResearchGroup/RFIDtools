package cn.rrg.rdv.fragment.tools;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;

import java.io.File;
import java.io.IOException;

import cn.dxl.common.util.AppUtil;
import cn.dxl.common.util.DiskKVUtil;
import cn.dxl.common.util.RestartUtils;
import cn.rrg.rdv.R;
import cn.rrg.rdv.activities.main.BaseActivity;
import cn.rrg.rdv.fragment.base.BaseFragment;
import cn.rrg.rdv.util.Paths;
import cn.rrg.rdv.application.Properties;

/*
 * 主设置活动!
 * */
public class MainSettingsFragment
        extends BaseFragment {

    RadioGroup radioGroupLanguages;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.act_app_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        initViews(view);
        initActions(view.getContext());
    }

    private void initViews(View v) {
        radioGroupLanguages = v.findViewById(R.id.rdoGroup);
        switch (Properties.v_app_language) {
            //选中了中文!
            case "zh":
                radioGroupLanguages.check(R.id.rdoBtnLanguageChineseSimple);
                break;
            //选中了英文!
            case "en":
                radioGroupLanguages.check(R.id.rdoBtnLanguageEnglish);
                break;
            //类型错误或者未选时是跟随系统!
            case "auto":
            default:
                radioGroupLanguages.check(R.id.rdoBtnLanguageAuto);
                break;
        }
    }

    private void initActions(Context context) {
        radioGroupLanguages.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.rdoBtnLanguageAuto:
                        //切换为跟随系统!
                        Properties.v_app_language = "auto";
                        updateLanguage("auto");
                        break;
                    case R.id.rdoBtnLanguageChineseSimple:
                        //切换为中文简体!
                        Properties.v_app_language = "zh";
                        updateLanguage("zh");
                        break;
                    case R.id.rdoBtnLanguageEnglish:
                        //切换为英文!
                        Properties.v_app_language = "en";
                        updateLanguage("en");
                        break;
                }
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
        });
    }

    private void updateLanguage(String language) {
        try {
            DiskKVUtil.update2Disk(Properties.k_app_language, language, new File(Paths.SETTINGS_FILE));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
