package com.alexvasilkov.gestures.internal;

import android.os.Build;
import android.support.annotation.NonNull;
import android.view.View;

public abstract class AnimationEngine implements Runnable {

    private static final long FRAME_TIME = 10L;

    private final View mView;
    private final Fps mFps;

    public AnimationEngine(@NonNull View view) {
        mView = view;
        mFps = GestureDebug.isDebugFps() ? new Fps() : null;
    }

    @Override
    public final void run() {
        boolean continueAnimation = onStep();

        if (mFps != null) {
            mFps.step();
            if (!continueAnimation) {
                mFps.stop();
            }
        }

        if (continueAnimation) {
            scheduleNextStep();
        }
    }

    public abstract boolean onStep();

    private void scheduleNextStep() {
        mView.removeCallbacks(this);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            mView.postDelayed(this, FRAME_TIME);
        } else {
            mView.postOnAnimationDelayed(this, FRAME_TIME);
        }
    }

    public void start() {
        if (mFps != null) {
            mFps.start();
        }

        scheduleNextStep();
    }

}
