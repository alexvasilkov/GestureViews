package com.alexvasilkov.gestures;

import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.annotation.Nullable;

import com.alexvasilkov.gestures.internal.MovementBounds;
import com.alexvasilkov.gestures.internal.ZoomBounds;
import com.alexvasilkov.gestures.utils.GravityUtils;
import com.alexvasilkov.gestures.utils.MathUtils;

/**
 * Helper class that holds reference to {@link Settings} object and controls some aspects of view
 * {@link State}, such as movement bounds restrictions
 * (see {@link #getMovementArea(State, RectF)}) and dynamic min / max zoom levels
 * (see {@link #getMinZoom(State)} and {@link #getMaxZoom(State)}).
 */
public class StateController {

    // Temporary objects
    private static final State tmpState = new State();
    private static final Rect tmpRect = new Rect();
    private static final RectF tmpRectF = new RectF();
    private static final Point tmpPoint = new Point();
    private static final PointF tmpPointF = new PointF();


    private final Settings settings;
    private final ZoomBounds zoomBounds = new ZoomBounds();
    private final MovementBounds movBounds = new MovementBounds();

    private boolean isResetRequired = true;

    // FIXME: Remove next fields when getEffectiveMinZoom and getEffectiveMaxZoom are removed.
    private float minZoom;
    private float maxZoom;

    StateController(Settings settings) {
        this.settings = settings;
    }

    /**
     * Resets to initial state (min zoom, position according to gravity). Reset will only occur
     * when image and viewport sizes are known, otherwise reset will occur sometime in the future
     * when {@link #updateState(State)} method will be called.
     *
     * @param state State to be reset
     * @return {@code true} if reset was completed or {@code false} if reset is scheduled for future
     */
    boolean resetState(State state) {
        isResetRequired = true;
        return updateState(state);
    }

    /**
     * Updates state (or resets state if reset was scheduled, see {@link #resetState(State)}).
     *
     * @param state State to be updated
     * @return {@code true} if state was reset to initial state or {@code false} if state was
     * updated.
     */
    boolean updateState(State state) {
        if (isResetRequired) {
            // We can correctly reset state only when we have both image size and viewport size
            // but there can be a delay before we have all values properly set
            // (waiting for layout or waiting for image to be loaded)
            state.set(0f, 0f, 1f, 0f);

            ZoomBounds zoomBounds = getZoomBounds(state);
            isResetRequired = !zoomBounds.isReady();

            // Applying initial state
            state.set(0f, 0f, zoomBounds.getMinZoom(), 0f);
            GravityUtils.getImagePosition(state, settings, tmpRect);
            state.translateTo(tmpRect.left, tmpRect.top);

            return !isResetRequired;
        } else {
            // Restricts state's translation and zoom bounds, disallowing overscroll / overzoom.
            restrictStateBounds(state, state, Float.NaN, Float.NaN, false, false, true);
            return false;
        }
    }

    /**
     * Maximizes zoom if it closer to min zoom or minimizes it if it closer to max zoom.
     *
     * @param state Current state
     * @param pivotX Pivot's X coordinate
     * @param pivotY Pivot's Y coordinate
     * @return End state for toggle animation.
     */
    State toggleMinMaxZoom(State state, float pivotX, float pivotY) {
        final ZoomBounds zoomBounds = getZoomBounds(state);
        final float minZoom = zoomBounds.getMinZoom();
        final float maxZoom = settings.getDoubleTapZoom() > 0f
                ? settings.getDoubleTapZoom() : zoomBounds.getMaxZoom();

        final float middleZoom = 0.5f * (minZoom + maxZoom);
        final float targetZoom = state.getZoom() < middleZoom ? maxZoom : minZoom;

        final State end = state.copy();
        end.zoomTo(targetZoom, pivotX, pivotY);
        return end;
    }

    /**
     * Restricts state's translation and zoom bounds.
     *
     * @param state State to be restricted
     * @param prevState Previous state to calculate overscroll and overzoom (optional)
     * @param pivotX Pivot's X coordinate
     * @param pivotY Pivot's Y coordinate
     * @param allowOverscroll Whether overscroll is allowed
     * @param allowOverzoom Whether overzoom is allowed
     * @param restrictRotation Whether rotation should be restricted to a nearest N*90 angle
     * @return End state to animate changes or null if no changes are required.
     */
    @SuppressWarnings("SameParameterValue") // Using same method params as in restrictStateBounds
    @Nullable
    State restrictStateBoundsCopy(State state, State prevState, float pivotX, float pivotY,
            boolean allowOverscroll, boolean allowOverzoom, boolean restrictRotation) {
        tmpState.set(state);
        boolean changed = restrictStateBounds(tmpState, prevState, pivotX, pivotY,
                allowOverscroll, allowOverzoom, restrictRotation);
        return changed ? tmpState.copy() : null;
    }

