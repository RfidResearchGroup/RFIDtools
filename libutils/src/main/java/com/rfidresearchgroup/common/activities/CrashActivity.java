package com.rfidresearchgroup.common.activities;

import android.app.Activity;
import android.os.Bundle;
import android.os.Process;
import androidx.annotation.Nullable;
import android.widget.TextView;
import android.widget.Toast;

import com.rfidresearchgroup.common.R;


/**
 * show throwable!
 */
public class CrashActivity extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_crash_main);

        TextView txtShowMsg = findViewById(R.id.txtView_showMsg);

        //接收意图中序列化过来的Throwable对象
        Throwable t = (Throwable) getIntent().getSerializableExtra("crash");
        if (t == null) {
            Toast.makeText(this, "接收到的异常对象为空!", Toast.LENGTH_SHORT).show();
        } else {
            StringBuilder msg = new StringBuilder();
            msg.append(t.getLocalizedMessage()).append('\n');
            for (StackTraceElement st : t.getStackTrace()) {
                msg.append('\n');
                msg.append("at: ");
                msg.append(st.toString());
            }
            txtShowMsg.setText(msg);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //终止虚拟机的执行
        finish();
        Process.killProcess(Process.myPid());
        System.exit(0);
    }
}
