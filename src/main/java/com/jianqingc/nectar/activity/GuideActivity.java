package com.jianqingc.nectar.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.app.Activity;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.Gravity;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import com.jianqingc.nectar.R;
import java.util.ArrayList;
import java.util.List;

public class GuideActivity extends AppCompatActivity {

    private ViewPager mViewPager;
    /**
     * LinearLayout with circles/beads
     */
    private LinearLayout indicatorLayout;
    /**
     * ViewPager Guide pages
     */
    private List<View> views;
    /**
     * Circles/Balls at the bottom of ViewPager
     */
    private ImageView[] mImageViews;
    private MyPagerAdapter myPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guide);

    }

    @Override
    protected void onStop() {
        super.onStop();
        this.finish();

    }

    @Override
    protected void onStart() {
        super.onStart();
        SharedPreferences sharedPreferences =  getApplicationContext().getSharedPreferences("nectar_android", 0);
        /**
         * Make sure the guide pages are only shown once.
         */
        Boolean isFirstRun = sharedPreferences.getBoolean("isFirstRun",true);
        if (!isFirstRun){
            initLogin();
        }else{
            initGuide();

        }
    }

    public void initLogin(){
        Intent i = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(i);
    }
    public void initGuide(){
        mViewPager = (ViewPager) findViewById(R.id.guide_viewPager);
        indicatorLayout = (LinearLayout) findViewById(R.id.beadsLayout);
        LayoutInflater inflater = LayoutInflater.from(this);
        views = new ArrayList<View>();
        views.add(inflater.inflate(R.layout.pager_layout1, null));
        views.add(inflater.inflate(R.layout.pager_layout2, null));
        views.add(inflater.inflate(R.layout.pager_layout3, null));
        views.add(inflater.inflate(R.layout.pager_layout4, null));
        myPagerAdapter = new MyPagerAdapter(this, views);
        mImageViews = new ImageView[views.size()];
        drawCircle();
        mImageViews[0].setImageResource(R.drawable.round_2);
        mViewPager.setAdapter(myPagerAdapter);
        mViewPager.addOnPageChangeListener(new GuidePageChangeListener());
    }

    private void drawCircle() {
        int num = views.size();
        for (int i = 0; i < num; i++) {
            mImageViews[i] = new ImageView(this);
            mImageViews[i].setImageResource(R.drawable.circle_2);
            mImageViews[i].setPadding(5, 5, 5, 5);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            params.gravity = Gravity.CENTER_VERTICAL;
            indicatorLayout.addView(mImageViews[i], params);
        }

    }
    private class GuidePageChangeListener implements OnPageChangeListener {
        public void onPageScrollStateChanged(int arg0) {
        }

        public void onPageScrolled(int arg0, float arg1, int arg2) {
        }

        /**
         * set this page as black round bead and set the rest as circle/donut
         */
        public void onPageSelected(int arg0) {
            for (int i = 0; i < mImageViews.length; i++) {
                if (arg0 != i) {
                    mImageViews[i]
                            .setImageResource(R.drawable.circle_2);
                } else {
                    mImageViews[i]
                            .setImageResource(R.drawable.round_2);
                }
            }
        }
    }


    class MyPagerAdapter extends PagerAdapter {
        private List<View> mViews;
        private Activity mContext;

        public MyPagerAdapter(Activity context, List<View> views) {
            this.mViews = views;
            this.mContext = context;
        }

        @Override
        public int getCount() {
            return mViews.size();
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;
        }

        @Override
        public int getItemPosition(Object object) {
            return super.getItemPosition(object);
        }

        @Override
        public void destroyItem(ViewGroup arg0, int arg1, Object arg2) {
            //arg0.removeViewInLayout(findViewById(R.id.beadsLayout));
            arg0.removeView(mViews.get(arg1));
        }

        /**
         * instantiate guide pages
         */
        @Override
        public Object instantiateItem(ViewGroup arg0, int arg1) {
            (arg0).addView(mViews.get(arg1), 0);
            if (arg1 == mViews.size() - 1) {
                Button enterBtn = (Button) arg0
                        .findViewById(R.id.guide_enter);
                enterBtn.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        /**
                         * set isFirstRun as false and jump to login activity
                         */
                        SharedPreferences sharedPreferences =  getApplicationContext().getSharedPreferences("nectar_android", 0);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putBoolean("isFirstRun",false);
                        editor.apply();
                        initLogin();
                    }
                });
            }
            return mViews.get(arg1);
        }
    }





}
