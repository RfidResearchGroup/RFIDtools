package com.rfidresearchgroup.fragment.init;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.viewpager.widget.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;

import com.rfidresearchgroup.adapter.MyViewPagerAdapter;
import com.rfidresearchgroup.fragment.base.BaseFragment;

import java.util.Arrays;

import com.rfidresearchgroup.common.implement.PermissionCallback;
import com.rfidresearchgroup.common.util.PermissionUtil;
import com.rfidresearchgroup.common.util.ViewUtil;
import com.rfidresearchgroup.rfidtools.R;

/*
 * 权限请求的碎片!
 * */
public class LoginFragment
        extends BaseFragment {

    private Button btnStartChecks;
    private PermissionUtil permissionUtil;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //初始化权限检查工具!
        permissionUtil = new PermissionUtil(getContext());
        Bundle data = getArguments();
        if (data != null) {
            permissionUtil.setPermissions(data.getStringArray("losePer"));
            permissionUtil.setCallback(new PermissionCallback() {
                @Override
                public void whatPermissionLose(String per, PermissionUtil util) {
                    if (getActivity() != null) {
                        //请求权限
                        ActivityCompat.requestPermissions(getActivity(), new String[]{per}, 0);
                    }
                }
            });
        }

        //初始化视图相关的实例!!!
        initViews();
        //初始化相关的动作!
        initActions();
    }

    private void initViews() {
        View view = getView();
        if (view != null) {
            //视图初始化正常，此处做出相关的处理!
            ViewPager vpBanner = view.findViewById(R.id.vpBanner);

            //初始化两个分页视图对象!
            View v1 = ViewUtil.inflate(getContext(), R.layout.item_act_guide_banner1);
            View v2 = ViewUtil.inflate(getContext(), R.layout.item_act_guide_banner2);

            btnStartChecks = v2.findViewById(R.id.btnStartChecks);

            //初始化适配器！
            MyViewPagerAdapter adapter = new MyViewPagerAdapter(Arrays.asList(v1, v2));
            //设置适配器!
            vpBanner.setAdapter(adapter);

            //设置动画!
            ImageView animView = v1.findViewById(R.id.image_anim);
            animView.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.alpha_repeat_1_0));
        }
    }

    private void initActions() {
        btnStartChecks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //取出丢失的权限，进行迭代获取!
                permissionUtil.checks();
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
