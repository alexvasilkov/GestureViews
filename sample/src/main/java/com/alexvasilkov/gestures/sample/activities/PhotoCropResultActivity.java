package com.alexvasilkov.gestures.sample.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.alexvasilkov.android.commons.utils.Views;
import com.alexvasilkov.gestures.Settings;
import com.alexvasilkov.gestures.sample.R;
import com.alexvasilkov.gestures.views.GestureImageView;

public class PhotoCropResultActivity extends BaseActivity {

    private static Bitmap sBitmapToShow; // Bad, but works fine for demonstration purpose

    public static void show(Context context, Bitmap bitmap) {
        sBitmapToShow = bitmap;
        context.startActivity(new Intent(context, PhotoCropResultActivity.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (sBitmapToShow == null) {
            finish();
            return;
        }

        setContentView(R.layout.activity_photo_crop_result);

        Toolbar toolbar = Views.find(this, R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        int frameW = getResources().getDimensionPixelSize(R.dimen.image_frame_width);
        int frameH = getResources().getDimensionPixelSize(R.dimen.image_frame_height);

        GestureImageView imageView = Views.find(this, R.id.cropped_image);
        imageView.getController().getSettings()
                .setFitMethod(Settings.Fit.OUTSIDE)
                .setFillViewport(true)
                .setMovementArea(frameW, frameH);

        imageView.setImageBitmap(sBitmapToShow);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isFinishing()) sBitmapToShow = null;
    }

}
