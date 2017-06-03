package com.alexvasilkov.gestures.sample.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;

import com.alexvasilkov.android.commons.state.InstanceState;
import com.alexvasilkov.android.commons.ui.Views;
import com.alexvasilkov.gestures.GestureController;
import com.alexvasilkov.gestures.Settings;
import com.alexvasilkov.gestures.State;
import com.alexvasilkov.gestures.commons.FinderView;
import com.alexvasilkov.gestures.sample.R;
import com.alexvasilkov.gestures.sample.utils.glide.GlideHelper;
import com.alexvasilkov.gestures.views.GestureImageView;
import com.googlecode.flickrjandroid.photos.Photo;

public class PhotoCropActivity extends BaseActivity {

    private static final String EXTRA_PHOTO = "EXTRA_PHOTO";

    private GestureImageView imageView;
    private FinderView finderView;

    @InstanceState
    private FinderShape finderShape = FinderShape.RECT;

    public static void show(Context context, Photo photo) {
        Intent intent = new Intent(context, PhotoCropActivity.class);
        intent.putExtra(EXTRA_PHOTO, photo);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_photo_crop);

        Toolbar toolbar = Views.find(this, R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        imageView = Views.find(this, R.id.cropping_image);
        imageView.getController().getSettings()
                .setFitMethod(Settings.Fit.OUTSIDE)
                .setFillViewport(true)
                .setRotationEnabled(true);

        finderView = Views.find(this, R.id.cropping_finder);
        finderView.setSettings(imageView.getController().getSettings());

        Photo photo = (Photo) getIntent().getSerializableExtra(EXTRA_PHOTO);
        GlideHelper.loadFlickrFull(photo, imageView, null);

        applyFinderShape(false);
    }

    private void applyFinderShape(boolean animate) {
        finderView.setRounded(finderShape == FinderShape.CIRCLE);

        // Computing cropping area size
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int finderWidth = Math.min(metrics.widthPixels, metrics.heightPixels) * 3 / 4;
        int finderHeight = finderShape == FinderShape.RECT ? finderWidth * 9 / 16 : finderWidth;

        GestureController controller = imageView.getController();

        // Setting cropping area
        controller.getSettings().setMovementArea(finderWidth, finderHeight);

        if (animate) {
            // Animating to zoomed out state, keeping image in bounds
            int pivotX = controller.getSettings().getViewportW() / 2;
            int pivotY = controller.getSettings().getViewportH() / 2;
            State end = controller.getState().copy();
            end.zoomTo(0.001f, pivotX, pivotY); // Zooming out to initial state
            controller.setPivot(pivotX, pivotY);
            controller.animateStateTo(end);
        } else {
            controller.updateState();
        }

        // Updating cropping area changes
        finderView.update(animate);
    }

    private void setFinderShape(FinderShape shape) {
        finderShape = shape;
        applyFinderShape(true);
        supportInvalidateOptionsMenu();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem shape;
        if (finderShape == FinderShape.RECT) {
            shape = menu.add(Menu.NONE, R.id.menu_crop_square, 0, R.string.menu_crop_square);
            shape.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            shape.setIcon(R.drawable.ic_crop_square_white_24dp);
        } else if (finderShape == FinderShape.SQUARE) {
            shape = menu.add(Menu.NONE, R.id.menu_crop_circle, 0, R.string.menu_crop_circle);
            shape.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            shape.setIcon(R.drawable.ic_radio_button_unchecked_white_24dp);
        } else {
            shape = menu.add(Menu.NONE, R.id.menu_crop_rect, 0, R.string.menu_crop_rect);
            shape.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            shape.setIcon(R.drawable.ic_crop_16_9_white_24dp);
        }

        MenuItem crop = menu.add(Menu.NONE, R.id.menu_crop, 0, R.string.button_crop);
        crop.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        crop.setIcon(R.drawable.ic_done_white_24dp);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_crop:
                // Cropping image within selected area
                Bitmap cropped = imageView.crop();
                if (cropped != null) {
                    PhotoCropResultActivity.show(PhotoCropActivity.this, cropped);
                    finish();
                }
                return true;
            case R.id.menu_crop_rect:
                setFinderShape(FinderShape.RECT);
                return true;
            case R.id.menu_crop_square:
                setFinderShape(FinderShape.SQUARE);
                return true;
            case R.id.menu_crop_circle:
                setFinderShape(FinderShape.CIRCLE);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private enum FinderShape {
        RECT, CIRCLE, SQUARE
    }

}
