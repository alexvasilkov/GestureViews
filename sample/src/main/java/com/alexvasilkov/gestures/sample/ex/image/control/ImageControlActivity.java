package com.alexvasilkov.gestures.sample.ex.image.control;

import android.graphics.Point;
import android.os.Bundle;
import android.widget.CheckBox;

import com.alexvasilkov.gestures.GestureController;
import com.alexvasilkov.gestures.State;
import com.alexvasilkov.gestures.sample.R;
import com.alexvasilkov.gestures.sample.base.BaseSettingsActivity;
import com.alexvasilkov.gestures.sample.ex.utils.GlideHelper;
import com.alexvasilkov.gestures.sample.ex.utils.Painting;
import com.alexvasilkov.gestures.utils.GravityUtils;
import com.alexvasilkov.gestures.views.GestureImageView;
import com.alexvasilkov.gestures.views.interfaces.GestureView;

/**
 * This example demonstrates how to programmatically control image state with and without animation.
 * <p>
 * You can basically get image's {@link State} with
 * {@link GestureView#getController() getController()}
 * .{@link GestureController#getState() getState()}, change it using any of state's methods
 * and then either immediately apply the changes with {@link GestureController#updateState()} method
 * or animate them with {@link GestureController#animateStateTo(State)}.<br>
 * Note, that in last case you should apply all the state manipulations to a copy of current state
 * (can be made with {@link State#copy()}).
 */
public class ImageControlActivity extends BaseSettingsActivity {

    private static final int PAINTING_ID = 1;

    private GestureImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.image_control_screen);
        setTitle(R.string.example_image_state_control);

        imageView = findViewById(R.id.image_control_viewer);

        initControlOptions();

        // Loading sample image
        final Painting painting = Painting.list(getResources())[PAINTING_ID];
        GlideHelper.loadFull(imageView, painting.imageId, painting.thumbId);
    }

    @Override
    protected void onSettingsChanged() {
        // Applying settings from toolbar menu, see BaseExampleActivity
        getSettingsController().apply(imageView);

        // Resetting to initial image state
        imageView.getController().resetState();
    }

    private void initControlOptions() {
        final CheckBox animate = findViewById(R.id.control_animate);

        findViewById(R.id.control_zoom_in).setOnClickListener(
                v -> zoomImage(true, animate.isChecked()));
        findViewById(R.id.control_zoom_out).setOnClickListener(
                v -> zoomImage(false, animate.isChecked()));
        findViewById(R.id.control_rotate).setOnClickListener(v -> rotateImage(animate.isChecked()));
        findViewById(R.id.control_reset).setOnClickListener(v -> resetImage(animate.isChecked()));
    }

    private void zoomImage(boolean zoomIn, boolean animate) {
        final GestureController controller = imageView.getController();

        if (controller.isAnimating()) {
            return; // Waiting for animation end
        }

        final State state = controller.getState().copy();
        final Point pivot = getPivot();

        // Zooming the image in or out
        state.zoomBy(zoomIn ? 1.333f : 0.75f, pivot.x, pivot.y);

        if (animate) {
            // Animating state changes. Do not forget to make a state's copy prior to any changes.
            controller.animateStateTo(state);
        } else {
            // Immediately applying state changes
            controller.getState().set(state);
            controller.updateState();
        }
    }

    private void rotateImage(boolean animate) {
        final GestureController controller = imageView.getController();

        if (controller.isAnimating()) {
            return; // Waiting for animation end
        }

        final State state = controller.getState().copy();
        final Point pivot = getPivot();

        // Rotating to closest next 90 degree ccw
        float rotation = Math.round(state.getRotation()) % 90f == 0f
                ? state.getRotation() - 90f : (float) Math.floor(state.getRotation() / 90f) * 90f;
        state.rotateTo(rotation, pivot.x, pivot.y);

        if (animate) {
            // Animating state changes. Do not forget to make a state's copy prior to any changes.
            controller.animateStateTo(state);
        } else {
            // Immediately applying state changes
            controller.getState().set(state);
            controller.updateState();
        }
    }

    private void resetImage(boolean animate) {
        final GestureController controller = imageView.getController();

        if (controller.isAnimating()) {
            return; // Waiting for animation end
        }

        if (animate) {
            final State state = controller.getState().copy();
            final Point pivot = getPivot();

            // Restoring initial image zoom and rotation
            final float minZoom = controller.getStateController().getMinZoom(state);
            state.zoomTo(minZoom, pivot.x, pivot.y);
            state.rotateTo(0f, pivot.x, pivot.y);

            // Animating state changes. Do not forget to make a state's copy prior to any changes.
            controller.animateStateTo(state);
        } else {
            // Immediately resetting the state
            controller.resetState();
        }
    }

    private Point getPivot() {
        // Default pivot point is a view center
        Point pivot = new Point();
        GravityUtils.getDefaultPivot(imageView.getController().getSettings(), pivot);
        return pivot;
    }

}
