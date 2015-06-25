package com.alexvasilkov.gestures.sample.utils;

import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.googlecode.flickrjandroid.photos.Photo;

public class GlideHelper {

    public static void loadFlickrThumb(@Nullable Photo photo, @NonNull final ImageView image) {
        Glide.with(image.getContext())
                .load(photo == null ? null : photo.getMediumUrl())
                .dontAnimate()
                .thumbnail(Glide.with(image.getContext())
                        .load(photo == null ? null : photo.getThumbnailUrl())
                        .diskCacheStrategy(DiskCacheStrategy.SOURCE))
                .into(new GlideDrawableTarget(image));
    }

    public static void loadFlickrFull(@NonNull Photo photo,
                                      @NonNull final ImageView image, @NonNull final View progress,
                                      @Nullable final OnImageLoadedListener listener) {

        final String photoUrl = photo.getLargeSize() == null
                ? photo.getMediumUrl() : photo.getLargeUrl();

        Glide.with(image.getContext())
                .load(photoUrl)
                .thumbnail(Glide.with(image.getContext())
                        .load(photo.getThumbnailUrl())
                        .diskCacheStrategy(DiskCacheStrategy.SOURCE))
                .placeholder(image.getDrawable())
                .dontAnimate()
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .listener(new GlideDrawableListener() {
                    @Override
                    public void onSuccess(String url) {
                        if (url.equals(photoUrl)) {
                            progress.animate().alpha(0f);
                            if (listener != null) listener.onImageLoaded();
                        }
                    }

                    @Override
                    public void onFail(String url) {
                        progress.animate().alpha(0f);
                    }
                })
                .into(new GlideDrawableTarget(image) {
                    @Override
                    public void onLoadStarted(Drawable placeholder) {
                        super.onLoadStarted(placeholder);
                        progress.animate().alpha(1f);
                    }
                });
    }


    public interface OnImageLoadedListener {
        void onImageLoaded();
    }

}
