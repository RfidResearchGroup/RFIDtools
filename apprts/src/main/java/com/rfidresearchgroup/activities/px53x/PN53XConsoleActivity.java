package com.rfidresearchgroup.activities.px53x;

import android.view.View;

import com.rfidresearchgroup.activities.tools.BaseConsoleActivity;


import java.io.File;

import com.rfidresearchgroup.common.util.FileUtils;
import com.rfidresearchgroup.util.Paths;

public class PN53XConsoleActivity extends BaseConsoleActivity {
    @Override
    protected View getCommandGUI() {
        return null;
    }

    @Override
    protected boolean prepareConsole() {
        return false;
    }

    public String getNativePath() {
        String ss = getApplicationInfo().nativeLibraryDir;
        if (ss == null)
            ss = getFilesDir().getPath() + "/lib";
        return ss;
    }

    protected String getDefaultExe() {
        return getNativePath() +
                File.separator +
                "libmfoc.so";
    }

    protected String[] getDefaultArg() {
        return new String[]{
                "-O test.mfd"
        };
    }

    @Override
    protected String getDefaultCWD() {
        return Paths.EXTERNAL_STORAGE_DIRECTORY;
    }

    @Override
    protected void onProcessStart() {

    }

    @Override
    protected void onProcessExit() {

    }

    @Override
    protected void onPrint(String chars) {

    }

    @Override
    protected void onPrintln(String chars) {

    }
}
