package com.alexvasilkov.gestures.internal;

import android.os.SystemClock;
import android.util.Log;

class Fps {

    private static final String TAG = "GestureFps";

    private static final long WARNING_TIME = 17L; // About 60 fps
    private static final long ERROR_TIME = 33L; // About 30 fps

    private long frameStart;
    private long animationStart;
    private int framesCount;

    void start() {
        if (GestureDebug.isDebugFps()) {
            animationStart = frameStart = SystemClock.uptimeMillis();
            framesCount = 0;
        }
    }

    void stop() {
        if (GestureDebug.isDebugFps() && framesCount > 0) {
            long time = SystemClock.uptimeMillis() - animationStart;
            Log.d(TAG, "Average FPS: " + (1000 * framesCount / time));
        }
    }

    void step() {
        if (GestureDebug.isDebugFps()) {
            long frameTime = SystemClock.uptimeMillis() - frameStart;
            if (frameTime > ERROR_TIME) {
                Log.e(TAG, "Frame time: " + frameTime);
            } else if (frameTime > WARNING_TIME) {
                Log.w(TAG, "Frame time: " + frameTime);
            }

            framesCount++;
            frameStart = SystemClock.uptimeMillis();
        }
    }

}
