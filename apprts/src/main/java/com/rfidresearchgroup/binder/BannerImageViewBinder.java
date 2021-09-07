package com.rfidresearchgroup.binder;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.rfidresearchgroup.rfidtools.R;
import com.zhpan.bannerview.BannerViewPager;
import com.zhpan.bannerview.constants.IndicatorGravity;
import com.zhpan.bannerview.holder.HolderCreator;
import com.zhpan.indicator.enums.IndicatorSlideMode;

import java.util.Arrays;

import com.rfidresearchgroup.common.util.DisplayUtil;
import com.rfidresearchgroup.holder.BannerImageHolder;
import com.rfidresearchgroup.javabean.BannerBean;
import me.drakeet.multitype.ItemViewBinder;

public class BannerImageViewBinder extends ItemViewBinder<BannerBean, BannerImageViewBinder.BannerHolder> {
    @NonNull
    @Override
    protected BannerHolder onCreateViewHolder(@NonNull LayoutInflater inflater, @NonNull ViewGroup parent) {
        return new BannerHolder(inflater.inflate(R.layout.item_act_main_banner, parent, false));
    }

    @Override
    protected void onBindViewHolder(@NonNull BannerHolder holder, @NonNull BannerBean item) {
        holder.pager.setCanLoop(true)
                .setPageMargin(56)
                .setIndicatorSlideMode(IndicatorSlideMode.SMOOTH)
                .setIndicatorMargin(0, 0, 0, DisplayUtil.dip2px(holder.itemView.getContext(), 4))
                .setIndicatorGravity(IndicatorGravity.CENTER)
                .setInterval(3000)
                .setHolderCreator(new HolderCreator<BannerImageHolder>() {
                    @Override
                    public BannerImageHolder createViewHolder() {
                        return new BannerImageHolder();
                    }
                }).create(Arrays.asList(item.getSubs()));
    }

    static class BannerHolder extends RecyclerView.ViewHolder {
        BannerViewPager<BannerBean, BannerImageHolder> pager;

        BannerHolder(@NonNull View itemView) {
            super(itemView);
            pager = itemView.findViewById(R.id.banner_view);
        }
    }
}
