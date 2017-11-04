package com.alexvasilkov.gestures.sample.ex.image.crop;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.alexvasilkov.gestures.Settings;
import com.alexvasilkov.gestures.commons.FinderView;
import com.alexvasilkov.gestures.sample.R;
import com.alexvasilkov.gestures.sample.base.BaseExampleActivity;
import com.alexvasilkov.gestures.sample.ex.utils.GlideHelper;
import com.alexvasilkov.gestures.sample.ex.utils.Painting;
import com.alexvasilkov.gestures.views.GestureImageView;

/**
 * Simple example demonstrates image cropping with optional {@link FinderView} widget overlay.
 */
public class ImageCropActivity extends BaseExampleActivity {

    private static final int PAINTING_ID = 1;

    private GestureImageView imageView;
    private FinderView finderView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.image_crop_screen);

        // Finder area width will be 75% of available screen size and will have 16:9 aspect ratio
        final DisplayMetrics metrics = getResources().getDisplayMetrics();
        final int finderWidth = Math.min(metrics.widthPixels, metrics.heightPixels) * 3 / 4;
        final int finderHeight = finderWidth * 9 / 16;

        imageView = findViewById(R.id.image_crop_viewer);
        imageView.getController().getSettings()
                .setFitMethod(Settings.Fit.OUTSIDE)
                .setFillViewport(true)
                .setRotationEnabled(true)
                .setMovementArea(finderWidth, finderHeight);

        setDefaultSettings(imageView.getController().getSettings());

        finderView = findViewById(R.id.image_crop_finder);
        finderView.setSettings(imageView.getController().getSettings());

        final Painting painting = Painting.list(getResources())[PAINTING_ID];
        GlideHelper.loadFull(imageView, painting.imageId, painting.thumbId);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        if (finderView.getVisibility() == View.VISIBLE) {
            MenuItem crop = menu.add(Menu.NONE, R.id.menu_crop, 0, R.string.menu_crop);
            crop.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            crop.setIcon(R.drawable.ic_crop_white_24dp);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_crop) {
            // Cropping image within selected area
            Bitmap cropped = imageView.crop();
            if (cropped != null) {
                // Here you can spin off background thread (e.g. AsyncTask) and save cropped bitmap:
                // cropped.compress(Bitmap.CompressFormat.JPEG, 80, outputStream);

                // We'll just show cropped bitmap in same image view
                imageView.setImageBitmap(cropped);
                finderView.setVisibility(View.GONE);

                // Updating gesture settings to work as a regular image viewer
                Settings settings = imageView.getController().getSettings();
                settings.setMovementArea(settings.getViewportW(), settings.getViewportH());
                settings.setFitMethod(Settings.Fit.INSIDE);
                settings.setRotationEnabled(false);
                imageView.getController().resetState();

                setDefaultSettings(settings);
            }
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onSettingsChanged() {
        // Applying settings from toolbar menu, see BaseExampleActivity
        getSettingsListener().onSetupGestureView(imageView);
        // Resetting to initial image state
        imageView.getController().resetState();
    }

}
