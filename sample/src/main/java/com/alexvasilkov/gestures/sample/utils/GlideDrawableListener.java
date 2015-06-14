package com.alexvasilkov.gestures.sample.utils;

import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

public abstract class GlideDrawableListener implements RequestListener<String, GlideDrawable> {

    @Override
    public boolean onException(Exception e, String url,
                               Target<GlideDrawable> target,
                               boolean isFirstResource) {
        onFail(url);
        return false;
    }

    @Override
    public boolean onResourceReady(GlideDrawable resource, String url,
                                   Target<GlideDrawable> target,
                                   boolean isFromMemoryCache, boolean isFirstResource) {
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
