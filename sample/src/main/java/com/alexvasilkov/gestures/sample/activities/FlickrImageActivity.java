package com.alexvasilkov.gestures.sample.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.alexvasilkov.android.commons.utils.Views;
import com.alexvasilkov.gestures.animation.ViewPosition;
import com.alexvasilkov.gestures.animation.ViewPositionAnimator;
import com.alexvasilkov.gestures.sample.R;
import com.alexvasilkov.gestures.sample.utils.GlideHelper;
import com.alexvasilkov.gestures.sample.utils.ViewsCompat;
import com.alexvasilkov.gestures.views.GestureImageView;
import com.googlecode.flickrjandroid.photos.Photo;

public class FlickrImageActivity extends BaseActivity {

    private static final String EXTRA_FROM_POSITION = "EXTRA_FROM_POSITION";
    private static final String EXTRA_PHOTO = "EXTRA_PHOTO";

    private ViewHolder mViews;
    private ViewPositionAnimator mPositionAnimator;

    private Drawable mBackground;

    public static void open(Activity activity, Photo photo, ImageView from) {
        Intent intent = new Intent(activity, FlickrImageActivity.class);
        intent.putExtra(EXTRA_FROM_POSITION, ViewPosition.from(from).pack());
        intent.putExtra(EXTRA_PHOTO, photo);
        activity.startActivity(intent);
        activity.overridePendingTransition(0, 0);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String fromPosStr = getIntent().getStringExtra(EXTRA_FROM_POSITION);
        final ViewPosition fromPos = fromPosStr == null ? null : ViewPosition.unpack(fromPosStr);
        final Photo photo = (Photo) getIntent().getSerializableExtra(EXTRA_PHOTO);

        if (fromPos == null || photo == null)
            throw new RuntimeException("Photo and / or view position was not specified");

        setContentView(R.layout.activity_flickr_image);
        setTitle(null);
        mViews = new ViewHolder(this);

        setSupportActionBar(mViews.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Setting up image view
        mViews.image.getController().getSettings().setFillViewport(true).setMaxZoom(3f);

        // Setting up animated background
        int color = getResources().getColor(R.color.window_background_dark_flickr);
        mBackground = new ColorDrawable(color);
        ViewsCompat.setBackground(mViews.layout, mBackground);

        // Playing opening animation
        mPositionAnimator = new ViewPositionAnimator();
        mPositionAnimator.reset(fromPos, mViews.image);
        mPositionAnimator.setOnPositionChangeListener(new ViewPositionAnimator.OnPositionChangeListener() {
            @Override
            public void onPositionChanged(float state, boolean isFinishing) {
                onPhotoAnimationState(state, isFinishing);
            }
        });

        mPositionAnimator.enter(savedInstanceState == null);

        // Temporary disabling touch controls
        mViews.image.getController().getSettings().disableGestures();
        // Loading image
        GlideHelper.loadFlickrFull(photo, mViews.image, mViews.progress,
                new GlideHelper.OnImageLoadedListener() {
                    @Override
                    public void onImageLoaded() {
                        // Re-enabling touch controls
                        mViews.image.getController().getSettings().enableGestures();
                    }
                });
    }

    @Override
    public void onBackPressed() {
        mPositionAnimator.exit(true);
    }

    private void onPhotoAnimationState(float state, boolean isFinishing) {
        mBackground.setAlpha((int) (255 * state));
        mViews.toolbar.setAlpha(state);
        mViews.progress.setAlpha(state);

        if (state == 0f && isFinishing) {
            finish();
            overridePendingTransition(0, 0);
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
