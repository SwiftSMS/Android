package com.icc.ui.view.anim;

import android.view.View;
import android.view.animation.Animation;

/**
 * @author Rob Powell
 */
public class AnimationRunner implements Runnable, Animation.AnimationListener{

    private View view;
    private int duration;
    private Animation animation;
    private int endVisibility;

    /**
     *
     * @param view
     *          The View to Animate
     * @param duration
     *          The duration of the animation
     * @param animation
     *          The Animation
     * @param endVisibility
     *          The View visibility after the animation has finished
     */
    public AnimationRunner(View view, int duration, Animation animation, int endVisibility){
        this.view = view;
        this.duration = duration;
        this.animation = animation;
        this.endVisibility = endVisibility;
        this.animation.setAnimationListener(AnimationRunner.this);
    }
    @Override
    public void run() {

        animation.setDuration(duration);
        view.startAnimation(animation);
    }

    @Override
    public void onAnimationStart(Animation animation) {
        if(view.getVisibility() == View.GONE)
            view.setVisibility(View.VISIBLE);
    }

    @Override
    public void onAnimationEnd(Animation animation) {
        if(endVisibility == View.GONE) {
            view.setVisibility(endVisibility);
        } else if(endVisibility == View.VISIBLE) {
            view.setVisibility(endVisibility);
        }
    }

    @Override
    public void onAnimationRepeat(Animation animation) {

    }
}