package com.alexvasilkov.gestures.sample.ui.demo.utils;

import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.NoTransition;
import com.bumptech.glide.request.transition.Transition;
import com.bumptech.glide.request.transition.TransitionFactory;
import com.googlecode.flickrjandroid.photos.Photo;

public class DemoGlideHelper {

    private static final Transition<Drawable> TRANSITION = new Transition<Drawable>() {
        @Override
        public boolean transition(Drawable current, ViewAdapter adapter) {
            if (adapter.getView() instanceof ImageView) {
                ImageView image = (ImageView) adapter.getView();
                if (image.getDrawable() == null) {
                    image.setAlpha(0f);
                    image.animate().alpha(1f);
                }
            }
            return false;
        }
    };

    private static final TransitionFactory<Drawable> TRANSITION_FACTORY =
            new TransitionFactory<Drawable>() {
                @Override
                public Transition<Drawable> build(DataSource dataSource, boolean isFirstResource) {
                    // Do not animate if image is loaded from memory
                    return dataSource == DataSource.REMOTE
                            ? TRANSITION : NoTransition.<Drawable>get();
                }
            };


    private DemoGlideHelper() {}


    public static void loadFlickrThumb(Photo photo, ImageView image) {
        final RequestOptions options = new RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.DATA)
                .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                .dontTransform();

        final RequestBuilder<Drawable> thumbRequest = Glide.with(image)
                .load(photo.getThumbnailUrl())
                .apply(options)
                .transition(DrawableTransitionOptions.with(TRANSITION_FACTORY));

        Glide.with(image).load(photo.getMediumUrl())
                .apply(options)
                .thumbnail(thumbRequest)
                .into(image);
    }

    public static void loadFlickrFull(Photo photo, ImageView image, LoadingListener listener) {
        final String photoUrl = photo.getLargeSize() == null
                ? photo.getMediumUrl() : photo.getLargeUrl();

        final RequestOptions options = new RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.DATA)
                .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                .dontTransform();

        final RequestBuilder<Drawable> thumbRequest = Glide.with(image)
                .load(photo.getThumbnailUrl())
                .apply(options);

        Glide.with(image)
                .load(photoUrl)
                .apply(new RequestOptions().apply(options).placeholder(image.getDrawable()))
                .thumbnail(thumbRequest)
                .listener(new RequestListenerWrapper<Drawable>(listener))
                .into(image);
    }

    public static void clear(ImageView view) {
        Glide.with(view).clear(view);
        view.setImageDrawable(null);
    }


    public interface LoadingListener {
        void onSuccess();

        void onError();
    }

    private static class RequestListenerWrapper<T> implements RequestListener<T> {

        private final LoadingListener listener;

        RequestListenerWrapper(@Nullable LoadingListener listener) {
            this.listener = listener;
        }

        @Override
        public boolean onResourceReady(T resource, Object model, Target<T> target,
                DataSource dataSource, boolean isFirstResource) {
            if (listener != null) {
                listener.onSuccess();
            }
            return false;
        }

        @Override
        public boolean onLoadFailed(@Nullable GlideException ex, Object model,
                Target<T> target, boolean isFirstResource) {
            if (listener != null) {
                listener.onError();
            }
            return false;
        }
    }

}
