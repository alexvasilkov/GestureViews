package com.alexvasilkov.gestures.sample.ui.ex;

import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.NoTransition;
import com.bumptech.glide.request.transition.Transition;
import com.bumptech.glide.request.transition.TransitionFactory;

public class GlideHelper {

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
                    return dataSource != DataSource.MEMORY_CACHE
                            ? TRANSITION : NoTransition.<Drawable>get();
                }
            };


    private GlideHelper() {}


    public static void loadResource(int drawableId, ImageView image) {
        // We don't want Glide to crop or resize our image
        final RequestOptions options = new RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.DATA)
                .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                .dontTransform();

        Glide.with(image).load(drawableId)
                .apply(options)
                .transition(DrawableTransitionOptions.with(TRANSITION_FACTORY))
                .into(image);
    }

    public static void clear(ImageView view) {
        Glide.with(view).clear(view);
        view.setImageDrawable(null);
    }

}
