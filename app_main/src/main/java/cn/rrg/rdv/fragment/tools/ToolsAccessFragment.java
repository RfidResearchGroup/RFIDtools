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

import com.proxgrind.pm3flasher.Proxmark3Flasher;

import cn.rrg.rdv.R;
import cn.rrg.rdv.activities.tools.DumpEqualActivity;
import cn.rrg.rdv.activities.tools.DumpListActivity;
import cn.rrg.rdv.activities.tools.FileListActivity;
import cn.rrg.rdv.activities.tools.FormatCovertActivity;
import cn.rrg.rdv.activities.tools.KeyFileListActivity;
import cn.rrg.rdv.activities.tools.Proxmark3FirmwareActivity;
import cn.rrg.rdv.fragment.base.BaseFragment;

public class ToolsAccessFragment
        extends BaseFragment {

    private Button btnDataEditor = null;
    private Button btnKeyEditor = null;
    private Button btnFormatCovert = null;
    private Button btnDiffTools = null;
    private Button btnPM3Flasher;

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
        btnFormatCovert = view.findViewById(R.id.btnConvertFormat);
        btnDiffTools = view.findViewById(R.id.btnDiffTool);
        btnPM3Flasher = view.findViewById(R.id.btnPM3Flasher);
    }

    private void initActions(Context context) {
        btnDataEditor.setOnClickListener(v -> {
            startActivity(new Intent(context, DumpListActivity.class).putExtra("mode", DumpListActivity.MODE.EDIT));
        });

        btnDiffTools.setOnClickListener(v -> startActivity(new Intent(context, DumpEqualActivity.class)));

        btnFormatCovert.setOnClickListener(v -> startActivity(new Intent(context, FormatCovertActivity.class)));

        btnKeyEditor.setOnClickListener(v -> startActivity(
                new Intent(context, KeyFileListActivity.class).putExtra("mode", FileListActivity.MODE.EDIT))
        );

        btnPM3Flasher.setOnClickListener(v -> startActivity(new Intent(context, Proxmark3FirmwareActivity.class)));
    }
}
