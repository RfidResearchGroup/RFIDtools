package cn.rrg.rdv.binder;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;

import cn.rrg.rdv.R;
import cn.rrg.rdv.holder.ItemCommonViewHolder;
import cn.rrg.rdv.javabean.ItemToggleBean;

public class ItemToggleBinder extends ItemCommonBinder<ItemToggleBean, ItemToggleBinder.ViewHolder> {

    @NonNull
    @Override
    protected ViewHolder onCreateViewHolder(@NonNull LayoutInflater inflater, @NonNull ViewGroup parent) {
        return new ViewHolder(inflater.inflate(R.layout.item_content_toggle, parent, false));
    }

    @Override
    protected void onBindViewHolder(@NonNull ViewHolder holder, @NonNull ItemToggleBean item) {
        super.onBindViewHolder(holder, item);
        holder.toggleButton.setChecked(item.isChecked());
        holder.toggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                item.onChange(v, holder.getAdapterPosition(), holder.toggleButton.isChecked());
            }
        });
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.toggleButton.toggle();
                item.onChange(v, holder.getAdapterPosition(), holder.toggleButton.isChecked());
            }
        });
    }

    static class ViewHolder extends ItemCommonViewHolder {
        ToggleButton toggleButton;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            toggleButton = itemView.findViewById(R.id.tbStatusChange);
        }
    }
}
