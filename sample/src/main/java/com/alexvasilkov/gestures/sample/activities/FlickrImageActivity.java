package com.alexvasilkov.gestures.sample.activities;

import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;

import com.alexvasilkov.android.commons.utils.Views;
import com.alexvasilkov.gestures.sample.R;
import com.alexvasilkov.gestures.sample.animation.Helper;
import com.alexvasilkov.gestures.sample.utils.GlideDrawableListener;
import com.alexvasilkov.gestures.sample.utils.GlideDrawableTarget;
import com.alexvasilkov.gestures.views.GestureImageView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.googlecode.flickrjandroid.photos.Photo;

public class FlickrImageActivity extends BaseActivity {

    private ViewHolder mViews;
    private Helper mHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_flickr_image);
        setTitle(null);
        mViews = new ViewHolder(this);

        setSupportActionBar(mViews.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Setting up image view
        mViews.image.getController().getSettings()
                .setFillViewport(true)
                .setMaxZoom(3f)
                .disableGestures(); // Temporary disabling touch controls

        // Setting up animated background
        int color = getResources().getColor(R.color.window_background_dark_flickr);
        final Drawable background = new ColorDrawable(color);
        setBackground(mViews.layout, background);

        // Playing opening animation
        mHelper = new Helper(this, mViews.image);
        mHelper.setAnimationUpdateListener(new Helper.AnimationUpdateListener() {
            @Override
            public void onAnimationUpdate(float animationState) {
                background.setAlpha((int) (255 * animationState));
                mViews.toolbar.setAlpha(animationState);
                mViews.progress.setAlpha(animationState);
            }
        });
        mHelper.enter(savedInstanceState);

        // Loading image
        final Photo photo = mHelper.getItem();
        final String photoUrl = photo.getLargeSize() == null
                ? photo.getMediumUrl() : photo.getLargeUrl();

        Glide.with(this)
                .load(photoUrl)
                .thumbnail(Glide.with(this)
                        .load(photo.getThumbnailUrl())
                        .diskCacheStrategy(DiskCacheStrategy.SOURCE))
                .dontAnimate()
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .listener(new GlideDrawableListener() {
                    @Override
                    public void onSuccess(String url) {
                        if (url.equals(photoUrl)) {
                            // Re-enabling touch controls
                            mViews.image.getController().getSettings().enableGestures();
                            mViews.progress.setVisibility(View.INVISIBLE);
                        }
                    }

                    @Override
                    public void onFail(String url) {
                        mViews.progress.setVisibility(View.INVISIBLE);
                    }
                })
                .into(new GlideDrawableTarget(mViews.image) {
                    @Override
                    public void onLoadStarted(Drawable placeholder) {
                        super.onLoadStarted(placeholder);
                        mViews.progress.setVisibility(View.VISIBLE);
                    }
                });
    }

    @Override
    public void onBackPressed() {
        mHelper.exit();
    }


    @SuppressWarnings("deprecation")
    private static void setBackground(View view, Drawable drawable) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            view.setBackgroundDrawable(drawable);
        } else {
            view.setBackground(drawable);
        }
    }


    private class ViewHolder {

        final Toolbar toolbar;
        final ViewGroup layout;
        final GestureImageView image;
        final View progress;

        public ViewHolder(Activity activity) {
            toolbar = Views.find(activity, R.id.toolbar);
            image = Views.find(activity, R.id.flickr_image);
            layout = (ViewGroup) image.getParent();
            progress = Views.find(activity, R.id.flickr_progress);
        }

    }

}
