package com.jianqingc.nectar.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.ImageView;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import com.jianqingc.nectar.R;
import com.jianqingc.nectar.util.SharedPreferencesUtil;

import java.util.concurrent.TimeUnit;

import static android.content.ContentValues.TAG;


public class WelcomeActivity extends Activity {

    @Bind(R.id.iv_entry)
    ImageView mIVEntry;

    private static final int ANIM_TIME = 2000;

    private static final float SCALE_END = 1.15F;

    private static final int[] Imgs = {R.drawable.welcome};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!this.isTaskRoot()) {
            Intent mainIntent = getIntent();
            String action = mainIntent.getAction();
            if (mainIntent.hasCategory(Intent.CATEGORY_LAUNCHER) && action.equals(Intent.ACTION_MAIN)) {
                Log.i(TAG,"---------- Not the top of Stack !!!!----------");
                finish();
                Log.i(TAG,"---------- Close the Welcome page !!!!----------");
                return;
            }
        }
        // 判断是否是第一次开启应用
        boolean isFirstOpen = SharedPreferencesUtil.getBoolean(this, SharedPreferencesUtil.FIRST_OPEN, true);
        // 如果是第一次启动，则先进入功能引导页
        if (isFirstOpen) {
            Log.i(TAG,"---------- First time to open the app ----------");
            Intent intent = new Intent(this, GuideActivity.class);
            Log.i(TAG,"---------- Open the Guide page ----------");
            startActivity(intent);
            finish();
            return;
        }
        setContentView(R.layout.activity_welcome);
        ButterKnife.bind(this);
        startLoginActivity();
    }

    private void startLoginActivity(){
        mIVEntry.setImageResource(Imgs[0]);
        Observable.timer(1000, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Long>()
                {

                    @Override
                    public void call(Long aLong)
                    {
                        startAnim();
                    }
                });
    }

    private void startAnim() {

        ObjectAnimator animatorX = ObjectAnimator.ofFloat(mIVEntry, "scaleX", 1f, SCALE_END);
        ObjectAnimator animatorY = ObjectAnimator.ofFloat(mIVEntry, "scaleY", 1f, SCALE_END);

        AnimatorSet set = new AnimatorSet();
        set.setDuration(ANIM_TIME).play(animatorX).with(animatorY);
        set.start();

        set.addListener(new AnimatorListenerAdapter()
        {

            @Override
            public void onAnimationEnd(Animator animation)
            {
                Intent intent = new Intent(WelcomeActivity.this, LoginActivity.class);
                startActivity(intent);
                Log.i(TAG,"---------- Open the Login page ----------");
                WelcomeActivity.this.finish();
                Log.i(TAG,"---------- Close the Welcome1 page ----------");
            }
        });
    }

    /**
     * 屏蔽物理返回按钮
     * @param keyCode
     * @param event
     * @return
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode==KeyEvent.KEYCODE_BACK){
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
