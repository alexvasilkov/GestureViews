package com.alexvasilkov.gestures.utils;

import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.Gravity;

import com.alexvasilkov.gestures.Settings;
import com.alexvasilkov.gestures.State;

public class GravityUtils {

    private static final Matrix tmpMatrix = new Matrix();
    private static final RectF tmpRectF = new RectF();

    private static final Rect tmpRect1 = new Rect();
    private static final Rect tmpRect2 = new Rect();


    private GravityUtils() {}

    /**
     * Calculates image position (scaled and rotated) within viewport area with gravity applied.
     *
     * @param state Image state
     * @param settings Image settings
     * @param out Output rectangle
     */
    public static void getImagePosition(State state, Settings settings, Rect out) {
        state.get(tmpMatrix);
        getImagePosition(tmpMatrix, settings, out);
    }

    /**
     * Calculates image position (scaled and rotated) within viewport area with gravity applied.
     *
     * @param matrix Image matrix
     * @param settings Image settings
     * @param out Output rectangle
     */
    public static void getImagePosition(Matrix matrix, Settings settings, Rect out) {
        tmpRectF.set(0, 0, settings.getImageW(), settings.getImageH());

        matrix.mapRect(tmpRectF);

        final int w = Math.round(tmpRectF.width());
        final int h = Math.round(tmpRectF.height());

        // Calculating image position basing on gravity
        tmpRect1.set(0, 0, settings.getViewportW(), settings.getViewportH());
        Gravity.apply(settings.getGravity(), w, h, tmpRect1, out);
    }

    /**
     * Calculates movement area position within viewport area with gravity applied.
     *
     * @param settings Image settings
     * @param out Output rectangle
     */
    public static void getMovementAreaPosition(Settings settings, Rect out) {
        tmpRect1.set(0, 0, settings.getViewportW(), settings.getViewportH());
        Gravity.apply(settings.getGravity(),
                settings.getMovementAreaW(), settings.getMovementAreaH(), tmpRect1, out);
    }

    /**
     * Calculates default pivot point for scale and rotation.
     *
     * @param settings Image settings
     * @param out Output point
     */
    public static void getDefaultPivot(Settings settings, Point out) {
        getMovementAreaPosition(settings, tmpRect2);
        Gravity.apply(settings.getGravity(), 0, 0, tmpRect2, tmpRect1);
        out.set(tmpRect1.left, tmpRect1.top);
    }

}
