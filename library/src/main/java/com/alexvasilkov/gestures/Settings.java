package com.alexvasilkov.gestures;

import android.content.Context;
import android.util.TypedValue;
import android.view.Gravity;

/**
 * Various settings needed for {@link GesturesController}
 * and for {@link StateController}.
 * <p/>
 * Required settings are viewport size ({@link #setViewport(int, int)}) and view size {@link #setSize(int, int)}
 */
public class Settings {

    public static final float MAX_ZOOM = 2f;
    public static final float OVERZOOM_FACTOR = 1.2f;
    /**
     * Overscroll distance in DP
     */
    public static final float OVERSCROLL_DISTANCE = 40;

    /**
     * Viewport area
     */
    private int viewportW, viewportH;

    /**
     * View size
     */
    private int viewW, viewH;

    /**
     * Max zoom level, default value is {@link #MAX_ZOOM}
     */
    private float maxZoom = MAX_ZOOM;

    /**
     * Overzoom factor
     */
    private float overzoomFactor = OVERZOOM_FACTOR;

    /**
     * Overscroll distance
     */
    private float overscrollDistance;

    /**
     * If isFillViewport = true small view will be scaled to fitMethod entire viewport
     * even if it will require zoom level above max zoom level
     */
    private boolean isFillViewport = false;

    /**
     * View's gravity inside viewport area
     */
    private int gravity = Gravity.CENTER;

    /**
     * Initial fitting within viewport area
     */
    private Fit fitMethod = Fit.INSIDE;

    /**
     * Whether panning & zooming is enabled or not
     */
    private boolean isEnabled = true;

    /**
     * Whether zooming by double tap is enabled or not
     */
    private boolean isDoubleTapEnabled = true;

    /**
     * Whether rotation gesture is enabled or not
     */
    private boolean isRotationEnabled = false;

    /**
     * Whether view's transformations should be kept in bounds or not
     */
    private boolean isRestrictBounds = true;

    Settings(Context context) {
        setOverscrollDistance(context, OVERSCROLL_DISTANCE);
    }

    /**
     * Setting viewport size
     */
    public Settings setViewport(int w, int h) {
        viewportW = w;
        viewportH = h;
        return this;
    }

    /**
     * Setting full view size
     */
    public Settings setSize(int w, int h) {
        viewW = w;
        viewH = h;
        return this;
    }

    /**
     * Setting max zoom level.
     * <p/>
     * Default value is {@link #MAX_ZOOM}.
     */
    public Settings setMaxZoom(float maxZoom) {
        this.maxZoom = maxZoom;
        return this;
    }

    /**
     * Setting overzoom factor. User will be able to "over zoom" up to this factor. Cannot be < 1.
     * <p/>
     * Default value is {@link #OVERZOOM_FACTOR}.
     */
    public Settings setOverzoomFactor(float factor) {
        if (factor < 1f) throw new IllegalArgumentException("Overzoom factor cannot be < 1");
        overzoomFactor = factor;
        return this;
    }

    /**
     * Setting overscroll distance in pixels. User will be able to "over scroll" up to this distance. Cannot be < 0.
     * <p/>
     * Default value is {@link #OVERSCROLL_DISTANCE} converted to pixels.
     */
    public Settings setOverscrollDistance(float distance) {
        if (distance < 0f) throw new IllegalArgumentException("Overscroll distance cannot be < 0");
        overscrollDistance = distance;
        return this;
    }

    /**
     * Same as {@link #setOverscrollDistance(float)} but accepts distance in DP
     */
    public Settings setOverscrollDistance(Context context, float distanceDp) {
        return setOverscrollDistance(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, distanceDp,
                context.getResources().getDisplayMetrics()));
    }

    /**
     * If set to true small view will be scaled to fitMethod entire viewport even if this will require zoom level
     * above max zoom level.
     * <p/>
     * Default value is false.
     */
    public Settings setFillViewport(boolean isFitViewport) {
        this.isFillViewport = isFitViewport;
        return this;
    }

    /**
     * Setting view's gravity inside viewport area.
     * <p/>
     * Default value is {@link android.view.Gravity#CENTER}.
     */
    public Settings setGravity(int gravity) {
        this.gravity = gravity;
        return this;
    }

    /**
     * Setting view's fitting within viewport area.
     * <p/>
     * Default value is {@link com.alexvasilkov.gestures.Settings.Fit#INSIDE}.
     */
    public Settings setFitMethod(Fit fitMethod) {
        if (fitMethod == null) throw new NullPointerException("Fitting method cannot be null");
        this.fitMethod = fitMethod;
        return this;
    }

    /**
     * Sets whether panning & zooming is enabled or not.
     * <p/>
     * Default value is true.
     */
    public Settings setEnabled(boolean enabled) {
        isEnabled = enabled;
        return this;
    }

    /**
     * Sets whether zooming by double tap is enabled or not.
     * <p/>
     * Default value is true.
     */
    public Settings setDoubleTapEnabled(boolean enabled) {
        isDoubleTapEnabled = enabled;
        return this;
    }

    /**
     * Sets whether rotation gesture is enabled or not.
     * <p/>
     * Default value is false.
     */
    public Settings setRotationEnabled(boolean enabled) {
        isRotationEnabled = enabled;
        return this;
    }

    /**
     * Sets whether view's transformations should be kept in bounds or not.
     * <p/>
     * Default value is true.
     */
    public Settings setRestrictBounds(boolean isRestrictBounds) {
        this.isRestrictBounds = isRestrictBounds;
        return this;
    }


    // --------------
    //  Getters
    // --------------

    public int getViewportW() {
        return viewportW;
    }

    public int getViewportH() {
        return viewportH;
    }

    public int getViewW() {
        return viewW;
    }

    public int getViewH() {
        return viewH;
    }

    public float getMaxZoom() {
        return maxZoom;
    }

    public float getOverzoomFactor() {
        return overzoomFactor;
    }

    public float getOverscrollDistance() {
        return overscrollDistance;
    }

    public boolean isFillViewport() {
        return isFillViewport;
    }

    public int getGravity() {
        return gravity;
    }

    public Fit getFitMethod() {
        return fitMethod;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public boolean isDoubleTapEnabled() {
        return isDoubleTapEnabled;
    }

    public boolean isRotationEnabled() {
        return isRotationEnabled;
    }

    public boolean isRestrictBounds() {
        return isRestrictBounds;
    }


    public static enum Fit {
        /**
         * Fit view width inside viewport area
         */
        HORIZONTAL,

        /**
         * Fit view height inside viewport area
         */
        VERTICAL,

        /**
         * Fit both view width and view height inside viewport area
         */
        INSIDE,

        /**
         * Fit view width or view height inside viewport area, so the entire viewport is filled up
         */
        OUTSIDE
    }

}
