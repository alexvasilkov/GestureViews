package com.alexvasilkov.gestures.sample.ex9;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.Gravity;

import com.alexvasilkov.gestures.views.GestureImageView;

import java.util.ArrayList;
import java.util.List;

public class MarkerGestureImageView extends GestureImageView {

    private final List<Marker> markers = new ArrayList<>();
    private final Rect posRect = new Rect();
    private final Rect iconRect = new Rect();
    private final Rect clipRect = new Rect();
    private final Matrix iconMatrix = new Matrix();
    private final float[] pointIn = new float[2];
    private final float[] pointOut = new float[2];


    public MarkerGestureImageView(Context context) {
        super(context);
    }

    public MarkerGestureImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MarkerGestureImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /**
     * Shows marker at specified position with specified gravity.
     */
    public void addMarker(@NonNull Marker marker) {
        markers.add(marker);
        invalidate();
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        super.draw(canvas);

        final Drawable image = getDrawable();
        if (image != null) {
            for (int i = 0, size = markers.size(); i < size; i++) {
                drawMarker(canvas, markers.get(i));
            }
        }
    }

    private void drawMarker(Canvas canvas, Marker marker) {
        final Drawable icon = marker.getIcon();

        if (icon == null) {
            throw new IllegalArgumentException("Marker must specify an icon");
        }

        getIconMatrix(marker, iconMatrix);

        if (marker.getMode() == Marker.Mode.PIN) {
            // Computing location of icon's focal point (including image translation)
            pointIn[0] = marker.getLocationX();
            pointIn[1] = marker.getLocationY();
            getImageMatrix().mapPoints(pointOut, pointIn);

            // Moving icon to new position
            iconMatrix.postTranslate(pointOut[0], pointOut[1]);
        } else if (marker.getMode() == Marker.Mode.STICK) {
            // Moving icon to desired location
            iconMatrix.postTranslate(marker.getLocationX(), marker.getLocationY());
            // Computing icon position within canvas (including current image position)
            iconMatrix.postConcat(getImageMatrix());
        }

        drawIcon(canvas, icon, iconMatrix);
    }

    private void getIconMatrix(Marker marker, Matrix out) {
        // Positioning focal point within marker icon according to provided gravity
        final Drawable icon = marker.getIcon();
        iconRect.set(0, 0, icon.getIntrinsicWidth(), icon.getIntrinsicHeight());
        Gravity.apply(marker.getGravity(), 0, 0, iconRect, posRect);

        out.reset();
        out.postTranslate(-posRect.left, -posRect.top);
        out.postTranslate(marker.getOffsetX(), marker.getOffsetY());

        final float zoom = marker.getZoom();
        if (zoom != 1f) {
            out.postScale(zoom, zoom);
        }

        final float rotation = marker.getRotation();
        if (rotation != 0f) {
            out.postRotate(rotation);
        }
    }

    private void drawIcon(Canvas canvas, Drawable icon, Matrix iconMatrix) {
        canvas.save();
        canvas.concat(iconMatrix);

        // Drawing icon if it is within the canvas
        canvas.getClipBounds(clipRect);
        final int width = icon.getIntrinsicWidth();
        final int height = icon.getIntrinsicHeight();

        if (clipRect.intersects(0, 0, width, height)) {
            icon.setBounds(0, 0, width, height);
            icon.draw(canvas);
        }

        canvas.restore();
    }

}
