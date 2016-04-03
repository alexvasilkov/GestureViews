package com.alexvasilkov.gestures;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.TypedValue;
import android.view.Gravity;

import com.alexvasilkov.gestures.views.interfaces.GestureView;

/**
 * Various settings needed for {@link GestureController} and for {@link StateController}.
 * <p/>
 * Required settings are viewport size ({@link #setViewport(int, int)})
 * and image size {@link #setImage(int, int)}
 */
public class Settings {

    public static final float MAX_ZOOM = 2f;
    public static final float OVERZOOM_FACTOR = 2f;

    /**
     * Viewport area.
     */
    private int viewportW;
    private int viewportH;

    /**
     * Moving area.
     */
    private int movementAreaW;
    private int movementAreaH;

    private boolean isMovementAreaSpecified;

    /**
     * Image size.
     */
    private int imageW;
    private int imageH;

    /**
     * Max zoom level, default value is {@link #MAX_ZOOM}.
     */
    private float maxZoom = MAX_ZOOM;

    /**
     * Overzoom factor.
     */
    private float overzoomFactor = OVERZOOM_FACTOR;

    /**
     * Overscroll distance.
     */
    private float overscrollDistanceX;
    private float overscrollDistanceY;

    /**
     * If isFillViewport = true small image will be scaled to fit entire viewport
     * even if it will require zoom level above max zoom level.
     */
    private boolean isFillViewport = false;

    /**
     * Image gravity inside viewport area.
     */
    private int gravity = Gravity.CENTER;

    /**
     * Initial fitting within viewport area.
     */
    private Fit fitMethod = Fit.INSIDE;

    /**
     * Whether panning is enabled or not.
     */
    private boolean isPanEnabled = true;

    /**
     * Whether zooming is enabled or not.
     */
    private boolean isZoomEnabled = true;

    /**
     * Whether rotation gesture is enabled or not.
     */
    private boolean isRotationEnabled = false;

    /**
     * Whether image rotation should stick to 90 degrees or can be free.
     */
    private boolean isRestrictRotation = false;

    /**
     * Whether zooming by double tap is enabled or not.
     */
    private boolean isDoubleTapEnabled = true;

    /**
     * Counter for gestures disable calls.
     */
    private int gesturesDisableCount;

    /**
     * Whether image transformations should be kept in bounds or not.
     */
    private boolean isRestrictBounds = true;

    Settings() {
        // Package private constructor
    }

    /**
     * Setting viewport size.
     * <p/>
     * Should only be used when implementing custom {@link GestureView}.
     */
    public Settings setViewport(int width, int height) {
        viewportW = width;
        viewportH = height;
        return this;
    }

    /**
     * Setting movement area size. Viewport area will be used instead if no movement area is
     * specified.
     */
    public Settings setMovementArea(int width, int height) {
        isMovementAreaSpecified = true;
        movementAreaW = width;
        movementAreaH = height;
        return this;
    }

    /**
     * Setting full image size.
     * <p/>
     * Should only be used when implementing custom {@link GestureView}.
     */
    public Settings setImage(int width, int height) {
        imageW = width;
        imageH = height;
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
        if (factor < 1f) {
            throw new IllegalArgumentException("Overzoom factor cannot be < 1");
        }
        overzoomFactor = factor;
        return this;
    }

    /**
     * Setting overscroll distance in pixels. User will be able to "over scroll"
     * up to this distance. Cannot be < 0.
     * <p/>
     * Default value is 0.
     */
    public Settings setOverscrollDistance(float distanceX, float distanceY) {
        if (distanceX < 0f || distanceY < 0f) {
            throw new IllegalArgumentException("Overscroll distance cannot be < 0");
        }
        overscrollDistanceX = distanceX;
        overscrollDistanceY = distanceY;
        return this;
    }

    /**
     * Same as {@link #setOverscrollDistance(float, float)} but accepts distance in DP.
     */
    public Settings setOverscrollDistance(Context context, float distanceXDp, float distanceYDp) {
        return setOverscrollDistance(toPixels(context, distanceXDp),
                toPixels(context, distanceYDp));
    }

    /**
     * If set to true small image will be scaled to fit entire viewport (or entire movement area
     * if it was set) even if this will require zoom level above max zoom level.
     * <p/>
     * Default value is false.
     */
    public Settings setFillViewport(boolean isFitViewport) {
        this.isFillViewport = isFitViewport;
        return this;
    }

    /**
     * Setting image gravity inside viewport area.
     * <p/>
     * Default value is {@link android.view.Gravity#CENTER}.
     */
    public Settings setGravity(int gravity) {
        this.gravity = gravity;
        return this;
    }

    /**
     * Setting image fitting method within viewport area.
     * <p/>
     * Default value is {@link Settings.Fit#INSIDE}.
     */
    public Settings setFitMethod(@NonNull Fit fitMethod) {
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
     * Sets whether image rotation should stick to 90 degrees intervals or can be free.
     * Only applied when {@link #isRestrictBounds()} is true as well.
     * <p/>
     * Default value is false.
     */
    public Settings setRestrictRotation(boolean isRestrictRotation) {
        this.isRestrictRotation = isRestrictRotation;
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
     * Disable all gestures.<br/>
     * Calls to this method are counted, so if you called it N times
     * you should call {@link #enableGestures()} N times to re-enable all gestures.
     * <p/>
     * Useful when you need temporary disable touch gestures during animation or image loading.
     * <p/>
     * See also {@link #enableGestures()}
     */
    public Settings disableGestures() {
        gesturesDisableCount++;
        return this;
    }

    /**
     * Re-enable all gestures disabled by {@link #disableGestures()} method.<br/>
     * Calls to this method are counted, so if you called {@link #disableGestures()} N times
     * you should call this method N times to re-enable all gestures.
     * <p/>
     * See also {@link #disableGestures()}
     */
    public Settings enableGestures() {
        gesturesDisableCount--;
        return this;
    }

    /**
     * Sets whether image transformations should be kept in bounds or not.
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

    public int getImageW() {
        return imageW;
    }

    public int getImageH() {
        return imageH;
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
        return isGesturesEnabled() && isPanEnabled;
    }

    public boolean isZoomEnabled() {
        return isGesturesEnabled() && isZoomEnabled;
    }

    public boolean isRotationEnabled() {
        return isGesturesEnabled() && isRotationEnabled;
    }

    public boolean isRestrictRotation() {
        return isRestrictRotation;
    }

    public boolean isDoubleTapEnabled() {
        return isGesturesEnabled() && isDoubleTapEnabled;
    }

    public boolean isGesturesEnabled() {
        return gesturesDisableCount <= 0;
    }

    public boolean isRestrictBounds() {
        return isRestrictBounds;
    }

    /**
     * Whether at least one of pan, zoom, rotation or double tap are enabled or not.
     */
    public boolean isEnabled() {
        return isGesturesEnabled()
                && (isPanEnabled || isZoomEnabled || isRotationEnabled || isDoubleTapEnabled);
    }


    public boolean hasImageSize() {
        return imageW != 0 && imageH != 0;
    }

    public boolean hasViewportSize() {
        return viewportW != 0 && viewportH != 0;
    }


    public enum Fit {
        /**
         * Fit image width inside viewport area.
         */
        HORIZONTAL,

        /**
         * Fit image height inside viewport area.
         */
        VERTICAL,

        /**
         * Fit both image width and image height inside viewport area.
         */
        INSIDE,

        /**
         * Fit image width or image height inside viewport area, so the entire viewport is filled.
         */
        OUTSIDE
    }

    private static float toPixels(Context context, float value) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value,
                context.getResources().getDisplayMetrics());
    }

}
