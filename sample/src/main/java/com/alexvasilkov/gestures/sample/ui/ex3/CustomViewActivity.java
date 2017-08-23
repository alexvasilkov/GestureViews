package com.alexvasilkov.gestures.sample.ui.ex3;

import android.os.Bundle;

import com.alexvasilkov.android.commons.ui.Views;
import com.alexvasilkov.gestures.sample.R;
import com.alexvasilkov.gestures.sample.ui.base.BaseActivity;

/**
 * This example demonstrates creation of custom text view with text size controlled by gestures.
 * See {@link GestureTextView} class.
 */
public class CustomViewActivity extends BaseActivity {

    private static final float MAX_ZOOM = 1.5f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.ex3_screen);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final GestureTextView textView = Views.find(this, R.id.text_view);
        textView.getController().getSettings().setMaxZoom(MAX_ZOOM);
    }

}
