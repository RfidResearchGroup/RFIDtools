package com.rfidresearchgroup.adapter;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import com.rfidresearchgroup.javabean.EasyCMDEntry;
import com.rfidresearchgroup.rfidtools.R;

public class EasyBtnAdapter
        extends AbsResIdArrayAdapter<EasyCMDEntry> {

    public EasyBtnAdapter(@NonNull Context context, int resource, @NonNull List<EasyCMDEntry> objects) {
        super(context, resource, objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_easy__btn, null);
        }
        EasyCMDEntry entry = getItem(position);
        if (entry != null) {
            TextView txtShowName = convertView.findViewById(R.id.txtShowCMDName);
            TextView txtShowCMD = convertView.findViewById(R.id.txtShowCMD);
            txtShowName.setText(entry.getCmdName());
            txtShowCMD.setText(entry.getCommand());
        }
        return convertView;
    }
}
