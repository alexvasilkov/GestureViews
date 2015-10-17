package com.alexvasilkov.gestures.sample.utils.glide;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.ViewPropertyAnimation;
import com.googlecode.flickrjandroid.photos.Photo;

public class GlideHelper {

    private static final ViewPropertyAnimation.Animator ANIMATOR =
            new ViewPropertyAnimation.Animator() {
                @Override
                public void animate(View view) {
                    view.setAlpha(0f);
                    view.animate().alpha(1f);
                }
            };


    public static void loadFlickrThumb(@Nullable Photo photo, @NonNull final ImageView image) {
        Glide.with(image.getContext())
                .load(photo == null ? null : photo.getMediumUrl())
                .dontAnimate()
                .thumbnail(Glide.with(image.getContext())
                        .load(photo == null ? null : photo.getThumbnailUrl())
                        .animate(ANIMATOR)
                        .diskCacheStrategy(DiskCacheStrategy.SOURCE))
                .into(new GlideDrawableTarget(image));
    }

    public static void loadFlickrFull(@NonNull Photo photo,
                                      @NonNull final ImageView image,
                                      @Nullable final ImageLoadingListener listener) {

        final String photoUrl = photo.getLargeSize() == null
                ? photo.getMediumUrl() : photo.getLargeUrl();

        Glide.with(image.getContext())
                .load(photoUrl)
                .dontAnimate()
                .placeholder(image.getDrawable())
                .thumbnail(Glide.with(image.getContext())
                        .load(photo.getThumbnailUrl())
                        .animate(ANIMATOR)
                        .diskCacheStrategy(DiskCacheStrategy.SOURCE))
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .listener(new GlideDrawableListener() {
                    @Override
                    public void onSuccess(String url) {
                        if (url.equals(photoUrl)) {
                            if (listener != null) listener.onLoaded();
                        }
                    }

                    @Override
                    public void onFail(String url) {
                        if (listener != null) listener.onFailed();
                    }
                })
                .into(new GlideDrawableTarget(image));
    }


    public interface ImageLoadingListener {
        void onLoaded();

        void onFailed();
    }

}
