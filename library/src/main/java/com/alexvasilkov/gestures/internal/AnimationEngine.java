package com.alexvasilkov.gestures.internal;

import android.os.Build;
import android.support.annotation.NonNull;
import android.view.View;

public abstract class AnimationEngine implements Runnable {

    private static final long FRAME_TIME = 10L;

    private final View mView;

    public AnimationEngine(@NonNull View view) {
        mView = view;
    }

    @Override
    public final void run() {
        if (onStep()) start();
    }

    public abstract boolean onStep();

    public void start() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            mView.postDelayed(this, FRAME_TIME);
        } else {
            mView.postOnAnimationDelayed(this, FRAME_TIME);
        }
    }

}
