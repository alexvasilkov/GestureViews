package com.alexvasilkov.gestures;

import android.graphics.Matrix;

/**
 * Represents 2d transformation state.
 */
@SuppressWarnings("WeakerAccess") // Public API (fields and methods)
public class State {

    public static final float EPSILON = 0.001f;

    private final Matrix matrix = new Matrix();
    private final float[] matrixValues = new float[9];

    private float x;
    private float y;
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

    /**
     * @return Rotation in degrees within the range [-180..180].
     */
    public float getRotation() {
        return rotation;
    }

    /**
     * @return {@code true} if {@code x == 0f && y == 0f && zoom == 1f && rotation == 0f}
     */
    @SuppressWarnings("unused") // Public API
    public boolean isEmpty() {
        return x == 0f && y == 0f && zoom == 1f && rotation == 0f;
    }

    /**
     * Applying state to provided matrix. Matrix will contain translation, scale and rotation.
     *
     * @param matrix Target matrix
     */
    public void get(Matrix matrix) {
        matrix.set(this.matrix);
    }

    public void translateBy(float dx, float dy) {
        matrix.postTranslate(dx, dy);
        updateFromMatrix(false, false); // only translation is changed
    }

    public void translateTo(float x, float y) {
        matrix.postTranslate(-this.x + x, -this.y + y);
        updateFromMatrix(false, false); // only translation is changed
    }

    public void zoomBy(float factor, float pivotX, float pivotY) {
        matrix.postScale(factor, factor, pivotX, pivotY);
        updateFromMatrix(true, false); // zoom & translation are changed
    }

    public void zoomTo(float zoom, float pivotX, float pivotY) {
        matrix.postScale(zoom / this.zoom, zoom / this.zoom, pivotX, pivotY);
        updateFromMatrix(true, false); // zoom & translation are changed
    }

    public void rotateBy(float angle, float pivotX, float pivotY) {
        matrix.postRotate(angle, pivotX, pivotY);
        updateFromMatrix(false, true); // rotation & translation are changed
    }

    public void rotateTo(float angle, float pivotX, float pivotY) {
        matrix.postRotate(-rotation + angle, pivotX, pivotY);
        updateFromMatrix(false, true); // rotation & translation are changed
    }

    public void set(float x, float y, float zoom, float rotation) {
        // Keeping rotation within the range [-180..180]
        while (rotation < -180f) {
            rotation += 360f;
        }
        while (rotation > 180f) {
            rotation -= 360f;
        }

        this.x = x;
        this.y = y;
        this.zoom = zoom;
        this.rotation = rotation;

        // Note, that order is vital here
        matrix.reset();
        if (zoom != 1f) {
            matrix.postScale(zoom, zoom);
        }
        if (rotation != 0f) {
            matrix.postRotate(rotation);
        }
        matrix.postTranslate(x, y);
    }

    /**
     * Applying state from given matrix. Matrix should contain correct translation/scale/rotation.
     *
     * @param matrix Source matrix
     */
    public void set(Matrix matrix) {
        this.matrix.set(matrix);
        updateFromMatrix(true, true);
    }

    public void set(State other) {
        x = other.x;
        y = other.y;
        zoom = other.zoom;
        rotation = other.rotation;
        matrix.set(other.matrix);
    }

    public State copy() {
        State copy = new State();
        copy.set(this);
        return copy;
    }

    /**
     * Applying state from current matrix.
     * <p>
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
     * See <a href="http://stackoverflow.com/questions/4361242">here</a>.
     *
     * @param updateZoom Whether to extract zoom from matrix
     * @param updateRotation Whether to extract rotation from matrix
     */
    private void updateFromMatrix(boolean updateZoom, boolean updateRotation) {
        matrix.getValues(matrixValues);
        x = matrixValues[2];
        y = matrixValues[5];
        if (updateZoom) {
            zoom = (float) Math.hypot(matrixValues[1], matrixValues[4]);
        }
        if (updateRotation) {
            rotation = (float) Math.toDegrees(Math.atan2(matrixValues[3], matrixValues[4]));
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        State state = (State) obj;

        return equals(state.x, x) && equals(state.y, y)
                && equals(state.zoom, zoom) && equals(state.rotation, rotation);
    }

    @Override
    public int hashCode() {
        int result = (x != +0.0f ? Float.floatToIntBits(x) : 0);
        result = 31 * result + (y != +0.0f ? Float.floatToIntBits(y) : 0);
        result = 31 * result + (zoom != +0.0f ? Float.floatToIntBits(zoom) : 0);
        result = 31 * result + (rotation != +0.0f ? Float.floatToIntBits(rotation) : 0);
        return result;
    }

    @Override
    public String toString() {
        return "{x=" + x + ",y=" + y + ",zoom=" + zoom + ",rotation=" + rotation + "}";
    }

    /**
     * Compares two float values, allowing small difference (see {@link #EPSILON}).
     *
     * @param v1 First value
     * @param v2 Second value
     * @return True if both values are close enough to be considered as equal
     */
    @SuppressWarnings("checkstyle:overloadmethodsdeclarationorder")
    public static boolean equals(float v1, float v2) {
        return v1 >= v2 - EPSILON && v1 <= v2 + EPSILON;
    }

    /**
     * Compares two float values, allowing small difference (see {@link #EPSILON}).
     *
     * @param v1 First value
     * @param v2 Second value
     * @return Positive int if first value is greater than second, negative int if second value
     * is greater than first or 0 if both values are close enough to be considered as equal
     */
    public static int compare(float v1, float v2) {
        return v1 > v2 + EPSILON ? 1 : v1 < v2 - EPSILON ? -1 : 0;
    }

}
