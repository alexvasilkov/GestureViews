package com.alexvasilkov.gestures.sample.activities;

import android.os.Bundle;

import com.alexvasilkov.android.commons.ui.Views;
import com.alexvasilkov.gestures.sample.R;
import com.alexvasilkov.gestures.sample.views.GestureTextView;

public class Ex3CustomViewSampleActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_ex3_custom_view);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        GestureTextView textView = Views.find(this, R.id.text_view);
        textView.getController().getSettings().setMaxZoom(1.5f);
    }

}
