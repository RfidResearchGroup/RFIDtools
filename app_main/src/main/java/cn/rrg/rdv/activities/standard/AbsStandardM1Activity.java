package cn.rrg.rdv.activities.standard;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.fragment.app.Fragment;

import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import java.util.Arrays;

import cn.dxl.common.implement.AnimationListenerImpl;
import cn.dxl.common.implement.OnPageChangeListenerImpl;
import cn.dxl.common.util.DisplayUtil;
import cn.dxl.common.widget.ViewPagerSlide;
import cn.rrg.rdv.R;
import cn.rrg.rdv.activities.main.BaseActivity;
import cn.rrg.rdv.adapter.FragmentListPagerAdapter;

/**
 * 一个抽象的标准M1卡操作活动
 * 封装了M1卡操作的必须动作与必须属性。
 */
public abstract class AbsStandardM1Activity
        extends BaseActivity {

    private ViewPagerSlide viewPager;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.act_std_mf_main);

        viewPager = findViewById(R.id.vpContainer);
        //得到碎片
        Fragment operaFragment = getTagOperateFragment();
        Fragment infoFragment = getInformatinFragment();
        //构建基于碎片的分页视图!
        FragmentListPagerAdapter pagerAdapter =
                new FragmentListPagerAdapter(
                        getSupportFragmentManager(), Arrays.asList(operaFragment, infoFragment));
        //设置适配器!
        viewPager.setAdapter(pagerAdapter);

        //初始化底部导航栏!
        bottomNavigationView = findViewById(R.id.bnvChangerSwitch);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                //在点击底部的视图时，切换按钮!
                switch (menuItem.getItemId()) {
                    case R.id.menu_operational_items:
                        viewPager.setCurrentItem(0);
                        break;
                    case R.id.menu_tag_information:
                        viewPager.setCurrentItem(1);
                        break;
                }
                return true;
            }
        });

        viewPager.addOnPageChangeListener(new OnPageChangeListenerImpl() {
            @Override
            public void onPageSelected(int i) {
                bottomNavigationView.setSelectedItemId(bottomNavigationView.getMenu().getItem(i).getItemId());
                if (i == 1) {
                    //隐藏底部的导航栏!
                    setBnvAnimationAndVisible(false);
                    //隐藏视图，触发动画！
                    DisplayUtil.dismissStatusAndNavigationBar(activity);
                } else {
                    //是0，则是第一个页面，我们需要显示底部的导航栏!
                    setBnvAnimationAndVisible(true);
                    //显示视图，触发动画！
                    DisplayUtil.showStatusAndNavigationBar(activity);
                }
            }
        });
    }

    private void setBnvAnimationAndVisible(boolean isVisible) {
        if (isVisible) {
            bottomNavigationView.clearAnimation();
            Animation fadeAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in);
            bottomNavigationView.setAnimation(fadeAnimation);
            fadeAnimation.setAnimationListener(new AnimationListenerImpl() {
                @Override
                public void onAnimationEnd(Animation animation) {
                    bottomNavigationView.clearAnimation();
                    bottomNavigationView.setVisibility(View.VISIBLE);
                }
            });
        } else {
            Animation fadeAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_out);
            bottomNavigationView.setAnimation(fadeAnimation);
            fadeAnimation.setAnimationListener(new AnimationListenerImpl() {
                @Override
                public void onAnimationEnd(Animation animation) {
                    bottomNavigationView.clearAnimation();
                    bottomNavigationView.setVisibility(View.GONE);
                }
            });
        }
    }

    public void setCanChange(boolean canChange) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                viewPager.setScrollable(canChange);
                if (canChange) {
                    bottomNavigationView.setVisibility(View.VISIBLE);
                } else {
                    bottomNavigationView.setVisibility(View.GONE);
                }
            }
        });
    }

    /**
     * 信息承载,用于显示Tag的一些信息。
     * 此碎片应当尽量做到风格统一!
     *
     * @return 承载信息的碎片
     */
    protected abstract Fragment getInformatinFragment();

    /**
     * 信息承载,用于显示Tag的一些信息。
     * 此碎片可根据设备的不同而对于一些选项的实现也不同，
     * 但也应当尽量的做到风格统一!
     *
     * @return 承载操作的碎片
     */
    protected abstract Fragment getTagOperateFragment();
}
