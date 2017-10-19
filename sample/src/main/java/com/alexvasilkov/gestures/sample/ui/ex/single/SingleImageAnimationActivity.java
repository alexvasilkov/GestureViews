package com.alexvasilkov.gestures.sample.ui.ex.single;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ImageView;

import com.alexvasilkov.gestures.animation.ViewPositionAnimator.PositionUpdateListener;
import com.alexvasilkov.gestures.sample.R;
import com.alexvasilkov.gestures.sample.ui.base.BaseExampleActivity;
import com.alexvasilkov.gestures.sample.ui.ex.GlideHelper;
import com.alexvasilkov.gestures.transition.GestureTransitions;
import com.alexvasilkov.gestures.transition.ViewsTransitionAnimator;
import com.alexvasilkov.gestures.views.GestureImageView;

/**
 * This example demonstrates small image animation into a full mode.
 */
public class SingleImageAnimationActivity extends BaseExampleActivity {

    private ImageView image;
    private GestureImageView fullImage;
    private View fullBackground;
    private ViewsTransitionAnimator animator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initContentView();

        image = findViewById(R.id.single_image);
        fullImage = findViewById(R.id.single_image_full);
        fullBackground = findViewById(R.id.single_image_back);

        // Loading image
        GlideHelper.loadResource(R.drawable.painting_03, image);

        // We will expand image on click
        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(@NonNull View view) {
                openFullImage();
            }
        });

        // Initializing image animator
        animator = GestureTransitions.from(image).into(fullImage);

        animator.addPositionUpdateListener(new PositionUpdateListener() {
            @Override
            public void onPositionUpdate(float position, boolean isLeaving) {
                applyImageAnimationState(position, isLeaving);
            }
        });
    }

    /**
     * Override this method if you want to provide slightly different layout,
     */
    protected void initContentView() {
        setContentView(R.layout.single_image_screen);
    }

    @Override
    public void onBackPressed() {
        // We should leave full image mode instead of closing the screen
        if (!animator.isLeaving()) {
            animator.exit(true);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onSettingsChanged() {
        // Applying settings from toolbar menu, see BaseExampleActivity
        getSettingsListener().onSetupGestureView(fullImage);
    }

    private void openFullImage() {
        // Setting image drawable from 'from' view to 'to' to prevent flickering
        if (fullImage.getDrawable() == null) {
            fullImage.setImageDrawable(image.getDrawable());
        }

        // Updating gesture image settings
        getSettingsListener().onSetupGestureView(fullImage);
        fullImage.getController().resetState();

        animator.enterSingle(true);
    }

    private void applyImageAnimationState(float position, boolean isLeaving) {
        fullBackground.setAlpha(position);
        fullBackground.setVisibility(position == 0f && isLeaving ? View.INVISIBLE : View.VISIBLE);
        fullImage.setVisibility(position == 0f && isLeaving ? View.INVISIBLE : View.VISIBLE);
    }

}
