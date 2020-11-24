package com.alexvasilkov.gestures.sample.ex.other.text;

import android.os.Bundle;

import com.alexvasilkov.gestures.sample.R;
import com.alexvasilkov.gestures.sample.base.BaseActivity;

/**
 * This example demonstrates creation of custom text view with text size controlled by gestures.
 * See {@link GestureTextView} class.
 */
public class CustomViewActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.custom_view_screen);
        setTitle(R.string.example_other_custom);
        getSupportActionBarNotNull().setDisplayHomeAsUpEnabled(true);

        final GestureTextView textView = findViewById(R.id.text_view);
        textView.getController().getSettings().setMaxZoom(1.5f);
    }

}
