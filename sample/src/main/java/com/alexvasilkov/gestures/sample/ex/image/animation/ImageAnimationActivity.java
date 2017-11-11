package com.alexvasilkov.gestures.sample.ex.image.animation;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.alexvasilkov.gestures.sample.R;
import com.alexvasilkov.gestures.sample.base.BaseExampleActivity;
import com.alexvasilkov.gestures.sample.ex.utils.GlideHelper;
import com.alexvasilkov.gestures.sample.ex.utils.Painting;
import com.alexvasilkov.gestures.transition.GestureTransitions;
import com.alexvasilkov.gestures.transition.ViewsTransitionAnimator;
import com.alexvasilkov.gestures.views.GestureImageView;

/**
 * This example demonstrates image animation from small mode into a full one.
 */
public class ImageAnimationActivity extends BaseExampleActivity {

    private static final int PAINTING_ID = 2;

    private ImageView image;
    private GestureImageView fullImage;
    private View fullBackground;
    private ViewsTransitionAnimator animator;

    private Painting painting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initContentView();

        image = findViewById(R.id.single_image);
        fullImage = findViewById(R.id.single_image_full);
        fullBackground = findViewById(R.id.single_image_back);

        // Loading image
        painting = Painting.list(getResources())[PAINTING_ID];
        GlideHelper.loadThumb(image, painting.thumbId);

        // We will expand image on click
        image.setOnClickListener(view -> openFullImage());

        // Initializing image animator
        animator = GestureTransitions.from(image).into(fullImage);
        animator.addPositionUpdateListener(this::applyImageAnimationState);
    }

    /**
     * Override this method if you want to provide slightly different layout,
     */
    protected void initContentView() {
        setContentView(R.layout.image_animation_screen);
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
        getSettingsController().apply(fullImage);
        // Resetting to initial image state
        fullImage.getController().resetState();
    }

    private void openFullImage() {
        // Setting image drawable from 'from' view to 'to' to prevent flickering
        if (fullImage.getDrawable() == null) {
            fullImage.setImageDrawable(image.getDrawable());
        }

        // Updating gesture image settings
        getSettingsController().apply(fullImage);
        // Resetting to initial image state
        fullImage.getController().resetState();

        animator.enterSingle(true);
        GlideHelper.loadFull(fullImage, painting.imageId, painting.thumbId);
    }

    private void applyImageAnimationState(float position, boolean isLeaving) {
        fullBackground.setAlpha(position);
        fullBackground.setVisibility(position == 0f && isLeaving ? View.INVISIBLE : View.VISIBLE);
        fullImage.setVisibility(position == 0f && isLeaving ? View.INVISIBLE : View.VISIBLE);
    }

}
