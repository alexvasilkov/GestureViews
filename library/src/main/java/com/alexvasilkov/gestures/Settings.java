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
    public static final float OVERSCROLL_DISTANCE = 0;

    /**
     * Viewport area
     */
    private int viewportW, viewportH;

    /**
     * Moving area
     */
    private int movementAreaW, movementAreaH;

    private boolean isMovementAreaSpecified;

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
    private float overscrollDistanceX, overscrollDistanceY;

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
     * Whether panning is enabled or not
     */
    private boolean isPanEnabled = true;

    /**
     * Whether zooming is enabled or not
     */
    private boolean isZoomEnabled = true;

    /**
     * Whether rotation gesture is enabled or not
     */
    private boolean isRotationEnabled = false;

    /**
     * Whether zooming by double tap is enabled or not
     */
    private boolean isDoubleTapEnabled = true;

    /**
     * Whether view's transformations should be kept in bounds or not
     */
    private boolean isRestrictBounds = true;

    Settings(Context context) {
        setOverscrollDistance(context, OVERSCROLL_DISTANCE, OVERSCROLL_DISTANCE);
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
     * Setting movement area size
     */
    public Settings setMovementArea(int w, int h) {
        isMovementAreaSpecified = true;
        movementAreaW = w;
        movementAreaH = h;
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
    public Settings setOverscrollDistance(float distanceX, float distanceY) {
        if (distanceX < 0f || distanceY < 0f) throw new IllegalArgumentException("Overscroll distance cannot be < 0");
        overscrollDistanceX = distanceX;
        overscrollDistanceY = distanceY;
        return this;
    }

    /**
     * Same as {@link #setOverscrollDistance(float, float)} but accepts distance in DP
     */
    public Settings setOverscrollDistance(Context context, float distanceXDp, float distanceYDp) {
        return setOverscrollDistance(toPixels(context, distanceXDp), toPixels(context, distanceYDp));
    }

    /**
     * If set to true small view will be scaled to fit entire viewport (or entire movement area if it was set)
     * even if this will require zoom level above max zoom level.
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
     * Sets whether panning is enabled or not.
     * <p/>
     * Default value is true.
     */
    public Settings setPanEnabled(boolean enabled) {
        isPanEnabled = enabled;
        return this;
    }

    /**
     * Sets whether zooming is enabled or not.
     * <p/>
     * Default value is true.
     */
    public Settings setZoomEnabled(boolean enabled) {
        isZoomEnabled = enabled;
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
     * Sets whether zooming by double tap is enabled or not.
     * <p/>
     * Default value is true.
     */
    public Settings setDoubleTapEnabled(boolean enabled) {
        isDoubleTapEnabled = enabled;
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

    public int getMovementAreaW() {
        return isMovementAreaSpecified ? movementAreaW : viewportW;
    }

    public int getMovementAreaH() {
        return isMovementAreaSpecified ? movementAreaH : viewportH;
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

    public float getOverscrollDistanceX() {
        return overscrollDistanceX;
    }

    public float getOverscrollDistanceY() {
        return overscrollDistanceY;
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

    public boolean isPanEnabled() {
        return isPanEnabled;
    }

    public boolean isZoomEnabled() {
        return isZoomEnabled;
    }

    public boolean isRotationEnabled() {
        return isRotationEnabled;
    }

    public boolean isDoubleTapEnabled() {
        return isDoubleTapEnabled;
    }

    public boolean isRestrictBounds() {
        return isRestrictBounds;
    }

    /**
     * Whether at least one of pan, zoom, rotation or double tap are enabled or not
     */
    public boolean isEnabled() {
        return isPanEnabled || isZoomEnabled || isRotationEnabled || isDoubleTapEnabled;
    }


    boolean hasViewSize() {
        return viewW != 0 && viewH != 0;
    }

    boolean hasViewportSize() {
        return viewportW != 0 && viewportH != 0;
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

    private static float toPixels(Context context, float value) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value,
                context.getResources().getDisplayMetrics());
    }

}
