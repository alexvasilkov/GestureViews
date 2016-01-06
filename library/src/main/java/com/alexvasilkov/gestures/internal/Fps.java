package com.alexvasilkov.gestures.internal;

import android.os.SystemClock;
import android.util.Log;

public class Fps {

    private static final String TAG = "GestureFps";

    private static final long WARNING_TIME = 17L; // About 60 fps
    private static final long ERROR_TIME = 33L; // About 30 fps

    private long mFrameStart;
    private long mAnimationStart;
    private int mFramesCount;

    public void start() {
        if (GestureDebug.isDebugFps()) {
            mAnimationStart = mFrameStart = SystemClock.uptimeMillis();
            mFramesCount = 0;
        }
    }

    public void stop() {
        if (GestureDebug.isDebugFps() && mFramesCount > 0) {
            long time = SystemClock.uptimeMillis() - mAnimationStart;
            Log.d(TAG, "Average FPS: " + (1000 * mFramesCount / time));
        }
    }

    public void step() {
        if (GestureDebug.isDebugFps()) {
            long frameTime = SystemClock.uptimeMillis() - mFrameStart;
            if (frameTime > ERROR_TIME) {
                Log.e(TAG, "Frame time: " + frameTime);
            } else if (frameTime > WARNING_TIME) {
                Log.w(TAG, "Frame time: " + frameTime);
            }

            mFramesCount++;
            mFrameStart = SystemClock.uptimeMillis();
        }
    }

}
