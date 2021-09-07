package com.rfidresearchgroup.adapter;

import android.content.Context;

import androidx.annotation.NonNull;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import com.rfidresearchgroup.javabean.DevBean;
import com.rfidresearchgroup.common.util.ViewUtil;
import com.rfidresearchgroup.rfidtools.R;

public class DevArrayAdapter extends AbsResIdArrayAdapter<DevBean> {

    public DevArrayAdapter(Context context, int resource, List<DevBean> objects) {
        super(context, resource, objects);

        resourceId = resource;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = ViewUtil.inflate(getContext(), resourceId);
        }
        //取出数据!
        DevBean bean = getItem(position);
        if (bean != null) {
            //得到视图实例!
            TextView name = convertView.findViewById(R.id.txtDevName);
            TextView address = convertView.findViewById(R.id.txtDevAddress);
            //设置进视图中!
            name.setText(bean.getDevName());
            address.setText(bean.getMacAddress());
        }
        return convertView;
    }
}
