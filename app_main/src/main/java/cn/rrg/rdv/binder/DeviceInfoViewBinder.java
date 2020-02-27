package cn.rrg.rdv.binder;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import cn.rrg.rdv.R;
import cn.rrg.rdv.javabean.DeviceInfoBean;
import me.drakeet.multitype.ItemViewBinder;

public class DeviceInfoViewBinder extends ItemViewBinder<DeviceInfoBean, DeviceInfoViewBinder.DeviceInfoHolder> {

    static class DeviceInfoHolder extends RecyclerView.ViewHolder {
        TextView txtName;
        ImageView icon;

        DeviceInfoHolder(@NonNull View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.txtName);
            icon = itemView.findViewById(R.id.icon);
        }
    }

    @NonNull
    @Override
    protected DeviceInfoHolder onCreateViewHolder(@NonNull LayoutInflater inflater, @NonNull ViewGroup parent) {
        return new DeviceInfoHolder(inflater.inflate(R.layout.item_device_info, parent, false));
    }

    @Override
    protected void onBindViewHolder(@NonNull DeviceInfoHolder holder, @NonNull DeviceInfoBean item) {
        holder.icon.setImageResource(item.getIcon());
        holder.txtName.setText(item.getName());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                item.onClick();
            }
        });
    }
}
