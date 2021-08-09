package com.alexvasilkov.gestures.utils;

import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;

import androidx.annotation.NonNull;
import androidx.annotation.Size;

import com.alexvasilkov.gestures.State;

public class MathUtils {

    private static final Matrix tmpMatrix = new Matrix();
    private static final Matrix tmpMatrixInverse = new Matrix();
    private static final RectF tmpRect = new RectF();

    private MathUtils() {}

    /**
     * Keeps value within provided bounds.
     *
     * @param value Value to be restricted
     * @param minValue Min value
     * @param maxValue Max value
     * @return Restricted value
     */
    public static float restrict(float value, float minValue, float maxValue) {
        return Math.max(minValue, Math.min(value, maxValue));
    }

    /**
     * Interpolates from start value to the end one by given factor (from 0 to 1).
     *
     * @param start Start value
     * @param end End value
     * @param factor Factor
     * @return Interpolated value
     */
    public static float interpolate(float start, float end, float factor) {
        return start + (end - start) * factor;
    }

    /**
     * Interpolates from start rect to the end rect by given factor (from 0 to 1),
     * storing result into out rect.
     *
     * @param out Interpolated rectangle (output)
     * @param start Start rectangle
     * @param end End rectangle
     * @param factor Factor
     */
    public static void interpolate(
            @NonNull RectF out,
            @NonNull RectF start,
            @NonNull RectF end,
            float factor
    ) {
        out.left = interpolate(start.left, end.left, factor);
        out.top = interpolate(start.top, end.top, factor);
        out.right = interpolate(start.right, end.right, factor);
        out.bottom = interpolate(start.bottom, end.bottom, factor);
    }

    /**
     * Interpolates from start state to end state by given factor (from 0 to 1),
     * storing result into out state.
     *
     * @param out Interpolated state (output)
     * @param start Start state
     * @param end End state
     * @param factor Factor
     * @deprecated Provide pivot point explicitly with
     * {@link #interpolate(State, State, float, float, State, float, float, float)}
     */
    @SuppressWarnings("WeakerAccess") // Public API
    @Deprecated
    public static void interpolate(
            @NonNull State out,
            @NonNull State start,
            @NonNull State end,
            float factor
    ) {
        interpolate(out, start, start.getX(), start.getY(), end, end.getX(), end.getY(), factor);
    }

    /**
     * Interpolates from start state to end state by given factor (from 0 to 1),
     * storing result into out state. All operations (translation, zoom, rotation) will be
     * performed within specified pivot points, assuming start and end pivot points represent
     * same physical point on the image.
     *
     * @param out Interpolated state (output)
     * @param start Start state
     * @param startPivotX Pivot point's X coordinate in start state coordinates
     * @param startPivotY Pivot point's Y coordinate in start state coordinates
     * @param end End state
     * @param endPivotX Pivot point's X coordinate in end state coordinates
     * @param endPivotY Pivot point's Y coordinate in end state coordinates
     * @param factor Factor
     */
    public static void interpolate(
            @NonNull State out,
            @NonNull State start,
            float startPivotX,
            float startPivotY,
            @NonNull State end,
            float endPivotX,
            float endPivotY,
            float factor
    ) {
        out.set(start);

        if (!State.equals(start.getZoom(), end.getZoom())) {
            float zoom = interpolate(start.getZoom(), end.getZoom(), factor);
            out.zoomTo(zoom, startPivotX, startPivotY);
        }

        // Getting rotations
        float startRotation = start.getRotation();
        float endRotation = end.getRotation();

        float rotation = Float.NaN;

        // Choosing shortest path to interpolate
        if (Math.abs(startRotation - endRotation) <= 180f) {
            if (!State.equals(startRotation, endRotation)) {
                rotation = interpolate(startRotation, endRotation, factor);
            }
        } else {
            // Keeping rotation positive
            float startRotationPositive = startRotation < 0f ? startRotation + 360f : startRotation;
            float endRotationPositive = endRotation < 0f ? endRotation + 360f : endRotation;

            if (!State.equals(startRotationPositive, endRotationPositive)) {
                rotation = interpolate(startRotationPositive, endRotationPositive, factor);
            }
        }

        if (!Float.isNaN(rotation)) {
            out.rotateTo(rotation, startPivotX, startPivotY);
        }

        float dx = interpolate(0f, endPivotX - startPivotX, factor);
        float dy = interpolate(0f, endPivotY - startPivotY, factor);
        out.translateBy(dx, dy);
    }

    public static void computeNewPosition(
            @NonNull @Size(2) float[] point,
            @NonNull State initialState,
            @NonNull State finalState
    ) {
        initialState.get(tmpMatrix);
        tmpMatrix.invert(tmpMatrixInverse);
        tmpMatrixInverse.mapPoints(point);
        finalState.get(tmpMatrix);
        tmpMatrix.mapPoints(point);
    }

    public static void mapIntRect(@NonNull Matrix matrix, @NonNull Rect rect) {
        tmpRect.set(rect);
        matrix.mapRect(tmpRect);
        rect.set((int) tmpRect.left, (int) tmpRect.top, (int) tmpRect.right, (int) tmpRect.bottom);
    }

}
