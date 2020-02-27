package cn.rrg.rdv.holder;

import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.widget.AppCompatImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.zhpan.bannerview.holder.ViewHolder;

import cn.dxl.common.widget.OvalImageView;
import cn.rrg.rdv.R;
import cn.rrg.rdv.javabean.BannerBean;

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
