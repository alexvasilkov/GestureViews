package com.alexvasilkov.gestures;

import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.RectF;

import com.alexvasilkov.gestures.utils.MovementBounds;

public class StateController {

    private final Settings mSettings;

    // Temporary objects
    private final State mTmpState = new State();
    private final Matrix mMatrix = new Matrix();
    private final RectF mRectF = new RectF();
    private final MovementBounds mMovementBounds = new MovementBounds();

    private boolean mIsResetRequired = true;

    /**
     * Values to store calculated values for min / max zoom levels
     */
    private float mMinZoom, mMaxZoom;

    public StateController(Settings settings) {
        mSettings = settings;
    }

    /**
     * Resets to initial state (min zoom, position according to gravity)
     */
    void resetState(State state) {
        mIsResetRequired = true;
        updateState(state);
    }

    void updateState(State state) {
        if (mIsResetRequired) {
            // We can correctly reset state only when we have both image size and viewport size
            // but there can be a delay before we have all values properly set
            // (waiting for layout or waiting for image to be loaded)
            boolean updated = adjustZoomLevels(state);

            state.set(0f, 0f, mMinZoom, 0f);
            MovementBounds.setupInitialMovement(state, mSettings);

            mIsResetRequired = !updated;
        } else {
            restrictStateBounds(state);
        }
    }

    public float getMinZoom() {
        return mMinZoom;
    }

    public float getMaxZoom() {
        return mMaxZoom;
    }

    /**
     * Maximizes zoom if it closer to min zoom or minimizes it if it closer to max zoom
     *
     * @return End state for toggle animation
     */
    public State toggleMinMaxZoom(State state, float pivotX, float pivotY) {
        final float middleZoom = (mMinZoom + mMaxZoom) / 2f;
        final float targetZoom = state.getZoom() < middleZoom ? mMaxZoom : mMinZoom;

        State end = state.copy();
        end.zoomTo(targetZoom, pivotX, pivotY);
        return end;
    }

    /**
     * Restricts state's translation and zoom bounds, disallowing overscroll / overzoom.
     */
    public boolean restrictStateBounds(State state) {
        return restrictStateBounds(state, null, 0f, 0f, false, false);
    }

    /**
     * Restricts state's translation and zoom bounds.
     *
     * @return End state to animate changes or null if no changes are required
     */
    public State restrictStateBoundsCopy(State state, float pivotX, float pivotY,
                                         boolean allowOverscroll, boolean allowOverzoom) {
        mTmpState.set(state);
        boolean changed = restrictStateBounds(mTmpState, null, pivotX, pivotY,
                allowOverscroll, allowOverzoom);
        return changed ? mTmpState.copy() : null;
    }

    /**
     * Restricts state's translation and zoom bounds. If {@code prevState} is not null and
     * {@code allowOverscroll (allowOverzoom)} parameter is true than resilience
     * will be applied to translation (zoom) changes if they are out of bounds.
     *
     * @return true if state was changed, false otherwise
     */
    public boolean restrictStateBounds(State state, State prevState, float pivotX, float pivotY,
                                       boolean allowOverscroll, boolean allowOverzoom) {

        if (!mSettings.isRestrictBounds()) return false;

        if (prevState != null && !State.equals(state.getRotation(), prevState.getRotation())) {
            // Rotation will change image bounds, so we should adjust zoom levels
            adjustZoomLevels(state);
        }

        boolean isStateChanged = false;

        float overzoom = allowOverzoom ? mSettings.getOverzoomFactor() : 1f;

        float zoom = restrict(state.getZoom(), mMinZoom / overzoom, mMaxZoom * overzoom);

        // Applying elastic overzoom
        if (prevState != null) {
            zoom = applyZoomResilience(zoom, prevState.getZoom(), overzoom);
        }

        if (!State.equals(zoom, state.getZoom())) {
            state.zoomTo(zoom, pivotX, pivotY);
            isStateChanged = true;
        }

        MovementBounds bounds = getMovementBounds(state);
        float overscrollX = allowOverscroll ? mSettings.getOverscrollDistanceX() : 0f;
        float overscrollY = allowOverscroll ? mSettings.getOverscrollDistanceY() : 0f;

        PointF tmpPos = bounds.restrict(state.getX(), state.getY(), overscrollX, overscrollY);
        float x = tmpPos.x;
        float y = tmpPos.y;

        if (zoom < mMinZoom) {
            // Decreasing overscroll if zooming less than minimum zoom
            float minZoom = mMinZoom / overzoom;
            float factor = (zoom - minZoom) / (mMinZoom - minZoom);
            factor = (float) Math.sqrt(factor);

            tmpPos = bounds.restrict(x, y);
            float strictX = tmpPos.x;
            float strictY = tmpPos.y;

            x = strictX + factor * (x - strictX);
            y = strictY + factor * (y - strictY);
        }

        if (prevState != null) {
            RectF extBounds = bounds.getExternalBounds();
            x = applyTranslationResilience(x, prevState.getX(),
                    extBounds.left, extBounds.right, overscrollX);
            y = applyTranslationResilience(y, prevState.getY(),
                    extBounds.top, extBounds.bottom, overscrollY);
        }

        if (!State.equals(x, state.getX()) || !State.equals(y, state.getY())) {
            state.translateTo(x, y);
            isStateChanged = true;
        }

        return isStateChanged;
    }

