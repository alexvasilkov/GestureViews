package com.alexvasilkov.gestures.sample.activities;

import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;

import com.alexvasilkov.android.commons.utils.Views;
import com.alexvasilkov.gestures.sample.R;
import com.alexvasilkov.gestures.sample.animation.Helper;
import com.alexvasilkov.gestures.widgets.GestureImageView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.googlecode.flickrjandroid.photos.Photo;

public class FlickrImageActivity extends BaseActivity {

    private Helper mHelper;


    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flickr_image);

        final GestureImageView imageView = Views.find(this, R.id.flickr_image);
        imageView.getController().getSettings()
                .setFillViewport(true)
                .setMaxZoom(3f)
                .disableGestures(); // Temporary disabling touch controls

        final View layout = Views.find(this, R.id.flickr_image_layout);
        int color = getResources().getColor(R.color.window_background_dark);
        final Drawable background = new ColorDrawable(color);
        layout.setBackgroundDrawable(background);

        mHelper = new Helper(this, imageView);
        mHelper.setAnimationUpdateListener(new Helper.AnimationUpdateListener() {
            @Override
            public void onAnimationUpdate(float animationState) {
                background.setAlpha((int) (255 * animationState));
            }
        });
        mHelper.enter(savedInstanceState);

        final Photo photo = mHelper.getItem();
        final String photoUrl = photo.getLargeSize() == null
                ? photo.getMediumUrl() : photo.getLargeUrl();

        Glide.with(this)
                .load(photoUrl)
                .thumbnail(Glide.with(this)
                        .load(photo.getThumbnailUrl())
                        .dontTransform()
                        .diskCacheStrategy(DiskCacheStrategy.SOURCE))
                .dontAnimate()
                .dontTransform()
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .listener(new RequestListener<String, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, String url,
                                               Target<GlideDrawable> target,
                                               boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, String url,
                                                   Target<GlideDrawable> target,
                                                   boolean isFromMemoryCache,
                                                   boolean isFirstResource) {
                        if (url.equals(photoUrl)) {
                            // Re-enabling touch controls
                            imageView.getController().getSettings().enableGestures();
                        }
                        return false;
                    }
                })
                .into(imageView);
    }

    @Override
    public void onBackPressed() {
        mHelper.exit();
    }

}
