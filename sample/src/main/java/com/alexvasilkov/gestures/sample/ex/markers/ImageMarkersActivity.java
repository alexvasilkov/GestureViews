package com.alexvasilkov.gestures.sample.ex.markers;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.view.Gravity;

import com.alexvasilkov.gestures.sample.R;
import com.alexvasilkov.gestures.sample.base.BaseExampleActivity;
import com.alexvasilkov.gestures.sample.ex.markers.Marker.Mode;
import com.alexvasilkov.gestures.views.GestureImageView;

/**
 * This example demonstrates how to show markers on top of {@link GestureImageView} pinned to
 * particular image points.<br/>
 * See also {@link MarkersOverlay}.
 */
public class ImageMarkersActivity extends BaseExampleActivity {

    private GestureImageView image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.markers_screen);

        // Setting up world map
        image = findViewById(R.id.markers_image);
        image.setImageResource(R.drawable.world_map);
        image.getController().getSettings().setMaxZoom(4f);

        final MarkersOverlay overlay = findViewById(R.id.markers_overlay);
        overlay.attachToImage(image);

        // Adding pins to demonstrate "PIN" and "STICK" behavior.
        // PIN: Icon will be pinned to the image but will not be zoomed and rotated along with it.
        // STICK: Icon will be zoomed and rotated along with the image.

        overlay.addMarker(createMarker(Mode.PIN, 332, 179, 0f)); // New-York
        overlay.addMarker(createMarker(Mode.STICK, 617, 133, 0f)); // London
        overlay.addMarker(createMarker(Mode.STICK, 422, 470, 0f)); // São Paulo
        overlay.addMarker(createMarker(Mode.PIN, 628, 334, 0f)); // Lagos
        overlay.addMarker(createMarker(Mode.PIN, 1071, 266, 0f)); // Hong Kong
        overlay.addMarker(createMarker(Mode.STICK, 1212, 517, 180f)); // Sydney

        // Applying general options
        getSettingsListener().onSetupGestureView(image);
    }

    private Marker createMarker(Mode mode, int px, int py, float rotation) {
        final float density = getResources().getDisplayMetrics().density;
        final int iconTipOffsetY = Math.round(2 * density); // Icon's tip vertical offset

        final Marker marker = new Marker()
                .setLocation(px, py)
                .setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL)
                .setOffset(0, iconTipOffsetY)
                .setRotation(rotation)
                .setMode(mode);

        switch (mode) {
            case PIN:
                marker.setIcon(getIcon(R.color.primary));
                return marker;
            case STICK:
                // We should also set initial icon scale, taking into account icon's density
                marker.setIcon(getIcon(R.color.accent)).setScale(3f / density);
                return marker;
            default:
                throw new IllegalArgumentException("Unknown mode: " + mode);
        }
    }

    /**
     * Returns tinted icon.
     */
    private Drawable getIcon(@ColorRes int colorId) {
        Drawable icon = ContextCompat.getDrawable(this, R.drawable.ic_place_black_24dp);
        icon = DrawableCompat.wrap(icon);
        DrawableCompat.setTint(icon, ContextCompat.getColor(this, colorId));
        return icon;
    }

    @Override
    protected void onSettingsChanged() {
        // Applying settings from toolbar menu, see BaseExampleActivity
        getSettingsListener().onSetupGestureView(image);
        // Resetting to initial image state
        image.getController().resetState();
    }

}
