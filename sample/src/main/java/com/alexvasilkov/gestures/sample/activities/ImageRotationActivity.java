package com.alexvasilkov.gestures.sample.activities;

import android.os.Bundle;
import com.alexvasilkov.fluffycommons.utils.Views;
import com.alexvasilkov.gestures.sample.R;
import com.alexvasilkov.gestures.sample.items.Painting;
import com.alexvasilkov.gestures.widgets.GestureImageView;
import com.squareup.picasso.Picasso;

public class ImageRotationActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_rotation);

        GestureImageView gImageView = Views.find(this, R.id.painting_image);
        gImageView.getController().getSettings().setRotationEnabled(true);

        Painting painting = Painting.getAllPaintings(getResources())[0];
        Picasso.with(this).load(painting.getImageId()).into(gImageView);
    }

}
