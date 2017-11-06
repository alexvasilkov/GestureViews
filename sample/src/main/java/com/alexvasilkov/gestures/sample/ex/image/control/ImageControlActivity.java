package com.alexvasilkov.gestures.sample.ex.image.control;

import android.graphics.PointF;
import android.os.Bundle;
import android.widget.CheckBox;

import com.alexvasilkov.gestures.GestureController;
import com.alexvasilkov.gestures.State;
import com.alexvasilkov.gestures.sample.R;
import com.alexvasilkov.gestures.sample.base.BaseExampleActivity;
import com.alexvasilkov.gestures.sample.ex.utils.GlideHelper;
import com.alexvasilkov.gestures.sample.ex.utils.Painting;
import com.alexvasilkov.gestures.views.GestureImageView;

public class ImageControlActivity extends BaseExampleActivity {

    private static final int PAINTING_ID = 1;

    private GestureImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.image_control_screen);

        imageView = findViewById(R.id.image_control_viewer);
        imageView.getController().getSettings()
                .setMaxZoom(6f)
                .setDoubleTapZoom(3f);

        initControlOptions();

        final Painting painting = Painting.list(getResources())[PAINTING_ID];
        GlideHelper.loadFull(imageView, painting.imageId, painting.thumbId);
    }

    @Override
    protected void onSettingsChanged() {
        // Applying settings from toolbar menu, see BaseExampleActivity
        getSettingsListener().onSetupGestureView(imageView);

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
        final PointF pivot = getPivot();

        // Zoom in or out the image
        state.zoomBy(zoomIn ? 1.333f : 0.75f, pivot.x, pivot.y);

        if (animate) {
            controller.setPivot(pivot.x, pivot.y);
            controller.animateStateTo(state);
        } else {
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
        final PointF pivot = getPivot();

        // Rotating to closest next 90 degree ccw
        float rotation = Math.round(state.getRotation()) % 90f == 0f ?
                state.getRotation() - 90f : (float) Math.floor(state.getRotation() / 90f) * 90f;
        state.rotateTo(rotation, pivot.x, pivot.y);

        if (animate) {
            controller.setPivot(pivot.x, pivot.y);
            controller.animateStateTo(state);
        } else {
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
            final PointF pivot = getPivot();

            // Restoring initial image zoom and rotation
            final float minZoom = controller.getStateController().getMinZoom(state);
            state.zoomTo(minZoom, pivot.x, pivot.y);
            state.rotateTo(0f, pivot.x, pivot.y);

            controller.setPivot(pivot.x, pivot.y);
            controller.animateStateTo(state);
        } else {
            controller.resetState();
        }
    }

    private PointF getPivot() {
        PointF pivot = new PointF();
        pivot.x = 0.5f * imageView.getController().getSettings().getViewportW();
        pivot.y = 0.5f * imageView.getController().getSettings().getViewportH();
        return pivot;
    }

}