    /**
     * Restricts state's translation and zoom bounds. If {@code prevState} is not null and
     * {@code allowOverscroll (allowOverzoom)} parameter is true then resilience
     * will be applied to translation (zoom) changes if they are out of bounds.
     *
     * @param state State to be restricted
     * @param prevState Previous state to calculate overscroll and overzoom (optional)
     * @param pivotX Pivot's X coordinate
     * @param pivotY Pivot's Y coordinate
     * @param allowOverscroll Whether overscroll is allowed
     * @param allowOverzoom Whether overzoom is allowed
     * @param restrictRotation Whether rotation should be restricted to a nearest N*90 angle
     * @return true if state was changed, false otherwise.
     */
    boolean restrictStateBounds(State state, State prevState, float pivotX, float pivotY,
            boolean allowOverscroll, boolean allowOverzoom, boolean restrictRotation) {

        if (!settings.isRestrictBounds()) {
            return false;
        }

        // Calculating default pivot point, if not provided
        if (Float.isNaN(pivotX) || Float.isNaN(pivotY)) {
            GravityUtils.getDefaultPivot(settings, tmpPoint);
            pivotX = tmpPoint.x;
            pivotY = tmpPoint.y;
        }

        boolean isStateChanged = false;

        if (restrictRotation && settings.isRestrictRotation()) {
            float rotation = Math.round(state.getRotation() / 90f) * 90f;
            if (!State.equals(rotation, state.getRotation())) {
                state.rotateTo(rotation, pivotX, pivotY);
                isStateChanged = true;
            }
        }

        ZoomBounds zoomBounds = getZoomBounds(state);
        final float minZoom = zoomBounds.getMinZoom();

        final float extraZoom = allowOverzoom ? settings.getOverzoomFactor() : 1f;
        float zoom = zoomBounds.restrict(state.getZoom(), extraZoom);

        // Applying elastic overzoom
        if (prevState != null) {
            zoom = applyZoomResilience(zoom, prevState.getZoom(), extraZoom);
        }

        if (!State.equals(zoom, state.getZoom())) {
            state.zoomTo(zoom, pivotX, pivotY);
            isStateChanged = true;
        }

        MovementBounds bounds = getMovementBounds(state);
        float extraX = allowOverscroll ? settings.getOverscrollDistanceX() : 0f;
        float extraY = allowOverscroll ? settings.getOverscrollDistanceY() : 0f;

        bounds.restrict(state.getX(), state.getY(), extraX, extraY, tmpPointF);
        float newX = tmpPointF.x;
        float newY = tmpPointF.y;

        if (zoom < minZoom) {
            // Decreasing overscroll if zooming less than minimum zoom
            float factor = (extraZoom * zoom / minZoom - 1f) / (extraZoom - 1f);
            factor = (float) Math.sqrt(factor);

            bounds.restrict(newX, newY, tmpPointF);
            float strictX = tmpPointF.x;
            float strictY = tmpPointF.y;

            newX = strictX + factor * (newX - strictX);
            newY = strictY + factor * (newY - strictY);
        }

        if (prevState != null) {
            bounds.getExternalBounds(tmpRectF);
            newX = applyTranslationResilience(newX, prevState.getX(),
                    tmpRectF.left, tmpRectF.right, extraX);
            newY = applyTranslationResilience(newY, prevState.getY(),
                    tmpRectF.top, tmpRectF.bottom, extraY);
        }

        if (!State.equals(newX, state.getX()) || !State.equals(newY, state.getY())) {
            state.translateTo(newX, newY);
            isStateChanged = true;
        }

        return isStateChanged;
    }

    private float applyZoomResilience(float zoom, float prevZoom, float overzoom) {
        if (overzoom == 1f) {
            return zoom;
        }

        float minZoom = this.minZoom / overzoom;
        float maxZoom = this.maxZoom * overzoom;

        float resilience = 0f;

        if (zoom < this.minZoom && zoom < prevZoom) {
            resilience = (this.minZoom - zoom) / (this.minZoom - minZoom);
        } else if (zoom > this.maxZoom && zoom > prevZoom) {
            resilience = (zoom - this.maxZoom) / (maxZoom - this.maxZoom);
        }

        if (resilience == 0f) {
            return zoom;
        } else {
            resilience = (float) Math.sqrt(resilience);
            return zoom + resilience * (prevZoom - zoom);
        }
    }

    private float applyTranslationResilience(float value, float prevValue,
            float boundsMin, float boundsMax, float overscroll) {
        if (overscroll == 0f) {
            return value;
        }

        float resilience = 0f;

        float avg = (value + prevValue) * 0.5f;

        if (avg < boundsMin && value < prevValue) {
            resilience = (boundsMin - avg) / overscroll;
        } else if (avg > boundsMax && value > prevValue) {
            resilience = (avg - boundsMax) / overscroll;
        }

        if (resilience == 0f) {
            return value;
        } else {
            if (resilience > 1f) {
                resilience = 1f;
            }
            resilience = (float) Math.sqrt(resilience);
            return value - resilience * (value - prevValue);
        }
    }


