package com.alexvasilkov.gestures.sample.ex.layout.viewer;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import com.alexvasilkov.gestures.sample.R;
import com.alexvasilkov.gestures.sample.base.BaseSettingsActivity;
import com.alexvasilkov.gestures.sample.ex.utils.GlideHelper;
import com.alexvasilkov.gestures.sample.ex.utils.Painting;
import com.alexvasilkov.gestures.views.GestureFrameLayout;

/**
 * Simple example demonstrates usage of {@link GestureFrameLayout}.
 * Basically, all you need is to wrap your layout with {@link GestureFrameLayout} and apply
 * necessary settings.
 */
public class LayoutViewerActivity extends BaseSettingsActivity {

    private static final int PAINTING_ID = 0;

    private GestureFrameLayout layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.layout_viewer_screen);

        layout = findViewById(R.id.frame_layout);

        // Initializing custom example settings
        setDefaultSettings(layout.getController().getSettings());

        // Loading sample image
        final ImageView imageView = findViewById(R.id.frame_layout_image);
        final Painting painting = Painting.list(getResources())[PAINTING_ID];
        GlideHelper.loadFull(imageView, painting.imageId, painting.thumbId);

        // Handling button click
        findViewById(R.id.frame_layout_button).setOnClickListener(view ->
                Toast.makeText(this, "Button clicked", Toast.LENGTH_SHORT).show());
    }

    @Override
    protected void onSettingsChanged() {
        // Applying settings from toolbar menu, see BaseExampleActivity
        getSettingsController().apply(layout);

        // Resetting to initial state
        layout.getController().resetState();
    }

}
