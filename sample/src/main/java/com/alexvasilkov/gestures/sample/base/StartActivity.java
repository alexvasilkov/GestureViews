package com.alexvasilkov.gestures.sample.base;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.alexvasilkov.gestures.sample.BuildConfig;
import com.alexvasilkov.gestures.sample.R;
import com.alexvasilkov.gestures.sample.demo.DemoActivity;
import com.alexvasilkov.gestures.sample.ex.ExamplesActivity;

public class StartActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_screen);

        findViewById(R.id.start_demo)
                .setOnClickListener(v -> startActivity(new Intent(this, DemoActivity.class)));
        findViewById(R.id.start_examples)
                .setOnClickListener(v -> startActivity(new Intent(this, ExamplesActivity.class)));

        final String version = "v" + BuildConfig.VERSION_NAME;
        final TextView versionView = findViewById(R.id.start_version);
        versionView.setText(version);
    }

}
