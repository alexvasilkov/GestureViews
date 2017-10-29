package com.alexvasilkov.gestures.sample.ui.base;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.alexvasilkov.gestures.sample.BuildConfig;
import com.alexvasilkov.gestures.sample.R;
import com.alexvasilkov.gestures.sample.ui.demo.DemoActivity;
import com.alexvasilkov.gestures.sample.ui.ex.ExamplesActivity;

public class StartActivity extends BaseActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_screen);

        findViewById(R.id.start_demo).setOnClickListener(this);
        findViewById(R.id.start_examples).setOnClickListener(this);

        final String version = "v" + BuildConfig.VERSION_NAME;
        final TextView versionView = findViewById(R.id.start_version);
        versionView.setText(version);
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
