package cn.rrg.rdv.activities.tools;

import android.os.Bundle;
import androidx.annotation.Nullable;
import android.view.View;

import java.io.File;

import cn.rrg.freo.Freopen;
import cn.rrg.console.define.ICommandTools;
import cn.rrg.console.define.ICommandType;
import cn.rrg.rdv.implement.EntryICommandType;
import cn.rrg.rdv.util.Paths;

public abstract class CommonConsoleActivity extends BaseConsoleActivity {

    private File o = new File(Paths.COMMON_FORWARD_O);
    private File e = new File(Paths.COMMON_FORWARD_E);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Freopen.setStdEO(o.getAbsolutePath(), Freopen.STD_OUT);
        Freopen.setStdEO(e.getAbsolutePath(), Freopen.STD_ERR);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected View getCommandGUI() {
        return null;
    }

    @Override
    protected ICommandType initType() {
        return new EntryICommandType();
    }

    @Override
    protected File initOutTarget() {
        return o;
    }

    @Override
    protected File initErrTarget() {
        return e;
    }

    @Override
    protected int startTest(ICommandTools cmd) {
        if (isTesting()) {
            showToast("Has some task is excuting...");
            return -1;
        }
        if (edtInputCmd.getText().toString().length() > 0) {
            mDefaultCMD = edtInputCmd.getText().toString();
        }
        return cmd.startExecute(mDefaultCMD);
    }

    @Override
    protected void onNewOutLine(String line) {

    }

    @Override
    protected void onNewErrLine(String line) {

    }

    @Override
    protected void onTestEnd() {

    }
}
