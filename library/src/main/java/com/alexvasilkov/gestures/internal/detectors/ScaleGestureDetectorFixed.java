package com.alexvasilkov.gestures.internal.detectors;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

/**
 * 'Double tap and swipe' mode works bad for fast gestures. This class tries to fix this issue.
 */
public class ScaleGestureDetectorFixed extends ScaleGestureDetector {

    private float currY;
    private float prevY;
    private float lastScaleFactor;

    public ScaleGestureDetectorFixed(Context context, OnScaleGestureListener listener) {
        super(context, listener);
        warmUpScaleDetector();
    }

    /**
     * Scale detector is a little buggy when first time scale is occurred.
     * So we will feed it with fake motion event to warm it up.
     */
    private void warmUpScaleDetector() {
        long time = System.currentTimeMillis();
        MotionEvent event = MotionEvent.obtain(time, time, MotionEvent.ACTION_CANCEL, 0f, 0f, 0);
        onTouchEvent(event);
        event.recycle();
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        final boolean result = super.onTouchEvent(event);

        prevY = currY;
        currY = event.getY();

        if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
            prevY = event.getY();
            lastScaleFactor = 1f;
        }

        return result;
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private boolean isInDoubleTapMode() {
        // Indirectly determine double tap mode
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
                && isQuickScaleEnabled() && getCurrentSpan() == getCurrentSpanY();
    }

    @Override
    public float getScaleFactor() {
        float factor = super.getScaleFactor();
        float lastFactor = lastScaleFactor;
        lastScaleFactor = factor;

        if (isInDoubleTapMode()) {
            return (currY > prevY && factor > 1f) || (currY < prevY && factor < 1f)
                    ? factor : lastFactor;
        }

        return factor;
    }

}
