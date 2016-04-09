package com.alexvasilkov.gestures.sample.utils.glide;

import android.graphics.Bitmap;

import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

abstract class GlideDrawableListener implements RequestListener<String, Bitmap> {

    @Override
    public boolean onException(Exception ex, String url,
            Target<Bitmap> target, boolean isFirstResource) {
        onFail(url);
        return false;
    }

    @Override
    public boolean onResourceReady(Bitmap resource, String url,
            Target<Bitmap> target, boolean isFromMemoryCache, boolean isFirstResource) {
        onSuccess(url);
        return false;
    }

    public void onSuccess(String url) {
        // No-op
    }

    public void onFail(String url) {
        // No-op
    }

}
