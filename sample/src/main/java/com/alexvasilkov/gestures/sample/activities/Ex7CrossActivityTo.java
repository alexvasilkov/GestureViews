package com.alexvasilkov.gestures.sample.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.view.ViewTreeObserver.OnPreDrawListener;

import com.alexvasilkov.android.commons.ui.Views;
import com.alexvasilkov.events.Events;
import com.alexvasilkov.gestures.animation.ViewPosition;
import com.alexvasilkov.gestures.animation.ViewPositionAnimator.PositionUpdateListener;
import com.alexvasilkov.gestures.sample.R;
import com.alexvasilkov.gestures.sample.utils.glide.GlideHelper;
import com.alexvasilkov.gestures.views.GestureImageView;

/**
 * Separate activity for fullscreen image. This activity should have translucent background
 * and should skip enter and exit animations.
 */
public class Ex7CrossActivityTo extends BaseActivity {

    private static final String EXTRA_POSITION = "position";
    private static final String EXTRA_IMAGE_ID = "image_id";

    public static void open(Activity from, ViewPosition position, @DrawableRes int imageId) {
        Intent intent = new Intent(from, Ex7CrossActivityTo.class);
        intent.putExtra(EXTRA_POSITION, position.pack());
        intent.putExtra(EXTRA_IMAGE_ID, imageId);
        from.startActivity(intent);
        from.overridePendingTransition(0, 0); // No activity animation
    }

    private GestureImageView image;
    private boolean hideOrigImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initializing views
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.activity_ex7_to);
        image = Views.find(this, R.id.cross_to);

        // Loading image
        int imageId = getIntent().getIntExtra(EXTRA_IMAGE_ID, 0);
        GlideHelper.loadResource(imageId, image);

        // Ensuring that original image is hidden as soon as image is loaded and drawn
        image.getViewTreeObserver().addOnPreDrawListener(new OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                if (hideOrigImage) {
                    image.getViewTreeObserver().removeOnPreDrawListener(this);
                    // Asking previous activity to hide original image
                    Events.create(Ex7CrossActivityFrom.EVENT_SHOW_IMAGE).param(false).post();
                } else if (image.getDrawable() != null) {
                    // Requesting hiding original image after first drawing
                    hideOrigImage = true;
                }
                return true;
            }
        });

        // Listening for end of exit animation
        image.getPositionAnimator().addPositionUpdateListener(new PositionUpdateListener() {
            @Override
            public void onPositionUpdate(float position, boolean isLeaving) {
                if (position == 0f && isLeaving) { // Exit finished
                    // Asking previous activity to show back original image
                    Events.create(Ex7CrossActivityFrom.EVENT_SHOW_IMAGE).param(true).post();

                    // By default end of exit animation will return GestureImageView into
                    // fullscreen state, this will make the image blink. So we need to hack this
                    // behaviour and keep image in exit state until activity is finished.
                    image.getController().getSettings().disableBounds();
                    image.getPositionAnimator().setState(0f, false, false);

                    // Finishing activity
                    finish();
                    overridePendingTransition(0, 0); // No activity animation
                }
            }
        });

        // Playing enter animation from provided position
        ViewPosition position = ViewPosition.unpack(getIntent().getStringExtra(EXTRA_POSITION));
        boolean animate = savedInstanceState == null; // No animation when restoring activity
        image.getPositionAnimator().enter(position, animate);
    }

    @Override
    public void onBackPressed() {
        if (!image.getPositionAnimator().isLeaving()) {
            image.getPositionAnimator().exit(true);
        }
    }

    @Events.Subscribe(Ex7CrossActivityFrom.EVENT_POSITION_CHANGED)
    private void onViewPositionChanged(ViewPosition position) {
        // If original image position is changed we should update animator with new position.
        image.getPositionAnimator().update(position);
    }

}
