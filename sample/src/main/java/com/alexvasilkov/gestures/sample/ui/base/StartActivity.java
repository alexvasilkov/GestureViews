package com.alexvasilkov.gestures.sample.ui.base;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.alexvasilkov.android.commons.ui.Views;
import com.alexvasilkov.gestures.sample.BuildConfig;
import com.alexvasilkov.gestures.sample.R;
import com.alexvasilkov.gestures.sample.ui.demo.DemoActivity;
import com.alexvasilkov.gestures.sample.ui.ex.ExamplesActivity;

public class StartActivity extends BaseActivity implements View.OnClickListener {

    @SuppressLint("SetTextI18n") // It's ok for version name
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_screen);

        Views.find(this, R.id.start_demo).setOnClickListener(this);
        Views.find(this, R.id.start_examples).setOnClickListener(this);

        Views.<TextView>find(this, R.id.start_version).setText("v" + BuildConfig.VERSION_NAME);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.start_demo:
                startActivity(new Intent(this, DemoActivity.class));
                break;
            case R.id.start_examples:
                startActivity(new Intent(this, ExamplesActivity.class));
                break;
            default:
        }
    }

}
