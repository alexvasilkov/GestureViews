package com.alexvasilkov.gestures.internal;

import android.os.SystemClock;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;

/**
 * A simple class that animates float values.Functionally similar to a
 * {@link android.widget.Scroller}.
 */
public class FloatScroller {

    public static final int DEFAULT_DURATION = 250;

    private final Interpolator mInterpolator;

    private boolean mFinished = true;

    private float mStartValue, mFinalValue;

    /**
     * Current value computed by {@link #computeScroll()}.
     */
    private float mCurrValue;

    /**
     * The time the animation started, computed using {@link SystemClock#elapsedRealtime()}.
     */
    private long mStartRTC;

    private long mDuration = DEFAULT_DURATION;

    public FloatScroller() {
        mInterpolator = new AccelerateDecelerateInterpolator();
    }

    public long getDuration() {
        return mDuration;
    }

    public void setDuration(long duration) {
        mDuration = duration;
    }

    /**
     * Force the finished field to a particular value.<br/>
     * Unlike {@link #abortAnimation()} the current value isn't set to the final value.
     *
     * @see android.widget.Scroller#forceFinished(boolean)
     */
    public void forceFinished() {
        mFinished = true;
    }

    /**
     * Aborts the animation, setting the current value to the final value.
     *
     * @see android.widget.Scroller#abortAnimation()
     */
    public void abortAnimation() {
        mFinished = true;
        mCurrValue = mFinalValue;
    }

    /**
     * Starts an animation from startValue to finalValue.
     *
     * @see android.widget.Scroller#startScroll(int, int, int, int)
     */
    public void startScroll(float startValue, float finalValue) {
        mFinished = false;
        mStartRTC = SystemClock.elapsedRealtime();

        mStartValue = startValue;
        mFinalValue = finalValue;
        mCurrValue = startValue;
    }

    /**
     * Computes the current value, returning true if the animation is still active and false if the
     * animation has finished.
     *
     * @see android.widget.Scroller#computeScrollOffset()
     */
    public boolean computeScroll() {
        if (mFinished) {
            return false;
        }

        long elapsed = SystemClock.elapsedRealtime() - mStartRTC;
        if (elapsed >= mDuration) {
            mFinished = true;
            mCurrValue = mFinalValue;
            return false;
        }

        float time = mInterpolator.getInterpolation((float) elapsed / mDuration);
        mCurrValue = interpolate(mStartValue, mFinalValue, time);
        return true;
    }

    /**
     * Returns current state.
     *
     * @see android.widget.Scroller#isFinished()
     */
    public boolean isFinished() {
        return mFinished;
    }

    /**
     * Returns starting value.
     *
     * @see android.widget.Scroller#getStartX()
     */
    public float getStart() {
        return mStartValue;
    }

    /**
     * Returns final value.
     *
     * @see android.widget.Scroller#getFinalX()
     */
    public float getFinal() {
        return mFinalValue;
    }

    /**
     * Returns the current value.
     *
     * @see android.widget.Scroller#getCurrX()
     */
    public float getCurr() {
        return mCurrValue;
    }

    private static float interpolate(float x1, float x2, float f) {
        return x1 + (x2 - x1) * f;
    }

}