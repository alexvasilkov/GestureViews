package com.alexvasilkov.gestures.sample.ex.single.cross;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.ImageView;

import com.alexvasilkov.events.Events;
import com.alexvasilkov.gestures.animation.ViewPosition;
import com.alexvasilkov.gestures.sample.R;
import com.alexvasilkov.gestures.sample.base.BaseActivity;
import com.alexvasilkov.gestures.sample.ex.utils.GlideHelper;
import com.alexvasilkov.gestures.sample.ex.utils.Painting;

/**
 * This example demonstrates image animation that crosses activities bounds.<br/>
 * Cross-activities animation is pretty complicated, since we'll need to have a connection between
 * activities in order to properly coordinate image position changes and animation state.<br/>
 * In this example we will use {@link Events} library to set up such connection, but you can also
 * do it using e.g. {@link LocalBroadcastManager} or manually by setting and removing listeners.
 */
public class ImageCrossAnimationActivity extends BaseActivity {

    private static final int PAINTING_ID = 2;

    private ImageView image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.image_cross_animation_from_screen);
        getSupportActionBarNotNull().setDisplayHomeAsUpEnabled(true);

        image = findViewById(R.id.single_image_from);

        // Loading image
        Painting painting = Painting.list(getResources())[PAINTING_ID];
        GlideHelper.loadThumb(image, painting.thumbId);

        // Setting image click listener
        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(@NonNull View view) {
                showFullImage();
            }
        });

        // Image position may change (e.g. when screen orientation is changed), so we should update
        // fullscreen image to ensure exit animation will return image into correct position.
        image.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                onLayoutChanges();
            }
        });
    }

    private void showFullImage() {
        // Requesting opening image in a new activity with animation.
        // First of all we need to get current image position:
        ViewPosition position = ViewPosition.from(image);
        // Now pass this position to a new activity. New activity should start without any
        // animations and should have transparent window (set through activity theme).
        FullImageActivity.open(this, position, PAINTING_ID);
    }

    private void onLayoutChanges() {
        // Notifying fullscreen image activity about image position changes.
        ViewPosition position = ViewPosition.from(image);
        Events.create(CrossEvents.POSITION_CHANGED).param(position).post();
    }

    @Events.Subscribe(CrossEvents.SHOW_IMAGE)
    private void showImage(boolean show) {
        // Fullscreen activity requested to show or hide original image
        image.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
    }

}
