package cn.rrg.rdv.binder;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import cn.rrg.rdv.R;
import cn.rrg.rdv.javabean.TitleBean;
import me.drakeet.multitype.ItemViewBinder;

public class ItemSingleTextBean extends ItemViewBinder<TitleBean, ItemSingleTextBean.ViewHolder> {

    @NonNull
    @Override
    protected ViewHolder onCreateViewHolder(@NonNull LayoutInflater inflater, @NonNull ViewGroup parent) {
        return new ViewHolder(inflater.inflate(R.layout.item_title, parent, false));
    }

    @Override
    protected void onBindViewHolder(@NonNull ViewHolder holder, @NonNull TitleBean item) {
        holder.txtTitle.setText(item.getTitle());
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtTitle;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtTitle = itemView.findViewById(R.id.txtTitle);
        }
    }
}