    private float applyZoomResilience(float zoom, float prevZoom, float overzoom) {
        if (overzoom == 1f) return zoom;

        float minZoom = mMinZoom / overzoom;
        float maxZoom = mMaxZoom * overzoom;

        float resilience = 0f;

        if (zoom < mMinZoom && zoom < prevZoom) {
            resilience = (mMinZoom - zoom) / (mMinZoom - minZoom);
        } else if (zoom > mMaxZoom && zoom > prevZoom) {
            resilience = (zoom - mMaxZoom) / (maxZoom - mMaxZoom);
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
        if (overscroll == 0) return value;

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
            if (resilience > 1f) resilience = 1f;
            float delta = value - prevValue;
            delta *= (1f - (float) Math.sqrt(resilience));
            return prevValue + delta;
        }
    }


    /**
     * Do note store returned object, since it will be reused next time this method is called.
     */
    public MovementBounds getMovementBounds(State state) {
        mMovementBounds.setup(state, mSettings);
        return mMovementBounds;
    }

    /**
     * Adjusting min and max zoom levels.
     *
     * @return true if zoom levels was correctly updated (image and viewport sizes are known),
     * false otherwise
     */
    private boolean adjustZoomLevels(State state) {
        mMaxZoom = mSettings.getMaxZoom();

        float fittingZoom = 1f;

        boolean isCorrectSize = mSettings.hasImageSize() && mSettings.hasViewportSize();

        if (isCorrectSize) {
            float w = mSettings.getImageW(), h = mSettings.getImageH();
            float areaW = mSettings.getMovementAreaW(), areaH = mSettings.getMovementAreaH();

            if (mSettings.getFitMethod() == Settings.Fit.OUTSIDE) {
                // Computing movement area size taking rotation into account. We need to inverse
                // rotation, since it will be applied to the area, not to the image itself.
                mMatrix.setRotate(-state.getRotation());
                mRectF.set(0, 0, areaW, areaH);
                mMatrix.mapRect(mRectF);
                areaW = mRectF.width();
                areaH = mRectF.height();
            } else {
                // Computing image bounding size taking rotation into account.
                mMatrix.setRotate(state.getRotation());
                mRectF.set(0, 0, w, h);
                mMatrix.mapRect(mRectF);
                w = mRectF.width();
                h = mRectF.height();
            }

            switch (mSettings.getFitMethod()) {
                case HORIZONTAL:
                    fittingZoom = areaW / w;
                    break;
                case VERTICAL:
                    fittingZoom = areaH / h;
                    break;
                case OUTSIDE:
                    fittingZoom = Math.max(areaW / w, areaH / h);
                    break;
                case INSIDE:
                default:
                    fittingZoom = Math.min(areaW / w, areaH / h);
                    break;
            }
        }

        if (fittingZoom > mMaxZoom) {
            if (mSettings.isFillViewport()) {
                // zooming to fill entire viewport
                mMinZoom = mMaxZoom = fittingZoom;
            } else {
                // restricting min zoom
                mMinZoom = mMaxZoom;
            }
        } else {
            mMinZoom = fittingZoom;
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
        float x = interpolate(start.getX(), end.getX(), factor);
        float y = interpolate(start.getY(), end.getY(), factor);
        float zoom = interpolate(start.getZoom(), end.getZoom(), factor);
        float rotation = interpolate(start.getRotation(), end.getRotation(), factor);
        out.set(x, y, zoom, rotation);
    }

    public static float interpolate(float start, float end, float factor) {
        return start + (end - start) * factor;
    }

}
