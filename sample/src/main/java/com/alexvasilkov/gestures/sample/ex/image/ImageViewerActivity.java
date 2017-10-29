package com.alexvasilkov.gestures.sample.ex.image;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.MotionEvent;
import android.widget.Toast;

import com.alexvasilkov.gestures.GestureController.SimpleOnGestureListener;
import com.alexvasilkov.gestures.sample.R;
import com.alexvasilkov.gestures.sample.base.BaseExampleActivity;
import com.alexvasilkov.gestures.views.GestureImageView;

/**
 * Simple example demonstrates general usage of {@link GestureImageView}.
 */
public class ImageViewerActivity extends BaseExampleActivity {

    private GestureImageView imageViewer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.image_screen);

        // Initializing image viewer
        imageViewer = findViewById(R.id.image_viewer);

        // Applying custom settings
        imageViewer.getController().getSettings()
                .setMaxZoom(6f)
                .setDoubleTapZoom(3f);

        // Setting basic gestures listener
        imageViewer.getController().setOnGesturesListener(new SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapConfirmed(@NonNull MotionEvent event) {
                Toast.makeText(ImageViewerActivity.this, "Single tap", Toast.LENGTH_SHORT).show();
                return false;
            }
        });
    }

    @Override
    protected void onSettingsChanged() {
        // Applying settings from toolbar menu, see BaseExampleActivity
        getSettingsListener().onSetupGestureView(imageViewer);

        // Resetting to initial image state
        imageViewer.getController().resetState();
    }

}
