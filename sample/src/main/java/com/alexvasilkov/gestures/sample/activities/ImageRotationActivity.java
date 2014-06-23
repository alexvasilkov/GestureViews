package com.alexvasilkov.gestures.sample.activities;

import android.os.Bundle;
import com.alexvasilkov.android.commons.utils.Views;
import com.alexvasilkov.gestures.sample.R;
import com.alexvasilkov.gestures.sample.items.Painting;
import com.alexvasilkov.gestures.sample.utils.PicassoHelper;
import com.alexvasilkov.gestures.widgets.GestureImageView;

public class ImageRotationActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_rotation);

        GestureImageView gImageView = Views.find(this, R.id.painting_image);
        gImageView.getController().getSettings().setRotationEnabled(true).setOverscrollDistance(this, 32, 32);

        Painting painting = Painting.getAllPaintings(getResources())[0];
        PicassoHelper.get(this).load(painting.getImageId()).into(gImageView);
    }

}
