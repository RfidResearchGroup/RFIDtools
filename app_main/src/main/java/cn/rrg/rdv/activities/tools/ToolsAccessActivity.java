package cn.rrg.rdv.activities.tools;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.widget.Button;

import cn.rrg.rdv.R;
import cn.rrg.rdv.activities.main.BaseActivity;

public class ToolsAccessActivity
        extends BaseActivity {

    private Button btnDataEditor = null;
    private Button btnKeyEditor = null;
    private Button btnUnionKeyEditor = null;
    private Button btnFormatCovert = null;
    private Button btnDiffTools = null;
    private Button btnMfKey32 = null;
    private Button btnMfKey64 = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_common_tools);

        initViews();
        initActions();
    }

    private void initViews() {
        btnDataEditor = findViewById(R.id.btnDumpEditor);
        btnKeyEditor = findViewById(R.id.btnKeyEditor);
        btnUnionKeyEditor = findViewById(R.id.btnUnionKeyEditor);
        btnFormatCovert = findViewById(R.id.btnConvertFormat);
        btnDiffTools = findViewById(R.id.btnDiffTool);
        btnMfKey32 = findViewById(R.id.btnMfKey32);
        btnMfKey64 = findViewById(R.id.btnMfKey64);
    }

    private void initActions() {
        //mfkey32工具的跳转!
        btnMfKey32.setOnClickListener(v -> startActivity(new Intent(context, MfKey32ConsoleActivity.class)));

        //mfkey64工具的跳转!
        btnMfKey64.setOnClickListener(v -> startActivity(new Intent(context, MfKey64ConsoleActivity.class)));

        //设置数据编辑的按钮的接口实现
        btnDataEditor.setOnClickListener(v -> {
            startActivity(new Intent(context, DumpActivity.class));
        });

        //数据对比
        btnDiffTools.setOnClickListener(v -> startActivity(new Intent(context, DumpEqualActivity.class)));

        //格式转换act
        btnFormatCovert.setOnClickListener(v -> startActivity(new Intent(context, FormatCovertActivity.class)));

        //联动密钥编辑的窗口实现!
        btnUnionKeyEditor.setOnClickListener(v -> startActivity(new Intent(context, UnionActionActivity.class)));

        //设置密钥编辑的按钮的接口实现
        btnKeyEditor.setOnClickListener(v -> startActivity(new Intent(context, KeyFileEditActivity.class)));
    }
}
