package com.alexvasilkov.gestures;

import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.RectF;
import android.support.annotation.Nullable;

import com.alexvasilkov.gestures.internal.MovementBounds;

/**
 * Helper class that holds reference to {@link Settings} object and controls some aspects of view
 * {@link State}, such as movement bounds restrictions
 * (see {@link #getEffectiveMovementArea(RectF, State)}) and dynamic min / max zoom levels
 * (see {@link #getEffectiveMinZoom()} and {@link #getEffectiveMaxZoom()}).
 * <p/>
 * It also provides few static methods that can be useful:
 * <ul>
 * <li>{@link #restrict(float, float, float) restrict()} float value,</li>
 * <li>{@link #interpolate(float, float, float) interpolate()} float value</li>
 * <li>{@link #interpolate(State, State, State, float) interpolate()} {@link State} object</li>
 * <li>{@link #interpolate(State, State, float, float, State, float, float, float) interpolate()}
 * {@link State} object with specified pivot point</li>
 * </ul>
 */
public class StateController {

    // Temporary objects
    private static final State tmpState = new State();
    private static final Matrix tmpMatrix = new Matrix();
    private static final RectF tmpRect = new RectF();
    private static final MovementBounds tmpMovBounds = new MovementBounds();

    private final Settings settings;

    private boolean isResetRequired = true;

    // Values to store calculated values for min / max zoom levels
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
     * @return {@code true} if reset was completed or {@code false} if reset is scheduled for future
     */
    boolean resetState(State state) {
        isResetRequired = true;
        return updateState(state);
    }

    /**
     * Updates state (or resets state if reset was scheduled, see {@link #resetState(State)}).
     *
     * @return {@code true} if state was reset to initial state or {@code false} if state was
     * updated.
     */
    boolean updateState(State state) {
        if (isResetRequired) {
            // We can correctly reset state only when we have both image size and viewport size
            // but there can be a delay before we have all values properly set
            // (waiting for layout or waiting for image to be loaded)
            state.set(0f, 0f, 1f, 0f);
            boolean updated = adjustZoomLevels(state);
            state.set(0f, 0f, minZoom, 0f);
            MovementBounds.setupInitialMovement(state, settings);

            isResetRequired = !updated;
            return !isResetRequired;
        } else {
            restrictStateBounds(state);
            return false;
        }
    }

    /**
     * @return Min zoom level as it's used by state controller.
     */
    public float getEffectiveMinZoom() {
        return minZoom;
    }

    /**
     * @return Max zoom level as it's used by state controller.
     * Note, that it may be different than {@link Settings#getMaxZoom()}.
     */
    @SuppressWarnings("unused") // Public API
    public float getEffectiveMaxZoom() {
        return maxZoom;
    }

    /**
     * Maximizes zoom if it closer to min zoom or minimizes it if it closer to max zoom.
     *
     * @return End state for toggle animation.
     */
    State toggleMinMaxZoom(State state, float pivotX, float pivotY) {
        adjustZoomLevels(state); // Calculating zoom levels

        final float middleZoom = (minZoom + maxZoom) / 2f;
        final float targetZoom = state.getZoom() < middleZoom ? maxZoom : minZoom;

        State end = state.copy();
        end.zoomTo(targetZoom, pivotX, pivotY);
        return end;
    }

    /**
     * Restricts state's translation and zoom bounds.
     *
     * @return End state to animate changes or null if no changes are required.
     */
    @Nullable
    State restrictStateBoundsCopy(State state, State prevState, float pivotX, float pivotY,
            boolean allowOverscroll, boolean allowOverzoom, boolean restrictRotation) {
        tmpState.set(state);
        boolean changed = restrictStateBounds(tmpState, prevState, pivotX, pivotY,
                allowOverscroll, allowOverzoom, restrictRotation);
        return changed ? tmpState.copy() : null;
    }

    /**
     * Restricts state's translation and zoom bounds, disallowing overscroll / overzoom.
     */
    boolean restrictStateBounds(State state) {
        return restrictStateBounds(state, null, Float.NaN, Float.NaN, false, false, true);
    }

