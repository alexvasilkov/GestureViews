package com.alexvasilkov.gestures.sample.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.alexvasilkov.android.commons.ui.Views;
import com.alexvasilkov.gestures.sample.R;
import com.alexvasilkov.gestures.views.GestureImageView;

public class PhotoCropResultActivity extends BaseActivity {

    private static Bitmap bitmapToShow; // Bad, but works fine for demonstration purpose

    public static void show(Context context, Bitmap bitmap) {
        bitmapToShow = bitmap;
        context.startActivity(new Intent(context, PhotoCropResultActivity.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (bitmapToShow == null) {
            finish();
            return;
        }

        setContentView(R.layout.activity_photo_crop_result);

        Toolbar toolbar = Views.find(this, R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        GestureImageView imageView = Views.find(this, R.id.cropped_image);
        imageView.setImageBitmap(bitmapToShow);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isFinishing()) {
            bitmapToShow = null;
        }
    }

}
