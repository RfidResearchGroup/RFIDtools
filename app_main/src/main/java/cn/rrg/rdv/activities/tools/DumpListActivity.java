package cn.rrg.rdv.activities.tools;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;

import java.io.File;
import java.io.FileFilter;

import cn.rrg.rdv.javabean.FileBean;
import cn.rrg.rdv.util.DumpUtils;
import cn.rrg.rdv.util.Paths;

public class DumpListActivity extends FileListActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // don't need add!
        btnAdd.setVisibility(View.GONE);
    }

    private boolean isDumpFileName(String name) {
        String[] suffixs = new String[]{
                ".bin",
                ".hex",
                ".eml",
                ".mfd",
                ".mtd",
                ".txt",
                ".json",
                ".dump"
        };
        for (String suffix : suffixs) {
            if (name.endsWith(suffix)) {
                return true;
            }
        }
        return false;
    }

    private boolean isHardnestedDir(File name) {
        return name.isDirectory() && Paths.HARDNESTED_PATH.equalsIgnoreCase(name.getName());
    }

    @Override
    protected FileFilter onInitFilter() {
        return new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                // 1. The name is match!
                // 2. not a hardnested res dir.
                String name = pathname.getName();
                if (isHardnestedDir(pathname)) {
                    return false;
                }
                if (pathname.isDirectory()) {
                    return true;
                }
                return isDumpFileName(name) && DumpUtils.isDump(pathname);
            }
        };
    }

    @Override
    protected String onInitPath() {
        return Paths.TOOLS_DIRECTORY;
    }

    @Override
    protected void onEdit(FileBean fileBean) {
        Intent intent = new Intent(context, DumpEditActivity.class).putExtra("file", fileBean.getPath());
        startActivity(intent);
    }

    @Override
    protected void onAdd() {

    }
}
