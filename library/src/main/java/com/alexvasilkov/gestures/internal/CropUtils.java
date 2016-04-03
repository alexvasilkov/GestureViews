package com.alexvasilkov.gestures.internal;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

import com.alexvasilkov.gestures.Settings;
import com.alexvasilkov.gestures.State;

public class CropUtils {

    private CropUtils() {}

    /**
     * Crops image drawable into bitmap according to current image position.
     */
    public static Bitmap crop(Drawable drawable, State state, Settings settings) {
        if (drawable == null) {
            return null;
        }

        float zoom = state.getZoom();

        // Computing crop size for base zoom level (zoom == 1)
        int width = Math.round(settings.getMovementAreaW() / zoom);
        int height = Math.round(settings.getMovementAreaH() / zoom);

        // Crop area coordinates within viewport
        Rect pos = MovementBounds.getMovementAreaWithGravity(settings);

        Matrix matrix = new Matrix();
        state.get(matrix);
        // Scaling to base zoom level (zoom == 1)
        matrix.postScale(1f / zoom, 1f / zoom, pos.left, pos.top);
        // Positioning crop area
        matrix.postTranslate(-pos.left, -pos.top);

        try {
            // Draw drawable into bitmap
            Bitmap dst = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

            Canvas canvas = new Canvas(dst);
            canvas.concat(matrix);
            drawable.draw(canvas);

            return dst;
        } catch (OutOfMemoryError e) {
            return null; // Not enough memory for cropped bitmap
        }
    }

}
