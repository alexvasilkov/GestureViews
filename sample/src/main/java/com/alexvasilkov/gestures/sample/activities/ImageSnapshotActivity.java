package com.alexvasilkov.gestures.sample.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import com.alexvasilkov.gestures.views.GestureImageView;

public class ImageSnapshotActivity extends BaseActivity {

    private static Bitmap sBitmapToShow;

    public static void show(Context context, Bitmap bitmap) {
        sBitmapToShow = bitmap;
        context.startActivity(new Intent(context, ImageSnapshotActivity.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        GestureImageView gImageView = new GestureImageView(this);
        setContentView(gImageView);
        gImageView.setImageBitmap(sBitmapToShow);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isFinishing()) sBitmapToShow = null;
    }

}
