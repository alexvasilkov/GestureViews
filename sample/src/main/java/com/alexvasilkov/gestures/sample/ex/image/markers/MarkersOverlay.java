package com.alexvasilkov.gestures.sample.ex.image.markers;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import androidx.annotation.NonNull;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;

import com.alexvasilkov.gestures.GestureController.OnStateChangeListener;
import com.alexvasilkov.gestures.State;
import com.alexvasilkov.gestures.views.GestureImageView;

import java.util.ArrayList;
import java.util.List;

/**
 * Special view that can draw markers on top of {@link GestureImageView} once it's attached
 * to it with {@link #attachToImage(GestureImageView)} and markers are added with
 * {@link #addMarker(Marker)}.
 */
public class MarkersOverlay extends View {

    private GestureImageView imageView;

    private final List<Marker> markers = new ArrayList<>();
    private final Rect posRect = new Rect();
    private final Rect iconRect = new Rect();
    private final Rect clipRect = new Rect();
    private final Matrix iconMatrix = new Matrix();
    private final float[] pointIn = new float[2];
    private final float[] pointOut = new float[2];


    public MarkersOverlay(Context context) {
        super(context);
    }

    public MarkersOverlay(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MarkersOverlay(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void attachToImage(GestureImageView imageView) {
        this.imageView = imageView;

        // We should compute and draw overlay whenever image state is changed
        imageView.getController().addOnStateChangeListener(new OnStateChangeListener() {
            @Override
            public void onStateChanged(State state) {
                invalidate();
            }

            @Override
            public void onStateReset(State oldState, State newState) {
                invalidate();
            }
        });
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

        final Drawable image = imageView == null ? null : imageView.getDrawable();
        if (image != null) {
            for (int i = 0, size = markers.size(); i < size; i++) {
                drawMarker(canvas, markers.get(i));
            }
        }
    }

    private void drawMarker(Canvas canvas, Marker marker) {
        final Drawable icon = marker.getIcon();

        if (icon == null) {
            throw new IllegalArgumentException("Marker must have an icon");
        }

        getIconMatrix(marker, iconMatrix);

        if (marker.getMode() == Marker.Mode.PIN) {

            // Computing marker location on the underlying image
            pointIn[0] = marker.getLocationX();
            pointIn[1] = marker.getLocationY();
            imageView.getImageMatrix().mapPoints(pointOut, pointIn);

            // Moving icon to corresponding position
            iconMatrix.postTranslate(pointOut[0], pointOut[1]);

        } else if (marker.getMode() == Marker.Mode.STICK) {

            // Moving icon to desired location
            iconMatrix.postTranslate(marker.getLocationX(), marker.getLocationY());
            // Computing icon position within canvas (including current image position)
            iconMatrix.postConcat(imageView.getImageMatrix());

        }

        drawIcon(canvas, icon, iconMatrix);
    }

    private void getIconMatrix(Marker marker, Matrix out) {
        // Positioning focal point within marker icon according to provided gravity.
        // In the end we should have a matrix which is when applied to the icon will position
        // it's focal point (specified by gravity + offsets) at (0, 0)

        final Drawable icon = marker.getIcon();
        iconRect.set(0, 0, icon.getIntrinsicWidth(), icon.getIntrinsicHeight());
        Gravity.apply(marker.getGravity(), 0, 0, iconRect, posRect);

        out.reset();
        out.postTranslate(-posRect.left, -posRect.top);
        out.postTranslate(marker.getOffsetX(), marker.getOffsetY());

        final float scale = marker.getScale();
        if (scale != 1f) {
            out.postScale(scale, scale);
        }

        final float rotation = marker.getRotation();
        if (rotation != 0f) {
            out.postRotate(rotation);
        }
    }

    private void drawIcon(Canvas canvas, Drawable icon, Matrix iconMatrix) {
        canvas.save();
        canvas.concat(iconMatrix);

        // Drawing icon if it is within a visible part of the canvas
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
