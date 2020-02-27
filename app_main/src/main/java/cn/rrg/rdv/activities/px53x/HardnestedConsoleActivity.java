package cn.rrg.rdv.activities.px53x;

import android.view.View;

import cn.rrg.natives.HardnestedTools;
import cn.rrg.console.define.ICommandTools;
import cn.rrg.console.define.ICommandType;

public class HardnestedConsoleActivity extends PN53XConsoleActivity {

    @Override
    protected View getCommandGUI() {
        return null;
    }

    @Override
    protected ICommandTools initCMD() {
        return new HardnestedTools();
    }

    @Override
    protected ICommandType initType() {
        return new ICommandType() {
            @Override
            public boolean isKey(String output) {
                return false;
            }

            @Override
            public String parseKey(String output) {
                return null;
            }

            @Override
            public boolean isData(String output) {
                return false;
            }

            @Override
            public String parseData(String output) {
                return null;
            }

            @Override
            public boolean isText(String output) {
                return false;
            }

            @Override
            public Runnable parseText(String output) {
                return null;
            }
        };
    }

    @Override
    protected int startTest(ICommandTools cmd) {
        mDefaultCMD = "hard FFFFFFFFFFFF 0 A 4 A";
        return super.startTest(cmd);
    }

    @Override
    protected void onTestEnd() {
        showToast("Hardnested结束执行...");
    }
}
