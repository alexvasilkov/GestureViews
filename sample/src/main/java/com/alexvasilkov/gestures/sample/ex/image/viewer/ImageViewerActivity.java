package com.alexvasilkov.gestures.sample.ex.image.viewer;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

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

        setContentView(R.layout.image_viewer_screen);

        // Initializing image viewer
        imageViewer = findViewById(R.id.image_viewer);

        // Applying custom settings
        imageViewer.getController().getSettings()
                .setMaxZoom(6f)
                .setDoubleTapZoom(3f);

        imageViewer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ImageViewerActivity.this, "Single click", Toast.LENGTH_SHORT).show();
            }
        });

        imageViewer.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Toast.makeText(ImageViewerActivity.this, "Long click", Toast.LENGTH_SHORT).show();
                return true;
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
