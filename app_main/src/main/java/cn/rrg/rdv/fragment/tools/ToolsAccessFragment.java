package cn.rrg.rdv.fragment.tools;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import cn.rrg.rdv.R;
import cn.rrg.rdv.activities.tools.DumpEditActivity;
import cn.rrg.rdv.activities.tools.DumpEqualActivity;
import cn.rrg.rdv.activities.tools.DumpListActivity;
import cn.rrg.rdv.activities.tools.FileListActivity;
import cn.rrg.rdv.activities.tools.FormatCovertActivity;
import cn.rrg.rdv.activities.tools.KeyFileEditActivity;
import cn.rrg.rdv.activities.tools.KeyFileListActivity;
import cn.rrg.rdv.activities.tools.MfKey32ConsoleActivity;
import cn.rrg.rdv.activities.tools.MfKey64ConsoleActivity;
import cn.rrg.rdv.fragment.base.BaseFragment;

public class ToolsAccessFragment
        extends BaseFragment {

    private Button btnDataEditor = null;
    private Button btnKeyEditor = null;
    private Button btnUnionKeyEditor = null;
    private Button btnFormatCovert = null;
    private Button btnDiffTools = null;
    private Button btnMfKey32 = null;
    private Button btnMfKey64 = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.act_common_tools, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        initActions(view.getContext());
    }

    private void initViews(View view) {
        btnDataEditor = view.findViewById(R.id.btnDumpEditor);
        btnKeyEditor = view.findViewById(R.id.btnKeyEditor);
        btnUnionKeyEditor = view.findViewById(R.id.btnUnionKeyEditor);
        btnFormatCovert = view.findViewById(R.id.btnConvertFormat);
        btnDiffTools = view.findViewById(R.id.btnDiffTool);
        btnMfKey32 = view.findViewById(R.id.btnMfKey32);
        btnMfKey64 = view.findViewById(R.id.btnMfKey64);
    }

    private void initActions(Context context) {
        //mfkey32工具的跳转!
        btnMfKey32.setOnClickListener(v -> startActivity(new Intent(context, MfKey32ConsoleActivity.class)));

        //mfkey64工具的跳转!
        btnMfKey64.setOnClickListener(v -> startActivity(new Intent(context, MfKey64ConsoleActivity.class)));

        //设置数据编辑的按钮的接口实现
        btnDataEditor.setOnClickListener(v -> {
            startActivity(new Intent(context, DumpListActivity.class).putExtra("mode", DumpListActivity.MODE.EDIT));
        });

        //数据对比
        btnDiffTools.setOnClickListener(v -> startActivity(new Intent(context, DumpEqualActivity.class)));

        //格式转换act
        btnFormatCovert.setOnClickListener(v -> startActivity(new Intent(context, FormatCovertActivity.class)));

        //设置密钥编辑的按钮的接口实现
        btnKeyEditor.setOnClickListener(v -> startActivity(
                new Intent(context, KeyFileListActivity.class).putExtra("mode", FileListActivity.MODE.EDIT))
        );
    }
}
