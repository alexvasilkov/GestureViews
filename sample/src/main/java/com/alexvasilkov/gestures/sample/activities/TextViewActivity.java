package com.alexvasilkov.gestures.sample.activities;

import android.os.Bundle;

import com.alexvasilkov.gestures.sample.R;

public class TextViewActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_text_view);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

}
