package com.rfidresearchgroup.activities.proxmark3.rdv4_rrg;

import android.view.View;
import android.widget.CompoundButton;

import com.rfidresearchgroup.activities.tools.BaseConsoleActivity;

public abstract class Proxmark3ConsoleActivity
        extends BaseConsoleActivity
        implements View.OnClickListener,
        CompoundButton.OnCheckedChangeListener {

    protected View guiView = null;

    @Override
    protected void onDestroy() {
        stop();
        super.onDestroy();
    }

    @Override
    public <T extends View> T findViewById(int id) {
        T view = super.findViewById(id);
        return view != null ? view : guiView != null ? guiView.findViewById(id) : null;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        //don't need process!!!
    }

    @Override
    protected View getCommandGUI() {
        return guiView;
    }
}