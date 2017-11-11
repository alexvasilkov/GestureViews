package com.alexvasilkov.gestures.sample.ex.image.viewer;

import android.os.Bundle;
import android.widget.Toast;

import com.alexvasilkov.gestures.sample.R;
import com.alexvasilkov.gestures.sample.base.BaseSettingsActivity;
import com.alexvasilkov.gestures.sample.ex.utils.GlideHelper;
import com.alexvasilkov.gestures.sample.ex.utils.Painting;
import com.alexvasilkov.gestures.views.GestureImageView;

/**
 * Simple example demonstrates general usage of {@link GestureImageView}.
 */
public class ImageViewerActivity extends BaseSettingsActivity {

    private static final int PAINTING_ID = 1;

    private GestureImageView imageViewer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.image_viewer_screen);

        // Initializing image viewer
        imageViewer = findViewById(R.id.image_viewer);

        // Applying custom settings (note, that all settings can be also set in XML)
        imageViewer.getController().getSettings()
                .setMaxZoom(6f)
                .setDoubleTapZoom(3f);

        imageViewer.setOnClickListener(view -> showToast("Single click"));

        imageViewer.setOnLongClickListener(view -> {
            showToast("Long click");
            return true;
        });

        final Painting painting = Painting.list(getResources())[PAINTING_ID];
        GlideHelper.loadFull(imageViewer, painting.imageId, painting.thumbId);
    }

    private void showToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onSettingsChanged() {
        // Applying settings from toolbar menu, see BaseExampleActivity
        getSettingsController().apply(imageViewer);

        // Resetting to initial image state
        imageViewer.getController().resetState();
    }

}
