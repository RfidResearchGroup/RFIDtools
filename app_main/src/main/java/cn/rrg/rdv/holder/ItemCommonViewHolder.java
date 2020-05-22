package cn.rrg.rdv.holder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import cn.rrg.rdv.R;

public class ItemCommonViewHolder extends RecyclerView.ViewHolder {
    public TextView txtTitle;
    public TextView txtSubTitle;
    public ImageView imgIcon;

    public ItemCommonViewHolder(@NonNull View itemView) {
        super(itemView);
        txtTitle = itemView.findViewById(R.id.txtMainTitle);
        txtSubTitle = itemView.findViewById(R.id.txtSubTitle);
        imgIcon = itemView.findViewById(R.id.imgIcon);
    }
}
