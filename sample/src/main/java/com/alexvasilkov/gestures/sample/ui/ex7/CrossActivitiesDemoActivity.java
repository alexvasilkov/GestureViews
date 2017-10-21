package com.alexvasilkov.gestures.sample.ui.ex7;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.ImageView;

import com.alexvasilkov.android.commons.ui.Views;
import com.alexvasilkov.events.Events;
import com.alexvasilkov.gestures.animation.ViewPosition;
import com.alexvasilkov.gestures.sample.R;
import com.alexvasilkov.gestures.sample.ui.base.BaseActivity;
import com.alexvasilkov.gestures.sample.ui.ex.GlideHelper;

/**
 * This example demonstrates image animation that crosses activities bounds.
 */
public class CrossActivitiesDemoActivity extends BaseActivity {

    public static final String EVENT_SHOW_IMAGE = "show_image";
    public static final String EVENT_POSITION_CHANGED = "position_changed";

    private static final int IMAGE_ID = R.drawable.painting_01;

    private ImageView image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initializing views
        setContentView(R.layout.ex7_screen);
        getSupportActionBarNotNull().setDisplayHomeAsUpEnabled(true);
        image = Views.find(this, R.id.cross_from);

        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(@NonNull View view) {
                // Requesting opening image in new activity and animating it from current position.
                ViewPosition position = ViewPosition.from(view);
                FullImageActivity.open(CrossActivitiesDemoActivity.this, position, IMAGE_ID);
            }
        });

        // Image position may change (i.e. when screen orientation is changed), so we should update
        // fullscreen image to ensure exit animation will return image into correct position.
        image.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                // Notifying fullscreen image activity about image position changes.
                ViewPosition position = ViewPosition.from(image);
                Events.create(EVENT_POSITION_CHANGED).param(position).post();
            }
        });

        // Loading image
        GlideHelper.loadResource(IMAGE_ID, image);
    }

    @Events.Subscribe(EVENT_SHOW_IMAGE)
    private void showImage(boolean show) {
        // Fullscreen activity requested to show or hide original image
        image.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
    }

}
