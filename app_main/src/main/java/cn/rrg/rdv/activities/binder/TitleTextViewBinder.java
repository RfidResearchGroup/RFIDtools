package cn.rrg.rdv.activities.binder;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import cn.rrg.rdv.R;
import cn.rrg.rdv.javabean.TitleTextBean;
import me.drakeet.multitype.ItemViewBinder;

public class TitleTextViewBinder extends ItemViewBinder<TitleTextBean, TitleTextViewBinder.TitleTextHolder> {

    static class TitleTextHolder extends RecyclerView.ViewHolder {
        TextView txtTitle;

        TitleTextHolder(@NonNull View itemView) {
            super(itemView);
            txtTitle = itemView.findViewById(R.id.txtTitle);
        }
    }

    @NonNull
    @Override
    protected TitleTextHolder onCreateViewHolder(@NonNull LayoutInflater inflater, @NonNull ViewGroup parent) {
        return new TitleTextHolder(inflater.inflate(R.layout.item_list_title, parent, false));
    }

    @Override
    protected void onBindViewHolder(@NonNull TitleTextHolder holder, @NonNull TitleTextBean item) {
        holder.txtTitle.setText(item.getTitle());
    }
}
