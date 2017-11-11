package com.alexvasilkov.gestures.sample.ex.image.crop;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.alexvasilkov.gestures.commons.CropAreaView;
import com.alexvasilkov.gestures.sample.R;
import com.alexvasilkov.gestures.sample.base.BaseActivity;
import com.alexvasilkov.gestures.sample.ex.utils.GlideHelper;
import com.alexvasilkov.gestures.sample.ex.utils.Painting;
import com.alexvasilkov.gestures.views.GestureImageView;

/**
 * This example demonstrates image cropping using {@link CropAreaView} as overlay.
 */
public class ImageCropActivity extends BaseActivity {

    private static final int PAINTING_ID = 1;
    private static final int MAX_GRID_RULES = 5;

    private GestureImageView imageView;
    private CropAreaView cropView;
    private GestureImageView resultView;

    private int gridRulesCount = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.image_crop_screen);
        getSupportActionBarNotNull().setDisplayHomeAsUpEnabled(true);

        imageView = findViewById(R.id.image_crop_viewer);

        cropView = findViewById(R.id.image_crop_area);
        cropView.setImageView(imageView);
        cropView.setRulesCount(gridRulesCount, gridRulesCount);

        resultView = findViewById(R.id.image_crop_result);

        initCropOptions();

        final Painting painting = Painting.list(getResources())[PAINTING_ID];
        GlideHelper.loadFull(imageView, painting.imageId, painting.thumbId);
    }

    @Override
    public void onBackPressed() {
        if (resultView.getVisibility() == View.VISIBLE) {
            // Return back to crop mode
            imageView.getController().resetState();

            resultView.setImageDrawable(null);
            resultView.setVisibility(View.GONE);
            invalidateOptionsMenu();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        if (resultView.getVisibility() != View.VISIBLE) {
            MenuItem crop = menu.add(Menu.NONE, R.id.menu_crop, 0, R.string.menu_crop);
            crop.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            crop.setIcon(R.drawable.ic_check_white_24dp);
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

                // We'll just show cropped bitmap on the same screen
                resultView.setImageBitmap(cropped);
                resultView.setVisibility(View.VISIBLE);
                invalidateOptionsMenu();
            }
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }


    private void initCropOptions() {
        findViewById(R.id.crop_16_9).setOnClickListener(v -> {
            cropView.setAspect(16f / 9f);
            cropView.setRounded(false);
            cropView.update(true);
        });
        findViewById(R.id.crop_1_1).setOnClickListener(v -> {
            cropView.setAspect(1f);
            cropView.setRounded(false);
            cropView.update(true);
        });
        findViewById(R.id.crop_orig).setOnClickListener(v -> {
            cropView.setAspect(CropAreaView.ORIGINAL_ASPECT);
            cropView.setRounded(false);
            cropView.update(true);
        });
        findViewById(R.id.crop_circle).setOnClickListener(v -> {
            cropView.setAspect(1f);
            cropView.setRounded(true);
            cropView.update(true);
        });

        findViewById(R.id.crop_add_rules).setOnClickListener(v -> {
            gridRulesCount = (gridRulesCount + 1) % (MAX_GRID_RULES + 1);
            cropView.setRulesCount(gridRulesCount, gridRulesCount);
        });
        findViewById(R.id.crop_reset).setOnClickListener(v ->
                imageView.getController().resetState());
    }

}
