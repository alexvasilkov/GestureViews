package com.alexvasilkov.gestures;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.Gravity;

import com.alexvasilkov.gestures.internal.UnitsUtils;
import com.alexvasilkov.gestures.views.interfaces.GestureView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Various settings needed for {@link GestureController} and for {@link StateController}.
 * <p>
 * Required settings are viewport size ({@link #setViewport(int, int)})
 * and image size {@link #setImage(int, int)}
 */
@SuppressWarnings({ "WeakerAccess", "UnusedReturnValue", "SameParameterValue" }) // Public API
public class Settings {

    public static final float MAX_ZOOM = 2f;
    public static final float OVERZOOM_FACTOR = 2f;
    public static final long ANIMATIONS_DURATION = 300L;

    /*
     * Viewport area.
     */
    private int viewportW;
    private int viewportH;

    /*
     * Movement area.
     */
    private int movementAreaW;
    private int movementAreaH;

    private boolean isMovementAreaSpecified;

    /*
     * Image size.
     */
    private int imageW;
    private int imageH;

    /*
     * Min zoom level, default value is 0f, meaning min zoom will be adjusted to fit viewport.
     */
    private float minZoom = 0f;

    /*
     * Max zoom level, default value is {@link #MAX_ZOOM}.
     */
    private float maxZoom = MAX_ZOOM;

    /*
     * Double tap zoom level, default value is -1. Defaults to {@link #maxZoom} if <= 0.
     */
    private float doubleTapZoom = -1f;

    /*
     * Overzoom factor.
     */
    private float overzoomFactor = OVERZOOM_FACTOR;

    /*
     * Overscroll distance.
     */
    private float overscrollDistanceX;
    private float overscrollDistanceY;

    /*
     * If isFillViewport is true:
     * Small images will be scaled to fit viewport even if it will require zooming above max zoom.
     * Big images will be scaled to fit viewport even if it will require zooming below min zoom.
     */
    private boolean isFillViewport = false;

    /*
     * Image gravity inside viewport area.
     */
    private int gravity = Gravity.CENTER;

    /*
     * Initial fitting within viewport area.
     */
    private Fit fitMethod = Fit.INSIDE;

    /*
     * Movement bounds restriction type.
     */
    private Bounds boundsType = Bounds.NORMAL;

    /*
     * Whether panning is enabled or not.
     */
    private boolean isPanEnabled = true;

    /*
     * Whether fling (inertial motion after scroll) is enabled or not.
     */
    private boolean isFlingEnabled = true;

    /*
     * Whether zooming is enabled or not.
     */
    private boolean isZoomEnabled = true;

    /*
     * Whether rotation gesture is enabled or not.
     */
    private boolean isRotationEnabled = false;

    /*
     * Whether image rotation should stick to 90 degrees or can be free.
     */
    private boolean isRestrictRotation = false;

    /*
     * Whether zooming by double tap is enabled or not.
     */
    private boolean isDoubleTapEnabled = true;

    /*
     * Which gestures to use to detect exit.
     */
    private ExitType exitType = ExitType.ALL;

    /*
     * Counter for gestures disabling calls.
     */
    private int gesturesDisableCount;

    /*
     * Counter for bounds disabling calls.
     */
    private int boundsDisableCount;

    /*
     * Duration of animations.
     */
    private long animationsDuration = ANIMATIONS_DURATION;

    Settings() {
        // Package private constructor
    }

    public void initFromAttributes(@NonNull Context context, @Nullable AttributeSet attrs) {
        if (attrs == null) {
            return;
        }

        TypedArray arr = context.obtainStyledAttributes(attrs, R.styleable.GestureView);

        movementAreaW = arr.getDimensionPixelSize(
                R.styleable.GestureView_gest_movementAreaWidth, movementAreaW);
        movementAreaH = arr.getDimensionPixelSize(
                R.styleable.GestureView_gest_movementAreaHeight, movementAreaH);
        isMovementAreaSpecified = movementAreaW > 0 && movementAreaH > 0;

        minZoom = arr.getFloat(
                R.styleable.GestureView_gest_minZoom, minZoom);
        maxZoom = arr.getFloat(
                R.styleable.GestureView_gest_maxZoom, maxZoom);
        doubleTapZoom = arr.getFloat(
                R.styleable.GestureView_gest_doubleTapZoom, doubleTapZoom);
        overzoomFactor = arr.getFloat(
                R.styleable.GestureView_gest_overzoomFactor, overzoomFactor);
        overscrollDistanceX = arr.getDimension(
                R.styleable.GestureView_gest_overscrollX, overscrollDistanceX);
        overscrollDistanceY = arr.getDimension(
                R.styleable.GestureView_gest_overscrollY, overscrollDistanceY);
        isFillViewport = arr.getBoolean(
                R.styleable.GestureView_gest_fillViewport, isFillViewport);
        gravity = arr.getInt(
                R.styleable.GestureView_gest_gravity, gravity);

        int fitMethodPos = arr.getInteger(
                R.styleable.GestureView_gest_fitMethod, fitMethod.ordinal());
        fitMethod = Fit.values()[fitMethodPos];

        int boundsTypePos = arr.getInteger(
                R.styleable.GestureView_gest_boundsType, boundsType.ordinal());
        boundsType = Bounds.values()[boundsTypePos];

        isPanEnabled = arr.getBoolean(
                R.styleable.GestureView_gest_panEnabled, isPanEnabled);
        isFlingEnabled = arr.getBoolean(
                R.styleable.GestureView_gest_flingEnabled, isFlingEnabled);
        isZoomEnabled = arr.getBoolean(
                R.styleable.GestureView_gest_zoomEnabled, isZoomEnabled);
        isRotationEnabled = arr.getBoolean(
                R.styleable.GestureView_gest_rotationEnabled, isRotationEnabled);
        isRestrictRotation = arr.getBoolean(
                R.styleable.GestureView_gest_restrictRotation, isRestrictRotation);
        isDoubleTapEnabled = arr.getBoolean(
                R.styleable.GestureView_gest_doubleTapEnabled, isDoubleTapEnabled);
        exitType = arr.getBoolean(
                R.styleable.GestureView_gest_exitEnabled, true) ? exitType : ExitType.NONE;
        animationsDuration = arr.getInt(
                R.styleable.GestureView_gest_animationDuration, (int) animationsDuration);

        boolean disableGestures = arr.getBoolean(
                R.styleable.GestureView_gest_disableGestures, false);
        if (disableGestures) {
            disableGestures();
        }

        boolean disableBounds = arr.getBoolean(
                R.styleable.GestureView_gest_disableBounds, false);
        if (disableBounds) {
            disableBounds();
        }

        arr.recycle();
    }

    /**
     * Setting viewport size.
     * <p>
     * Should only be used when implementing custom {@link GestureView}.
     *
     * @param width Viewport width
     * @param height Viewport height
     * @return Current settings object for calls chaining
     */
    public Settings setViewport(int width, int height) {
        viewportW = width;
        viewportH = height;
        return this;
    }

    /**
     * Setting movement area size. Viewport area will be used instead if no movement area is
     * specified.
     *
     * @param width Movement area width
     * @param height Movement area height
     * @return Current settings object for calls chaining
     */
    public Settings setMovementArea(int width, int height) {
        isMovementAreaSpecified = true;
        movementAreaW = width;
        movementAreaH = height;
        return this;
    }

    /**
     * Setting full image size.
     * <p>
     * Should only be used when implementing custom {@link GestureView}.
     *
     * @param width Image width
     * @param height Image height
     * @return Current settings object for calls chaining
     */
    public Settings setImage(int width, int height) {
        imageW = width;
        imageH = height;
        return this;
    }

    /**
     * Setting min zoom level.
     * <p>
     * Default value is 0.
     *
     * @param minZoom Min zoom level, or 0 to use zoom level which fits the image into the viewport.
     * @return Current settings object for calls chaining
     */
    public Settings setMinZoom(float minZoom) {
        this.minZoom = minZoom;
        return this;
    }

    /**
     * Setting max zoom level.
     * <p>
     * Default value is {@link #MAX_ZOOM}.
     *
     * @param maxZoom Max zoom level, or 0 to use zoom level which fits the image into the viewport.
     * @return Current settings object for calls chaining
     */
    public Settings setMaxZoom(float maxZoom) {
        this.maxZoom = maxZoom;
        return this;
    }

    /**
     * Setting double tap zoom level, should not be greater than {@link #getMaxZoom()}.
     * Defaults to {@link #getMaxZoom()} if &lt;= 0.
     * <p>
     * Default value is -1.
     *
     * @param doubleTapZoom Double tap zoom level
     * @return Current settings object for calls chaining
     */
    public Settings setDoubleTapZoom(float doubleTapZoom) {
        this.doubleTapZoom = doubleTapZoom;
        return this;
    }

    /**
     * Setting overzoom factor. User will be able to "over zoom" up to this factor.
     * Cannot be &lt; 1.
     * <p>
     * Default value is {@link #OVERZOOM_FACTOR}.
     *
     * @param factor Overzoom factor
     * @return Current settings object for calls chaining
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
     * up to this distance. Cannot be &lt; 0.
     * <p>
     * Default value is 0.
     *
     * @param distanceX Horizontal overscroll distance in pixels
     * @param distanceY Vertical overscroll distance in pixels
     * @return Current settings object for calls chaining
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
     *
     * @param context Context
     * @param distanceXDp Horizontal overscroll distance in dp
     * @param distanceYDp Vertical overscroll distance in dp
     * @return Current settings object for calls chaining
     */
    public Settings setOverscrollDistance(Context context, float distanceXDp, float distanceYDp) {
        return setOverscrollDistance(
                UnitsUtils.toPixels(context, distanceXDp),
                UnitsUtils.toPixels(context, distanceYDp));
    }

    /**
     * If set to true small images will be scaled to fit entire viewport (or entire movement area
     * if it was set) even if this will require zoom level above max zoom level. And big images
     * will be scaled to fit the viewport even if it will require zooming below min zoom.
     * <p>
     * Default value is false.
     *
     * @param isFitViewport Whether image should fit viewport or not
     * @return Current settings object for calls chaining
     */
    public Settings setFillViewport(boolean isFitViewport) {
        this.isFillViewport = isFitViewport;
        return this;
    }

    /**
     * Setting image gravity inside viewport area.
     * <p>
     * Default value is {@link android.view.Gravity#CENTER}.
     *
     * @param gravity Image gravity, one of {@link android.view.Gravity} constants
     * @return Current settings object for calls chaining
     */
    public Settings setGravity(int gravity) {
        this.gravity = gravity;
        return this;
    }

    /**
     * Setting image fitting method within viewport area.
     * <p>
     * Default value is {@link Settings.Fit#INSIDE}.
     *
     * @param fitMethod Fit method
     * @return Current settings object for calls chaining
     */
    public Settings setFitMethod(@NonNull Fit fitMethod) {
        this.fitMethod = fitMethod;
        return this;
    }

    /**
     * Setting movement bounds restriction type.
     * <p>
     * Default value is {@link Settings.Bounds#NORMAL}.
     *
     * @param boundsType Bounds restrictions type
     * @return Current settings object for calls chaining
     */
    public Settings setBoundsType(@NonNull Bounds boundsType) {
        this.boundsType = boundsType;
        return this;
    }

    /**
     * Sets whether panning is enabled or not.
     * <p>
     * Default value is true.
     *
     * @param enabled Whether panning should be enabled or not
     * @return Current settings object for calls chaining
     */
    public Settings setPanEnabled(boolean enabled) {
        isPanEnabled = enabled;
        return this;
    }

    /**
     * Sets whether fling (inertial motion after scroll) is enabled or not.
     * <p>
     * Default value is true.
     *
     * @param enabled Whether fling should be enabled or not
     * @return Current settings object for calls chaining
     */
    public Settings setFlingEnabled(boolean enabled) {
        isFlingEnabled = enabled;
        return this;
    }

    /**
     * Sets whether zooming is enabled or not.
     * <p>
     * Default value is true.
     *
     * @param enabled Whether zooming should be enabled or not
     * @return Current settings object for calls chaining
     */
    public Settings setZoomEnabled(boolean enabled) {
        isZoomEnabled = enabled;
        return this;
    }

    /**
     * Sets whether rotation gesture is enabled or not.
     * <p>
     * Default value is false.
     *
     * @param enabled Whether rotation should be enabled or not
     * @return Current settings object for calls chaining
     */
    public Settings setRotationEnabled(boolean enabled) {
        isRotationEnabled = enabled;
        return this;
    }

    /**
     * Sets whether image rotation should stick to 90 degrees intervals or can be free.
     * Only applied when {@link #isRestrictBounds()} is true as well.
     * <p>
     * Default value is false.
     *
     * @param restrict Whether rotation should be restricted or not
     * @return Current settings object for calls chaining
     */
    public Settings setRestrictRotation(boolean restrict) {
        isRestrictRotation = restrict;
        return this;
    }

    /**
     * Sets whether zooming by double tap is enabled or not.
     * <p>
     * Default value is true.
     *
     * @param enabled Whether double tap should be enabled or not
     * @return Current settings object for calls chaining
     */
    public Settings setDoubleTapEnabled(boolean enabled) {
        isDoubleTapEnabled = enabled;
        return this;
    }

    /**
     * Sets whether to detect and animate exit from gesture views.
     * <p>
     * Default value is true.
     *
     * @param enabled Whether exit gesture should be enabled or not
     * @return Current settings object for calls chaining
     */
    @SuppressWarnings("unused") // Public API
    public Settings setExitEnabled(boolean enabled) {
        exitType = enabled ? ExitType.ALL : ExitType.NONE;
        return this;
    }

    /**
     * Sets which gestures to use to detect exit.
     * <p>
     * Default value is {@link ExitType#ALL}.
     *
     * @param type Exit type
     * @return Current settings object for calls chaining
     */
    @SuppressWarnings("unused") // Public API
    public Settings setExitType(ExitType type) {
        exitType = type;
        return this;
    }

    /**
     * Disable all gestures.<br>
     * Calls to this method are counted, so if you called it N times
     * you should call {@link #enableGestures()} N times to re-enable all gestures.
     * <p>
     * Useful when you need to temporary disable touch gestures during animation or image loading.
     * <p>
     * See also {@link #enableGestures()}
     *
     * @return Current settings object for calls chaining
     */
    public Settings disableGestures() {
        gesturesDisableCount++;
        return this;
    }

    /**
     * Re-enable all gestures disabled by {@link #disableGestures()} method.<br>
     * Calls to this method are counted, so if you called {@link #disableGestures()} N times
     * you should call this method N times to re-enable all gestures.
     * <p>
     * See also {@link #disableGestures()}
     *
     * @return Current settings object for calls chaining
     */
    public Settings enableGestures() {
        gesturesDisableCount--;
        return this;
    }

    /**
     * Disable bounds restrictions.<br>
     * Calls to this method are counted, so if you called it N times
     * you should call {@link #enableBounds()} N times to re-enable bounds restrictions.
     * <p>
     * Useful when you need to temporary disable bounds restrictions during animation.
     * <p>
     * See also {@link #enableBounds()}
     *
     * @return Current settings object for calls chaining
     */
    public Settings disableBounds() {
        boundsDisableCount++;
        return this;
    }

    /**
     * Re-enable bounds restrictions disabled by {@link #disableBounds()} method.<br>
     * Calls to this method are counted, so if you called {@link #disableBounds()} N times
     * you should call this method N times to re-enable bounds restrictions.
     * <p>
     * See also {@link #disableBounds()}
     *
     * @return Current settings object for calls chaining
     */
    public Settings enableBounds() {
        boundsDisableCount--;
        return this;
    }

    /**
     * @param restrict Whether image bounds should be restricted or not
     * @return Current settings object for calls chaining
     * @deprecated Use {@link #disableBounds()} and {@link #enableBounds()} methods instead.
     */
    @SuppressWarnings("unused") // Public API
    @Deprecated
    public Settings setRestrictBounds(boolean restrict) {
        boundsDisableCount += restrict ? -1 : 1;
        if (boundsDisableCount < 0) { // In case someone explicitly used this method during setup
            boundsDisableCount = 0;
        }
        return this;
    }

    /**
     * Duration of animations.
     *
     * @param duration Animation duration in milliseconds
     * @return Current settings object for calls chaining
     */
    public Settings setAnimationsDuration(long duration) {
        if (duration < 0L) {
            throw new IllegalArgumentException("Animations duration should be >= 0");
        }
        animationsDuration = duration;
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

    public float getMinZoom() {
        return minZoom;
    }

    public float getMaxZoom() {
        return maxZoom;
    }

    public float getDoubleTapZoom() {
        return doubleTapZoom;
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

    public Bounds getBoundsType() {
        return boundsType;
    }

    public boolean isPanEnabled() {
        return isGesturesEnabled() && isPanEnabled;
    }

    public boolean isFlingEnabled() {
        return isGesturesEnabled() && isFlingEnabled;
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

    public boolean isExitEnabled() {
        return getExitType() != ExitType.NONE;
    }

    public ExitType getExitType() {
        return isGesturesEnabled() ? exitType : ExitType.NONE;
    }

    public boolean isGesturesEnabled() {
        return gesturesDisableCount <= 0;
    }

    public boolean isRestrictBounds() {
        return boundsDisableCount <= 0;
    }

    public long getAnimationsDuration() {
        return animationsDuration;
    }


    /**
     * @return Whether at least one of pan, zoom, rotation or double tap are enabled or not
     */
    public boolean isEnabled() {
        return isGesturesEnabled()
                && (isPanEnabled || isZoomEnabled || isRotationEnabled || isDoubleTapEnabled);
    }


    @SuppressWarnings("BooleanMethodIsAlwaysInverted") // Public API
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
        OUTSIDE,

        /**
         * Do not fit the image into viewport area.
         */
        NONE
    }

    public enum Bounds {
        /**
         * The image is moved within the movement area and always placed according to gravity
         * if less than the area.
         */
        NORMAL,

        /**
         * The image is moved within the movement area and can be freely moved inside the area
         * if less than the area.
         */
        INSIDE,

        /**
         * The image can be freely moved until it's completely outside of the movement area.
         */
        OUTSIDE,

        /**
         * The image can be freely moved until it contains a pivot point (e.g. center point if
         * the gravity is set to {@link Gravity#CENTER}).
         */
        PIVOT,

        /**
         * The image can be freely moved with no restrictions.
         */
        NONE
    }

    public enum ExitType {
        /**
         * To detect both scroll and zoom exit gestures.
         */
        ALL,

        /**
         * To detect only scroll-to-exit gesture.
         */
        SCROLL,

        /**
         * To detect only zoom-to-exit gesture.
         */
        ZOOM,

        /**
         * Do not detect exit gestures.
         */
        NONE
    }

}
