package com.alexvasilkov.gestures.sample.utils.glide;

import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import android.widget.ImageView;

import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;

public class GlideDrawableTarget extends GlideDrawableImageViewTarget {

    private static final long NO_ANIMATION_INTERVAL = 150L;

    private long mStartTime = 0L;

    public GlideDrawableTarget(ImageView view) {
        super(view);
    }

    @Override
    public void onLoadStarted(Drawable placeholder) {
        super.onLoadStarted(placeholder);
        mStartTime = SystemClock.uptimeMillis();
    }

    @Override
    public void onResourceReady(GlideDrawable resource,
                                GlideAnimation<? super GlideDrawable> glideAnimation) {

        if (mStartTime == 0 || SystemClock.uptimeMillis() - mStartTime < NO_ANIMATION_INTERVAL) {
            mStartTime = 0L;
            glideAnimation = null;
        }

        super.onResourceReady(resource, glideAnimation);
    }

    @Override
    public void onLoadCleared(Drawable placeholder) {
        super.onLoadCleared(placeholder);
        mStartTime = 0L;
    }

}
