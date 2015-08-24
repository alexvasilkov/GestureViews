package com.alexvasilkov.gestures.internal;

import android.os.Build;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;

public abstract class AnimationEngine implements Runnable {

    private static final String TAG = "GestureFps";
    private static final long FRAME_TIME = 10L;

    private static final long WARNING_TIME = 17L; // About 60 fps
    private static final long ERROR_TIME = 33L; // About 30 fps

    private final View mView;

    private long mFrameStart;
    private long mAnimationStart;
    private int mFramesCount;

    public AnimationEngine(@NonNull View view) {
        mView = view;
    }

    @Override
    public final void run() {
        boolean continueAnimation = onStep();

        if (GestureDebug.isDebugFps()) {
            mFramesCount++;
            long now = SystemClock.uptimeMillis();

            long frameTime = now - mFrameStart;
            if (frameTime > ERROR_TIME) {
                Log.e(TAG, "Frame time: " + frameTime);
            } else if (frameTime > WARNING_TIME) {
                Log.w(TAG, "Frame time: " + frameTime);
            }

            if (!continueAnimation) {
                long time = now - mAnimationStart;
                Log.d(TAG, "Average FPS: " + (1000 * mFramesCount / time));
            }
        }

        if (continueAnimation) scheduleNextStep();
    }

    public abstract boolean onStep();

    private void scheduleNextStep() {
        if (GestureDebug.isDebugFps()) {
            mFrameStart = SystemClock.uptimeMillis();
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            mView.postDelayed(this, FRAME_TIME);
        } else {
            mView.postOnAnimationDelayed(this, FRAME_TIME);
        }
    }

    public void start() {
        if (GestureDebug.isDebugFps()) {
            mAnimationStart = SystemClock.uptimeMillis();
            mFramesCount = 0;
        }

        scheduleNextStep();
    }

}
