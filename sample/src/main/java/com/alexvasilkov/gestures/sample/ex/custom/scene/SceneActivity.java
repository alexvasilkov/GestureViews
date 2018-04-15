package com.alexvasilkov.gestures.sample.ex.custom.scene;

import android.os.Bundle;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;

import com.alexvasilkov.gestures.sample.R;
import com.alexvasilkov.gestures.sample.base.BaseActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * This example demonstrates advanced gesture controller usage.
 * <p>
 * Single tap selects an item on the screen. Once selected an item can be moved, scaled and rotated
 * within the bounds of the screen.
 */
public class SceneActivity extends BaseActivity {

    private SceneView scene;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        scene = new SceneView(this);
        setContentView(scene);
        getSupportActionBarNotNull().setDisplayHomeAsUpEnabled(true);
        setInfoText(R.string.info_objects_control);

        // Waiting for scene to be laid out before setting up the items
        scene.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                scene.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                showItems();
            }
        });
    }

    private void showItems() {
        // Setting initial positions for bowling pins and ball
        List<Item> items = new ArrayList<>(11);

        final float sceneWidth = scene.getWidth();
        final float sceneHeight = scene.getHeight();
        final float zoom = 2f / getResources().getDisplayMetrics().density;

        final float pinWidth = 175 * zoom;
        final float pinHeight = 480 * zoom;
        final float ballSize = 480 * zoom;

        // Setting up pins in a pyramid
        final float[] pinsX = new float[] { -2f, -1f, 0f, 1f, -1.5f, -0.5f, 0.5f, -1f, 0f, -0.5f };
        final float[] pinsY = new float[] { -3f, -3f, -3f, -3f, -2f, -2f, -2f, -1f, -1f, 0f };

        for (int i = 0; i < 10; i++) {
            float posX = 0.5f * sceneWidth + pinsX[i] * pinWidth;
            float posY = 0.5f * sceneHeight - pinHeight + 0.2f * pinsY[i] * pinHeight;

            Item pin = new Item(R.drawable.item_pin);
            pin.getState().set(posX, posY, zoom, 0);
            items.add(pin);
        }

        // Adding a ball
        float posX = 0.5f * (sceneWidth - ballSize);
        float posY = 0.5f * (sceneHeight + ballSize);

        Item ball = new Item(R.drawable.item_ball);
        ball.getState().set(posX, posY, zoom, 0);
        items.add(ball);

        // Updating scene items, setting min zoom setting
        scene.setItems(items);
        scene.getController().getSettings().setMinZoom(zoom);
    }

}
