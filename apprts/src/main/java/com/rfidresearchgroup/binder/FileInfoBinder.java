package com.rfidresearchgroup.binder;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.rfidresearchgroup.javabean.FileBean;
import com.rfidresearchgroup.rfidtools.R;

import me.drakeet.multitype.ItemViewBinder;

public class FileInfoBinder extends ItemViewBinder<FileBean, FileInfoBinder.FileHolder> {

    @NonNull
    @Override
    protected FileHolder onCreateViewHolder(@NonNull LayoutInflater inflater, @NonNull ViewGroup parent) {
        return new FileHolder(inflater.inflate(R.layout.item_file_info, parent, false));
    }

    @Override
    protected void onBindViewHolder(@NonNull FileHolder holder, @NonNull FileBean item) {
        holder.txtName.setText(item.getName());
        holder.txtInfo.setText(item.getInfo());
        if (item.isShowIcon()) {
            if (item.isFile()) {
                holder.imgIcon.setImageResource(R.drawable.ic_unknow_black_56dp);
            } else {
                holder.imgIcon.setImageResource(R.drawable.ic_folder_yellow_56dp);
            }
        } else {
            holder.imgIcon.setVisibility(View.GONE);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                item.onClick();
            }
        });
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return item.onLongClick();
            }
        });
    }

    static class FileHolder extends RecyclerView.ViewHolder {
        private @NonNull
        TextView txtName;
        private @NonNull
        TextView txtInfo;
        private @NonNull
        ImageView imgIcon;

        FileHolder(@NonNull View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.txtName);
            txtInfo = itemView.findViewById(R.id.txtInfo);
            imgIcon = itemView.findViewById(R.id.imgIcon);
        }
    }
}
