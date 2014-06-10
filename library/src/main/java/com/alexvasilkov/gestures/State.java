package com.alexvasilkov.gestures;

import android.graphics.Matrix;

/**
 * Represents 2d transformation state
 */
public class State {

    private final Matrix matrix = new Matrix();
    private final float[] tmp = new float[9];

    private float x, y;
    private float zoom = 1f;
    private float rotation;

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getZoom() {
        return zoom;
    }

    public float getRotation() {
        return rotation;
    }

    public void translateBy(float dx, float dy) {
        matrix.postTranslate(dx, dy);
        updateFrom(matrix, false, false); // only translation is changed
    }

    public void translateTo(float x, float y) {
        matrix.postTranslate(-this.x + x, -this.y + y);
        updateFrom(matrix, false, false); // only translation is changed
    }

    public void zoomBy(float factor, float pivotX, float pivotY) {
        matrix.postScale(factor, factor, pivotX, pivotY);
        updateFrom(matrix, true, false); // zoom & translation are changed
    }

    public void zoomTo(float zoom, float pivotX, float pivotY) {
        matrix.postScale(zoom / this.zoom, zoom / this.zoom, pivotX, pivotY);
        updateFrom(matrix, true, false); // zoom & translation are changed
    }

    public void rotateBy(float angle, float pivotX, float pivotY) {
        matrix.postRotate(angle, pivotX, pivotY);
        updateFrom(matrix, false, true); // rotation & translation are changed
    }

    public void rotateTo(float angle, float pivotX, float pivotY) {
        matrix.postRotate(-rotation + angle, pivotX, pivotY);
        updateFrom(matrix, false, true); // rotation & translation are changed
    }

    public void set(float x, float y, float zoom, float rotation) {
        this.x = x;
        this.y = y;
        this.zoom = zoom;
        this.rotation = rotation;

        // Note, that order is vital here
        matrix.reset();
        matrix.postScale(zoom, zoom);
        matrix.postRotate(rotation);
        matrix.postTranslate(x, y);
    }

    /**
     * Applying state to provided matrix. Matrix will contain translation, scale and rotation.
     */
    public void applyTo(Matrix matrix) {
        matrix.set(this.matrix);
    }

    /**
     * Applying state from given matrix.
     */
    public void updateFrom(Matrix matrix) {
        updateFrom(matrix, true, true);
        this.matrix.set(matrix);
    }

    /**
     * Applying state from given matrix.
     * <p/>
     * Having matrix:
     * <pre>
     *     | a  b  tx |
     * A = | c  d  ty |
     *     | 0  0  1  |
     *
     * x = tx
     * y = ty
     * scale = sqrt(b^2+d^2)
     * rotation = atan(c/d) = atan(-b/a)
     * </pre>
     * See <a href="http://stackoverflow.com/questions/4361242/extract-rotation-scale-values-from-2d-transformation-matrix">here</a>.
     */
    private void updateFrom(Matrix matrix, boolean updateZoom, boolean updateRotation) {
        matrix.getValues(tmp);
        x = tmp[2];
        y = tmp[5];
        if (updateZoom) zoom = (float) Math.sqrt(tmp[1] * tmp[1] + tmp[4] * tmp[4]);
        if (updateRotation) rotation = (float) Math.toDegrees(Math.atan2(tmp[3], tmp[4]));
    }

    public State copy() {
        State copy = new State();
        copy.x = x;
        copy.y = y;
        copy.zoom = zoom;
        copy.rotation = rotation;
        copy.matrix.set(matrix);
        return copy;
    }

}
