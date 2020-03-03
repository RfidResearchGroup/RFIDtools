package cn.rrg.rdv.activities.tools;

import android.content.DialogInterface;
import android.content.Intent;

import androidx.appcompat.app.AlertDialog;

import java.io.File;
import java.io.FileFilter;

import cn.dxl.common.util.FileUtils;
import cn.dxl.common.widget.SingleInputDialog;
import cn.dxl.common.widget.ToastUtil;
import cn.rrg.rdv.R;
import cn.rrg.rdv.javabean.FileBean;
import cn.rrg.rdv.util.Commons;
import cn.rrg.rdv.util.Paths;

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
