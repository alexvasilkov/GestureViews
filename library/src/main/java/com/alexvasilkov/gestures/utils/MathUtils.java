package com.alexvasilkov.gestures.utils;

import android.graphics.Matrix;
import android.graphics.RectF;

import com.alexvasilkov.gestures.State;

import androidx.annotation.Size;

public class MathUtils {

    private static final Matrix tmpMatrix = new Matrix();
    private static final Matrix tmpMatrixInverse = new Matrix();

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
    public static void interpolate(RectF out, RectF start, RectF end, float factor) {
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
     */
    @SuppressWarnings("WeakerAccess") // Public API
    public static void interpolate(State out, State start, State end, float factor) {
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
    public static void interpolate(State out, State start, float startPivotX, float startPivotY,
            State end, float endPivotX, float endPivotY, float factor) {
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

        float dx = interpolate(0, endPivotX - startPivotX, factor);
        float dy = interpolate(0, endPivotY - startPivotY, factor);
        out.translateBy(dx, dy);
    }

    public static void computeNewPosition(@Size(2) float[] point,
            State initialState, State finalState) {
        initialState.get(tmpMatrix);
        tmpMatrix.invert(tmpMatrixInverse);
        tmpMatrixInverse.mapPoints(point);
        finalState.get(tmpMatrix);
        tmpMatrix.mapPoints(point);
    }

}
