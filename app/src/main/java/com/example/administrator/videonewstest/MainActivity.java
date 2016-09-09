package com.example.administrator.videonewstest;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class MainActivity extends AppCompatActivity implements ViewPager.OnPageChangeListener{
    @BindView(R.id.btnNews) Button btnNews;
    @BindView(R.id.btnLikes) Button btnLikes;
    @BindView(R.id.btnLocal) Button btnLocal;
    @BindView(R.id.viewPager)ViewPager viewPager;
    private Unbinder mUnbinder;

    private final FragmentPagerAdapter adapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
        //利用getItem返回每一个Fragment,没必要建立一个集合
        @Override
        public Fragment getItem(int position) {
            switch (position){
                case 0:
                case 1:
                case 2:
                    return new LocalVideoFragment();
                default:
                    // TODO: 2016/9/9 0009
                    throw new RuntimeException("未知");
            }
        }

        @Override
        public int getCount() {
            return 3;
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    /**
     * 加载完布局执行此方法
     */
    @Override
    public void onContentChanged() {
        super.onContentChanged();
        mUnbinder = ButterKnife.bind(this);
        viewPager.setAdapter(adapter);
        // viewpager的监听 - 用于Button的切换
        viewPager.addOnPageChangeListener(this);
        // 首次进入默认选中在线新闻
        btnNews.setSelected(true);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //activity销毁时要对butterknife进行解绑
        mUnbinder.unbind();
    }
    /**
     * 点击按键也完成切换Fragment
     */
    @OnClick({R.id.btnNews,R.id.btnLikes,R.id.btnLocal})
    public void chooseFragment(Button button){
        switch (button.getId()) {
            case R.id.btnNews:
                //false代表不展示切换动画
                viewPager.setCurrentItem(0, false);
                break;
            case R.id.btnLocal:
                viewPager.setCurrentItem(1, false);
                break;
            case R.id.btnLikes:
                viewPager.setCurrentItem(2, false);
                break;
            default:
                // TODO: 2016/9/9 0009
                throw new RuntimeException("未知");
        }
    }

    /**
     * ViewPager滚动监听
     * @param position
     * @param positionOffset
     * @param positionOffsetPixels
     */
    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        //ViewPager切换时,Button背景随着变化
        btnNews.setSelected(position == 0);
        btnLocal.setSelected(position == 1);
        btnLikes.setSelected(position == 2);
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }
}
