package com.alexvasilkov.gestures.sample.activities;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import com.alexvasilkov.android.commons.utils.Views;
import com.alexvasilkov.gestures.Settings;
import com.alexvasilkov.gestures.sample.R;
import com.alexvasilkov.gestures.sample.items.Painting;
import com.alexvasilkov.gestures.sample.utils.PicassoHelper;
import com.alexvasilkov.gestures.widgets.GestureImageView;

public class ImageCroppingActivity extends BaseActivity {

    private GestureImageView mImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_rotation);

        int frameW = getResources().getDimensionPixelSize(R.dimen.image_frame_width);
        int frameH = getResources().getDimensionPixelSize(R.dimen.image_frame_height);

        mImageView = Views.find(this, R.id.painting_image);
        mImageView.getController().getSettings()
                .setFitMethod(Settings.Fit.OUTSIDE)
                .setFillViewport(true)
                .setMovementArea(frameW, frameH)
                .setRotationEnabled(true);

        Painting painting = Painting.getAllPaintings(getResources())[0];
        PicassoHelper.get(this).load(painting.getImageId()).into(mImageView);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.NONE, R.string.button_crop, 0, R.string.button_crop)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.string.button_crop:
                mImageView.getSnapshot(new GestureImageView.OnSnapshotLoadedListener() {
                    @Override
                    public void onSnapshotLoaded(Bitmap bitmap) {
                        ImageSnapshotActivity.show(ImageCroppingActivity.this, bitmap);
                    }
                });
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
