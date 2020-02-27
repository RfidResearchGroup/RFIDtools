package cn.rrg.rdv.activities.main;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;

import android.view.View;
import android.widget.Button;

import cn.rrg.rdv.R;
import cn.rrg.rdv.activities.proxmark3.rdv4_rrg.Proxmark3Rdv4RRGRedTeamConsoleActivity;

/*
 * PM3 RDV4 功能界面!
 * */
public class Proxmark3Rdv4RRGMain
        extends BaseActivity {

    private Button btnGo2Proxmark3Rdv4RRGRedTeam;
    private Button btnGo2Proxmark3Rdv4RRGReader;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_main_proxmark3_rdv4_rrg);

        initViews();
        initActions();
    }

    private void initViews() {
        btnGo2Proxmark3Rdv4RRGRedTeam = findViewById(R.id.btnGo2Proxmark3Rdv4RRGRedTeam);
        btnGo2Proxmark3Rdv4RRGReader = findViewById(R.id.btnGo2Proxmark3Rdv4RRGReader);
    }

    private void initActions() {
        btnGo2Proxmark3Rdv4RRGRedTeam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(context, Proxmark3Rdv4RRGRedTeamConsoleActivity.class));
            }
        });
        btnGo2Proxmark3Rdv4RRGReader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(context, Proxmark3Rdv4RRGReaderMain.class));
            }
        });
    }
}