    /**
     * Restricts state's translation and zoom bounds. If {@code prevState} is not null and
     * {@code allowOverscroll (allowOverzoom)} parameter is true than resilience
     * will be applied to translation (zoom) changes if they are out of bounds.
     *
     * @return true if state was changed, false otherwise.
     */
    boolean restrictStateBounds(State state, State prevState, float pivotX, float pivotY,
            boolean allowOverscroll, boolean allowOverzoom, boolean restrictRotation) {

        if (!settings.isRestrictBounds()) {
            return false;
        }

        // Calculating default pivot point, if not provided
        if (Float.isNaN(pivotX) || Float.isNaN(pivotY)) {
            Point pivot = MovementBounds.getDefaultPivot(settings);
            pivotX = pivot.x;
            pivotY = pivot.y;
        }

        boolean isStateChanged = false;

        if (restrictRotation && settings.isRestrictRotation()) {
            float rotation = Math.round(state.getRotation() / 90f) * 90f;
            if (!State.equals(rotation, state.getRotation())) {
                state.rotateTo(rotation, pivotX, pivotY);
                isStateChanged = true;
            }
        }

        adjustZoomLevels(state); // Calculating zoom levels

        float overzoom = allowOverzoom ? settings.getOverzoomFactor() : 1f;

        float zoom = restrict(state.getZoom(), minZoom / overzoom, maxZoom * overzoom);

        // Applying elastic overzoom
        if (prevState != null) {
            zoom = applyZoomResilience(zoom, prevState.getZoom(), overzoom);
        }

        if (!State.equals(zoom, state.getZoom())) {
            state.zoomTo(zoom, pivotX, pivotY);
            isStateChanged = true;
        }

        MovementBounds bounds = getMovementBounds(state);
        float overscrollX = allowOverscroll ? settings.getOverscrollDistanceX() : 0f;
        float overscrollY = allowOverscroll ? settings.getOverscrollDistanceY() : 0f;

        PointF tmpPos = bounds.restrict(state.getX(), state.getY(), overscrollX, overscrollY);
        float newX = tmpPos.x;
        float newY = tmpPos.y;

        if (zoom < minZoom) {
            // Decreasing overscroll if zooming less than minimum zoom
            float minZoom = this.minZoom / overzoom;
            float factor = (zoom - minZoom) / (this.minZoom - minZoom);
            factor = (float) Math.sqrt(factor);

            tmpPos = bounds.restrict(newX, newY);
            float strictX = tmpPos.x;
            float strictY = tmpPos.y;

            newX = strictX + factor * (newX - strictX);
            newY = strictY + factor * (newY - strictY);
        }

        if (prevState != null) {
            RectF extBounds = bounds.getExternalBounds();
            newX = applyTranslationResilience(newX, prevState.getX(),
                    extBounds.left, extBounds.right, overscrollX);
            newY = applyTranslationResilience(newY, prevState.getY(),
                    extBounds.top, extBounds.bottom, overscrollY);
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
            float factor = zoom / prevZoom;
            factor += (float) Math.sqrt(resilience) * (1f - factor);
            return prevZoom * factor;
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
            float delta = value - prevValue;
            delta *= (1f - (float) Math.sqrt(resilience));
            return prevValue + delta;
        }
    }


    /**
     * Do note store returned object, since it will be reused next time this method is called.
     */
    MovementBounds getMovementBounds(State state) {
        tmpMovBounds.setup(state, settings);
        return tmpMovBounds;
    }


    /**
     * Returns area in which {@link State#getX()} & {@link State#getY()} values can change.
     * Note, that this is different than {@link Settings#setMovementArea(int, int)} which defines
     * part of the viewport in which image can move.
     *
     * @param out Result will be stored in this rect.
     * @param state State for which to calculate bounds.
     */
    @SuppressWarnings("unused") // Public API
    public void getEffectiveMovementArea(RectF out, State state) {
        out.set(getMovementBounds(state).getExternalBounds());
    }

    /**
     * Adjusting min and max zoom levels.
     *
     * @return true if zoom levels was correctly updated (image and viewport sizes are known),
     * false otherwise
     */
    private boolean adjustZoomLevels(State state) {
        maxZoom = settings.getMaxZoom();

        float fittingZoom = 1f;

        boolean isCorrectSize = settings.hasImageSize() && settings.hasViewportSize();

        if (isCorrectSize) {
            float imageWidth = settings.getImageW();
            float imageHeight = settings.getImageH();

            float areaWidth = settings.getMovementAreaW();
            float areaHeight = settings.getMovementAreaH();

            if (settings.getFitMethod() == Settings.Fit.OUTSIDE) {
                // Computing movement area size taking rotation into account. We need to inverse
                // rotation, since it will be applied to the area, not to the image itself.
                tmpMatrix.setRotate(-state.getRotation());
                tmpRect.set(0, 0, areaWidth, areaHeight);
                tmpMatrix.mapRect(tmpRect);
                areaWidth = tmpRect.width();
                areaHeight = tmpRect.height();
            } else {
                // Computing image bounding size taking rotation into account.
                tmpMatrix.setRotate(state.getRotation());
                tmpRect.set(0, 0, imageWidth, imageHeight);
                tmpMatrix.mapRect(tmpRect);
                imageWidth = tmpRect.width();
                imageHeight = tmpRect.height();
            }

            switch (settings.getFitMethod()) {
                case HORIZONTAL:
                    fittingZoom = areaWidth / imageWidth;
                    break;
                case VERTICAL:
                    fittingZoom = areaHeight / imageHeight;
                    break;
                case OUTSIDE:
                    fittingZoom = Math.max(areaWidth / imageWidth, areaHeight / imageHeight);
                    break;
                case INSIDE:
                default:
                    fittingZoom = Math.min(areaWidth / imageWidth, areaHeight / imageHeight);
                    break;
            }
        }

        if (fittingZoom > maxZoom) {
            if (settings.isFillViewport()) {
                // zooming to fill entire viewport
                minZoom = maxZoom = fittingZoom;
            } else {
                // restricting min zoom
                minZoom = maxZoom;
            }
        } else {
            minZoom = fittingZoom;
            if (!settings.isZoomEnabled()) {
                maxZoom = minZoom;
            }
        }

        return isCorrectSize;
    }

    public static float restrict(float value, float minValue, float maxValue) {
        return Math.max(minValue, Math.min(value, maxValue));
    }

    /**
     * Interpolates from start state to end state by given factor (from 0 to 1),
     * storing result into out state.
     */
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

    public static float interpolate(float start, float end, float factor) {
        return start + (end - start) * factor;
    }

}