    private ZoomBounds getZoomBounds(State state) {
        zoomBounds.setup(state, settings);

        // FIXME: Remove next lines when getEffectiveMinZoom and getEffectiveMaxZoom are removed.
        minZoom = zoomBounds.getMinZoom();
        maxZoom = zoomBounds.getMaxZoom();

        return zoomBounds;
    }

    private MovementBounds getMovementBounds(State state) {
        movBounds.setup(state, settings);
        return movBounds;
    }


    /**
     * @param state Current state
     * @return Min zoom level as it's used by state controller.
     */
    public float getMinZoom(State state) {
        return getZoomBounds(state).getMinZoom();
    }

    /**
     * @param state Current state
     * @return Max zoom level as it's used by state controller.
     * Note, that it may be different from {@link Settings#getMaxZoom()}.
     */
    @SuppressWarnings({ "unused", "WeakerAccess" }) // Public API
    public float getMaxZoom(State state) {
        return getZoomBounds(state).getMaxZoom();
    }

    /**
     * Calculates area in which {@link State#getX()} &amp; {@link State#getY()} values can change.
     * Note, that this is different from {@link Settings#setMovementArea(int, int)} which defines
     * part of the viewport in which image can move.
     *
     * @param state Current state
     * @param out Output movement area rectangle
     */
    public void getMovementArea(State state, RectF out) {
        getMovementBounds(state).getExternalBounds(out);
    }


    /*
     * Deprecated methods.
     */

    /**
     * @return Min zoom level
     * @deprecated Use {@link #getMinZoom(State)} instead.
     */
    @SuppressWarnings("unused") // Public API
    @Deprecated
    public float getEffectiveMinZoom() {
        return minZoom;
    }

    /**
     * @return Max zoom level
     * @deprecated Use {@link #getMaxZoom(State)} instead.
     */
    @SuppressWarnings("unused") // Public API
    @Deprecated
    public float getEffectiveMaxZoom() {
        return maxZoom;
    }

    /**
     * @param out Output movement area rectangle
     * @param state Current state
     * @deprecated User {@link #getMovementArea(State, RectF)} instead.
     */
    @SuppressWarnings("unused") // Public API
    @Deprecated
    public void getEffectiveMovementArea(RectF out, State state) {
        getMovementArea(state, out);
    }

    /**
     * @param value Value to be restricted
     * @param minValue Min value
     * @param maxValue Max value
     * @return Restricted value
     * @deprecated Use {@link MathUtils#restrict(float, float, float)}.
     */
    @SuppressWarnings("unused") // Public API
    @Deprecated
    public static float restrict(float value, float minValue, float maxValue) {
        return Math.max(minValue, Math.min(value, maxValue));
    }

    /**
     * @param out Interpolated state (output)
     * @param start Start state
     * @param end End state
     * @param factor Factor
     * @deprecated Use {@link MathUtils#interpolate(State, State, State, float)}.
     */
    @SuppressWarnings("unused") // Public API
    @Deprecated
    public static void interpolate(State out, State start, State end, float factor) {
        MathUtils.interpolate(out, start, end, factor);
    }

    /**
     * @param out Interpolated state (output)
     * @param start Start state
     * @param startPivotX Pivot point's X coordinate in start state coordinates
     * @param startPivotY Pivot point's Y coordinate in start state coordinates
     * @param end End state
     * @param endPivotX Pivot point's X coordinate in end state coordinates
     * @param endPivotY Pivot point's Y coordinate in end state coordinates
     * @param factor Factor
     * @deprecated Use
     * {@link MathUtils#interpolate(State, State, float, float, State, float, float, float)}.
     */
    @SuppressWarnings("unused") // Public API
    @Deprecated
    public static void interpolate(State out, State start, float startPivotX, float startPivotY,
            State end, float endPivotX, float endPivotY, float factor) {
        MathUtils.interpolate(out, start, startPivotX, startPivotY,
                end, endPivotX, endPivotY, factor);
    }

    /**
     * @param start Start value
     * @param end End value
     * @param factor Factor
     * @return Interpolated value
     * @deprecated Use {@link MathUtils#interpolate(float, float, float)}.
     */
    @SuppressWarnings("unused") // Public API
    @Deprecated
    public static float interpolate(float start, float end, float factor) {
        return MathUtils.interpolate(start, end, factor);
    }

    /**
     * @param out Interpolated rectangle (output)
     * @param start Start rectangle
     * @param end End rectangle
     * @param factor Factor
     * @deprecated Use {@link MathUtils#interpolate(RectF, RectF, RectF, float)},
     */
    @SuppressWarnings("unused") // Public API
    @Deprecated
    public static void interpolate(RectF out, RectF start, RectF end, float factor) {
        MathUtils.interpolate(out, start, end, factor);
    }

}
