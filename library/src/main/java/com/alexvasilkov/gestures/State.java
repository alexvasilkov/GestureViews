package com.alexvasilkov.gestures;

import android.graphics.Matrix;

public class State implements Cloneable {

    public float x, y;
    public float zoom = 1f;
    public float rotation;

    /**
     * Applying state to provided matrix. Matrix will contain translation, scale and rotation.
     */
    public void apply(Matrix matrix) {
        matrix.reset();
        matrix.postScale(zoom, zoom);
        matrix.postRotate(rotation);
        matrix.postTranslate(x, y);
    }

    @Override
    public State clone() {
        State copy = new State();
        copy.x = x;
        copy.y = y;
        copy.zoom = zoom;
        copy.rotation = rotation;
        return copy;
    }

}
