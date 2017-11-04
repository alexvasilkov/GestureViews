package com.alexvasilkov.gestures.sample.ex.image.animation.cross;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver.OnPreDrawListener;

import com.alexvasilkov.events.Events;
import com.alexvasilkov.gestures.animation.ViewPosition;
import com.alexvasilkov.gestures.sample.R;
import com.alexvasilkov.gestures.sample.base.BaseExampleActivity;
import com.alexvasilkov.gestures.sample.ex.utils.GlideHelper;
import com.alexvasilkov.gestures.sample.ex.utils.Painting;
import com.alexvasilkov.gestures.views.GestureImageView;

/**
 * Separate activity for fullscreen image. This activity should have translucent background
 * and should skip enter and exit animations.
 */
public class FullImageActivity extends BaseExampleActivity {

    private static final String EXTRA_POSITION = "position";
    private static final String EXTRA_PAINTING_ID = "painting_id";

    private GestureImageView image;
    private View background;

    static void open(Activity from, ViewPosition position, int paintingId) {
        Intent intent = new Intent(from, FullImageActivity.class);
        intent.putExtra(EXTRA_POSITION, position.pack());
        intent.putExtra(EXTRA_PAINTING_ID, paintingId);
        from.startActivity(intent);
        from.overridePendingTransition(0, 0); // No activity animation
    }


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.image_cross_animation_to_screen);

        image = findViewById(R.id.single_image_to);
        background = findViewById(R.id.single_image_to_back);

        // Making sure image and background are invisible at first
        image.setVisibility(View.INVISIBLE);
        background.setVisibility(View.INVISIBLE);

        // Loading image. Note, that this image should already be cached in the memory to ensure
        // very fast loading. Consider using same image or its thumbnail as on prev screen.
        final int paintingId = getIntent().getIntExtra(EXTRA_PAINTING_ID, 0);
        Painting painting = Painting.list(getResources())[paintingId];
        GlideHelper.loadFull(image, painting.imageId, painting.thumbId);

        // Listening for animation state and updating our view accordingly
        image.getPositionAnimator().addPositionUpdateListener(this::applyImageAnimationState);

        // Starting enter image animation only once image is drawn for the first time to prevent
        // image blinking on activity start
        runAfterImageDraw(() -> {
            // Enter animation should only be played if activity is not created from saved state
            enterFullImage(savedInstanceState == null);

            // Hiding original image
            Events.create(CrossEvents.SHOW_IMAGE).param(false).post();
        });
    }

    @Override
    public void onBackPressed() {
        // We should leave full image mode instead of finishing this activity,
        // activity itself should only be finished in the end of the "exit" animation.
        if (!image.getPositionAnimator().isLeaving()) {
            image.getPositionAnimator().exit(true);
        }
    }

    @Override
    protected void onSettingsChanged() {
        // Applying settings from toolbar menu, see BaseExampleActivity
        getSettingsListener().onSetupGestureView(image);
        // Resetting to initial image state
        image.getController().resetState();
    }

    private void enterFullImage(boolean animate) {
        // Updating gesture image settings
        getSettingsListener().onSetupGestureView(image);

        // Playing enter animation from provided position
        ViewPosition position = ViewPosition.unpack(getIntent().getStringExtra(EXTRA_POSITION));
        image.getPositionAnimator().enter(position, animate);
    }

    private void applyImageAnimationState(float position, boolean isLeaving) {
        boolean isFinished = position == 0f && isLeaving; // Exit animation is finished

        background.setAlpha(position);
        background.setVisibility(isFinished ? View.INVISIBLE : View.VISIBLE);
        image.setVisibility(isFinished ? View.INVISIBLE : View.VISIBLE);

        if (isFinished) {
            // Showing back original image
            Events.create(CrossEvents.SHOW_IMAGE).param(true).post();

            // By default end of exit animation will return GestureImageView into
            // fullscreen state, this will make the image blink. So we need to hack this
            // behaviour and keep image in exit state until activity is finished.
            image.getController().getSettings().disableBounds();
            image.getPositionAnimator().setState(0f, false, false);

            runOnNextFrame(() -> {
                finish();
                overridePendingTransition(0, 0);
            });
        }
    }

    @Events.Subscribe(CrossEvents.POSITION_CHANGED)
    private void onImagePositionChanged(ViewPosition position) {
        // If original image position is changed we should update animator with its new position.
        if (image.getPositionAnimator().getPosition() > 0f) {
            image.getPositionAnimator().update(position);
        }
    }


    /**
     * Runs provided action after image is drawn for the first time.
     */
    private void runAfterImageDraw(final Runnable action) {
        image.getViewTreeObserver().addOnPreDrawListener(new OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                image.getViewTreeObserver().removeOnPreDrawListener(this);
                runOnNextFrame(action);
                return true;
            }
        });
        image.invalidate();
    }

    private void runOnNextFrame(Runnable action) {
        final long frameLength = 17L; // 1 frame at 60 fps
        image.postDelayed(action, frameLength);
    }

}
