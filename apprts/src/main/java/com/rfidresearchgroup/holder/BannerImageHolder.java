package com.rfidresearchgroup.holder;

import android.view.View;

import com.bumptech.glide.Glide;
import com.rfidresearchgroup.rfidtools.R;
import com.zhpan.bannerview.holder.ViewHolder;

import com.rfidresearchgroup.common.widget.OvalImageView;
import com.rfidresearchgroup.javabean.BannerBean;

public class BannerImageHolder implements ViewHolder<BannerBean> {
    @Override
    public int getLayoutId() {
        return R.layout.item_act_img_banner;
    }

    @Override
    public void onBind(View itemView, BannerBean data, int position, int size) {
        OvalImageView imageView = itemView.findViewById(R.id.imgView_Banner);
        // 加载进IV中
        Glide.with(itemView.getContext()).load(data.getImgRes()).into(imageView);
    }

}
