package com.rfidresearchgroup.activities.tools;

import android.content.DialogInterface;
import android.content.Intent;

import com.rfidresearchgroup.javabean.FileBean;
import com.rfidresearchgroup.rfidtools.R;
import com.rfidresearchgroup.util.Commons;
import com.rfidresearchgroup.util.Paths;

import java.io.File;
import java.io.FileFilter;

import com.rfidresearchgroup.common.util.FileUtils;
import com.rfidresearchgroup.common.widget.SingleInputDialog;
import com.rfidresearchgroup.common.widget.ToastUtil;

public class KeyFileListActivity extends FileListActivity {
    @Override
    protected FileFilter onInitFilter() {
        return new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return true;
            }
        };
    }

    @Override
    protected String onInitPath() {
        return Paths.KEY_DIRECTORY;
    }

    @Override
    protected void onEdit(FileBean fileBean) {
        Intent intent = new Intent(context, KeyFileEditActivity.class)
                .putExtra("file", fileBean.getPath());
        startActivity(intent);
    }

    @Override
    protected void onAdd() {
        SingleInputDialog dialog = new SingleInputDialog(context).setTips(R.string.title_plz_input_name);
        dialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface d, int which) {
                String name = ((SingleInputDialog) d).getInput().toString();
                if (FileUtils.isValidFileName(name)) {
                    Commons.createInternalKey(name + ".txt");
                    initDatas();
                } else {
                    ToastUtil.show(context, getString(R.string.input_err), false);
                }
            }
        });
        dialog.show();
    }
}
