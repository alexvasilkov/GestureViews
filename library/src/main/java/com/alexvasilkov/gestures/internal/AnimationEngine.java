package com.alexvasilkov.gestures.internal;

import android.view.View;

import androidx.annotation.NonNull;

public abstract class AnimationEngine implements Runnable {

    private final View view;
    private final Fps fps;

    public AnimationEngine(@NonNull View view) {
        this.view = view;
        this.fps = GestureDebug.isDebugFps() ? new Fps() : null;
    }

    @Override
    public final void run() {
        boolean continueAnimation = onStep();

        if (fps != null) {
            fps.step();
            if (!continueAnimation) {
                fps.stop();
            }
        }

        if (continueAnimation) {
            scheduleNextStep();
        }
    }

    public abstract boolean onStep();

    private void scheduleNextStep() {
        view.removeCallbacks(this);
        view.postOnAnimation(this);
    }

    public void start() {
        if (fps != null) {
            fps.start();
        }

        scheduleNextStep();
    }

}
