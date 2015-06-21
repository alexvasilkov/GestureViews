package com.alexvasilkov.gestures.internal;

import android.os.Build;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.View;

public abstract class AnimationEngine implements Runnable {

    private static final long FRAME_TIME = 10L;

    private final Handler mHandler = new Handler();

    private View mView;

    @Override
    public final void run() {
        if (onStep()) start();
    }

    public abstract boolean onStep();

    public void start() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            mHandler.postDelayed(this, FRAME_TIME);
        } else if (mView == null) {
            mHandler.postDelayed(this, FRAME_TIME);
        } else {
            mView.postOnAnimationDelayed(this, FRAME_TIME);
        }
    }

    public void attachToView(@Nullable View view) {
        mView = view;
    }

}
