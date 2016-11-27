package com.alexvasilkov.gestures.utils;

import android.graphics.RectF;

import com.alexvasilkov.gestures.State;

public class MathUtils {

    private MathUtils() {}

    /**
     * Keeps value within provided bounds.
     */
    public static float restrict(float value, float minValue, float maxValue) {
        return Math.max(minValue, Math.min(value, maxValue));
    }

    /**
     * Interpolates from start value to the end one by given factor (from 0 to 1).
     */
    public static float interpolate(float start, float end, float factor) {
        return start + (end - start) * factor;
    }

    /**
     * Interpolates from start rect to the end rect by given factor (from 0 to 1),
     * storing result into out rect.
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

}
