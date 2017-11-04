package com.alexvasilkov.gestures.utils;

import android.os.SystemClock;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;

/**
 * A simple class that animates float values.Functionally similar to a
 * {@link android.widget.Scroller}.
 */
public class FloatScroller {

    private static final long DEFAULT_DURATION = 250L;

    private final Interpolator interpolator;

    private boolean finished = true;

    private float startValue;
    private float finalValue;

    /**
     * Current value computed by {@link #computeScroll()}.
     */
    private float currValue;

    /**
     * The time the animation started, computed using {@link SystemClock#elapsedRealtime()}.
     */
    private long startRtc;

    private long duration = DEFAULT_DURATION;

    public FloatScroller() {
        interpolator = new AccelerateDecelerateInterpolator();
    }

    @SuppressWarnings("unused") // Public API
    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    /**
     * Force the finished field to a particular value.<br>
     * Unlike {@link #abortAnimation()} the current value isn't set to the final value.
     *
     * @see android.widget.Scroller#forceFinished(boolean)
     */
    public void forceFinished() {
        finished = true;
    }

    /**
     * Aborts the animation, setting the current value to the final value.
     *
     * @see android.widget.Scroller#abortAnimation()
     */
    @SuppressWarnings({ "unused", "WeakerAccess" }) // Public API
    public void abortAnimation() {
        finished = true;
        currValue = finalValue;
    }

    /**
     * Starts an animation from startValue to finalValue.
     *
     * @param startValue Start value
     * @param finalValue Final value
     * @see android.widget.Scroller#startScroll(int, int, int, int)
     */
    public void startScroll(float startValue, float finalValue) {
        finished = false;
        startRtc = SystemClock.elapsedRealtime();

        this.startValue = startValue;
        this.finalValue = finalValue;
        currValue = startValue;
    }

    /**
     * Computes the current value, returning true if the animation is still active and false if the
     * animation has finished.
     *
     * @return Computed scroll
     * @see android.widget.Scroller#computeScrollOffset()
     */
    public boolean computeScroll() {
        if (finished) {
            return false;
        }

        long elapsed = SystemClock.elapsedRealtime() - startRtc;
        if (elapsed >= duration) {
            finished = true;
            currValue = finalValue;
            return false;
        }

        float time = interpolator.getInterpolation((float) elapsed / duration);
        currValue = interpolate(startValue, finalValue, time);
        return true;
    }

    /**
     * @return Current state
     * @see android.widget.Scroller#isFinished()
     */
    public boolean isFinished() {
        return finished;
    }

    /**
     * @return Starting value
     * @see android.widget.Scroller#getStartX()
     */
    public float getStart() {
        return startValue;
    }

    /**
     * @return Final value
     * @see android.widget.Scroller#getFinalX()
     */
    public float getFinal() {
        return finalValue;
    }

    /**
     * @return Current value
     * @see android.widget.Scroller#getCurrX()
     */
    public float getCurr() {
        return currValue;
    }

    private static float interpolate(float x1, float x2, float state) {
        return x1 + (x2 - x1) * state;
    }

}
