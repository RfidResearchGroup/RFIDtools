package com.rfidresearchgroup.binder;


import androidx.annotation.NonNull;


import com.rfidresearchgroup.holder.ItemCommonViewHolder;
import com.rfidresearchgroup.javabean.ItemCommonBean;
import me.drakeet.multitype.ItemViewBinder;

public abstract class ItemCommonBinder<T extends ItemCommonBean, VH extends ItemCommonViewHolder>
        extends ItemViewBinder<T, VH> {

    @Override
    protected void onBindViewHolder(@NonNull VH holder, @NonNull T item) {
        holder.imgIcon.setImageResource(item.getIconResID());
        holder.txtTitle.setText(item.getTitle());
        holder.txtSubTitle.setText(item.getSubTitle());
    }
}
