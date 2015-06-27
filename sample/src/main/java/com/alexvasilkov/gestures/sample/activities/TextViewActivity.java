package com.alexvasilkov.gestures.sample.activities;

import android.os.Bundle;

import com.alexvasilkov.android.commons.utils.Views;
import com.alexvasilkov.gestures.sample.R;
import com.alexvasilkov.gestures.sample.views.GestureTextView;

public class TextViewActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_text_view);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        GestureTextView textView = Views.find(this, R.id.text_view);
        textView.getController().getSettings().setMaxZoom(1.5f);
    }

}
