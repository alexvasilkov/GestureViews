package com.alexvasilkov.gestures.sample.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.alexvasilkov.android.commons.utils.Views;
import com.alexvasilkov.gestures.sample.R;
import com.alexvasilkov.gestures.views.GestureImageView;

public class ImageCropResultActivity extends BaseActivity {

    private static Bitmap sBitmapToShow; // Bad, but works fine for demonstration purpose

    public static void show(Context context, Bitmap bitmap) {
        sBitmapToShow = bitmap;
        context.startActivity(new Intent(context, ImageCropResultActivity.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (sBitmapToShow == null) {
            finish();
            return;
        }

        setContentView(R.layout.activity_image_crop_result);

        Toolbar toolbar = Views.find(this, R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        GestureImageView imageView = Views.find(this, R.id.cropped_image);
        imageView.setImageBitmap(sBitmapToShow);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isFinishing()) sBitmapToShow = null;
    }

}
