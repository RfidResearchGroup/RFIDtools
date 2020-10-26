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
import cn.rrg.rdv.activities.tools.DumpListActivity;
import cn.rrg.rdv.activities.tools.FileListActivity;
import cn.rrg.rdv.activities.tools.KeyFileListActivity;
import cn.rrg.rdv.activities.tools.Proxmark3FirmwareActivity;
import cn.rrg.rdv.fragment.base.BaseFragment;

public class ToolsAccessFragment
        extends BaseFragment {

    private Button btnDataEditor = null;
    private Button btnKeyEditor = null;
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
        btnPM3Flasher = view.findViewById(R.id.btnPM3Flasher);
    }

    private void initActions(Context context) {
        btnDataEditor.setOnClickListener(v -> {
            startActivity(new Intent(context, DumpListActivity.class).putExtra("mode", DumpListActivity.MODE.EDIT));
        });

        btnKeyEditor.setOnClickListener(v -> startActivity(
                new Intent(context, KeyFileListActivity.class).putExtra("mode", FileListActivity.MODE.EDIT))
        );

        btnPM3Flasher.setOnClickListener(v -> startActivity(new Intent(context, Proxmark3FirmwareActivity.class)));
    }
}
