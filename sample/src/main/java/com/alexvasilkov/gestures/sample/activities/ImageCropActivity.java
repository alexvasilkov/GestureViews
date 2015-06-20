package com.alexvasilkov.gestures.sample.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.alexvasilkov.android.commons.utils.Views;
import com.alexvasilkov.gestures.Settings;
import com.alexvasilkov.gestures.sample.R;
import com.alexvasilkov.gestures.sample.items.Painting;
import com.alexvasilkov.gestures.views.GestureImageView;
import com.bumptech.glide.Glide;

public class ImageCropActivity extends BaseActivity {

    private static final String EXTRA_POS = "EXTRA_POS";

    private GestureImageView mImageView;

    public static void show(Context context, int pos) {
        Intent intent = new Intent(context, ImageCropActivity.class);
        intent.putExtra(EXTRA_POS, pos);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_image_crop);

        Toolbar toolbar = Views.find(this, R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        int frameW = getResources().getDimensionPixelSize(R.dimen.image_frame_width);
        int frameH = getResources().getDimensionPixelSize(R.dimen.image_frame_height);

        mImageView = Views.find(this, R.id.painting_image);
        mImageView.getController().getSettings()
                .setFitMethod(Settings.Fit.OUTSIDE)
                .setFillViewport(true)
                .setMovementArea(frameW, frameH)
                .setRotationEnabled(true);

        int pos = getIntent().getIntExtra(EXTRA_POS, 0);
        Painting painting = Painting.getAllPaintings(getResources())[pos];
        Glide.with(this).load(painting.getImageId()).into(mImageView);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem crop = menu.add(Menu.NONE, R.id.menu_crop, 0, R.string.button_crop);
        crop.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        crop.setIcon(R.drawable.ic_done_white_24dp);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_crop:
                mImageView.getSnapshot(new GestureImageView.OnSnapshotLoadedListener() {
                    @Override
                    public void onSnapshotLoaded(Bitmap bitmap) {
                        finish();
                        ImageCropResultActivity.show(ImageCropActivity.this, bitmap);
                    }
                });
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
