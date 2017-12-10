package com.alexvasilkov.gestures.sample.ex.image.markers;

import android.graphics.drawable.Drawable;
import android.view.Gravity;

@SuppressWarnings({ "WeakerAccess", "UnusedReturnValue", "SameParameterValue" }) // Public API
public class Marker {

    private Drawable icon;
    private int gravity = Gravity.TOP | Gravity.LEFT;
    private int locationX = 0;
    private int locationY = 0;
    private int offsetX = 0;
    private int offsetY = 0;
    private float scale = 1f;
    private float rotation = 0f;
    private Mode mode = Mode.PIN;

    /**
     * @param icon Drawable to be shown
     */
    public Marker setIcon(Drawable icon) {
        this.icon = icon;
        return this;
    }

    public Drawable getIcon() {
        return icon;
    }

    /**
     * @param gravity Gravity of the drawable. E.g. Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL,
     * means that drawable coordinates are applied to the center of the bottom edge of the
     * drawable.
     */
    public Marker setGravity(int gravity) {
        this.gravity = gravity;
        return this;
    }

    public int getGravity() {
        return gravity;
    }

    /**
     * @param x X coordinate in pixels relative to original image.
     * @param y Y coordinate in pixels relative to original image.
     */
    public Marker setLocation(int x, int y) {
        locationX = x;
        locationY = y;
        return this;
    }

    public int getLocationX() {
        return locationX;
    }

    public int getLocationY() {
        return locationY;
    }

    /**
     * @param x X offset in pixels relative to marker's icon.
     * @param y Y offset in pixels relative to marker's icon.
     */
    public Marker setOffset(int x, int y) {
        offsetX = x;
        offsetY = y;
        return this;
    }

    public int getOffsetX() {
        return offsetX;
    }

    public int getOffsetY() {
        return offsetY;
    }

    /**
     * Sets default scale for marker's icon.
     */
    public Marker setScale(float scale) {
        this.scale = scale;
        return this;
    }

    public float getScale() {
        return scale;
    }

    /**
     * Sets default rotation for marker's icon.
     */
    public Marker setRotation(float rotation) {
        this.rotation = rotation;
        return this;
    }

    public float getRotation() {
        return rotation;
    }

    public Marker setMode(Mode mode) {
        this.mode = mode;
        return this;
    }

    public Mode getMode() {
        return mode;
    }


    public enum Mode {
        /**
         * Pin is attached to an image according to specified gravity
         * but does not follow zoom and rotation.
         */
        PIN,
        /**
         * Pin is attached to an image according to specified gravity and follows zoom and rotation.
         */
        STICK
    }

}
