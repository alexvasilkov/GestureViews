package com.alexvasilkov.gestures.sample.utils.glide;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import android.widget.ImageView;

import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.BitmapImageViewTarget;

class GlideImageTarget extends BitmapImageViewTarget {

    private static final long NO_ANIMATION_INTERVAL = 150L;

    private long startTime = 0L;

    GlideImageTarget(ImageView view) {
        super(view);
    }

    @Override
    public void onLoadStarted(Drawable placeholder) {
        super.onLoadStarted(placeholder);
        startTime = SystemClock.uptimeMillis();
    }

    @Override
    public void onResourceReady(Bitmap resource,
            GlideAnimation<? super Bitmap> glideAnimation) {

        if (startTime == 0 || SystemClock.uptimeMillis() - startTime < NO_ANIMATION_INTERVAL) {
            startTime = 0L;
            glideAnimation = null;
        }

        super.onResourceReady(resource, glideAnimation);
    }

    @Override
    public void onLoadCleared(Drawable placeholder) {
        super.onLoadCleared(placeholder);
        startTime = 0L;
    }

}
