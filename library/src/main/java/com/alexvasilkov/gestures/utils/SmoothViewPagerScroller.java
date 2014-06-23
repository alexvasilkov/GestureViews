package com.alexvasilkov.gestures.utils;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.Scroller;
import com.alexvasilkov.gestures.BuildConfig;

import java.lang.reflect.Field;

public class SmoothViewPagerScroller extends Scroller {

    private static final int BASE_DURATION = 500;

    private final int mDuration;
    private final float mDurationFactor;
    private boolean mIsFixedDuration;

    /**
     * Creates and applies SmoothViewPagerScroller to given ViewPager
     */
    public static SmoothViewPagerScroller applySmoothScroller(ViewPager pager, int duration) {
        try {
            Field scrollerField = ViewPager.class.getDeclaredField("mScroller");
            scrollerField.setAccessible(true);
            SmoothViewPagerScroller scroller = new SmoothViewPagerScroller(pager.getContext(),
                    new DecelerateInterpolator(), duration, duration / (float) BASE_DURATION);
            scrollerField.set(pager, scroller);
            return scroller;
        } catch (NoSuchFieldException e) {
            if (BuildConfig.DEBUG) e.printStackTrace();
        } catch (IllegalArgumentException e) {
            if (BuildConfig.DEBUG) e.printStackTrace();
        } catch (IllegalAccessException e) {
            if (BuildConfig.DEBUG) e.printStackTrace();
        }
        return null;
    }

    private SmoothViewPagerScroller(Context context, Interpolator interpolator, int duration, float durationFactor) {
        super(context, interpolator);
        mDuration = duration;
        mDurationFactor = durationFactor;
    }

    public void setFixedDuration(boolean isFixedDuration) {
        mIsFixedDuration = isFixedDuration;
    }

    @Override
    public void startScroll(int startX, int startY, int dx, int dy, int duration) {
        // Ignores received duration, uses fixed one instead
        int d = mIsFixedDuration ? mDuration : Math.round(Math.max(duration, mDuration) * mDurationFactor);
        super.startScroll(startX, startY, dx, dy, d);
    }

}
