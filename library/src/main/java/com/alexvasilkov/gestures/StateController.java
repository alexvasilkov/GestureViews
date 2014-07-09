package com.alexvasilkov.gestures;

import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.Gravity;

public class StateController {

    private final Settings mSettings;

    // Temporary objects
    private final State mTmpState = new State();
    private final Matrix mMatrix = new Matrix();
    private final RectF mRectF = new RectF();
    private final float[] mPointF = new float[2];
    private final Rect mRectContainer = new Rect(), mRectOut = new Rect();
    private final Rect mMovingBounds = new Rect();

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
    public void resetState(State state) {
        mIsResetRequired = true;
        updateState(state);
    }

    public void updateState(State state) {
        if (mIsResetRequired) {
            // We can correctly reset state only when we have both view size and viewport size
            // but there can be a delay before we have all values properly set
            // (waiting for layout or waiting for image to be loaded)
            boolean updated = adjustZoomLevels(state);
            applyInitialState(state);
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

    private void applyInitialState(State state) {
        state.set(0f, 0f, mMinZoom, 0f);
        Rect pos = getPositionWithGravity(state);
        state.translateTo(pos.left, pos.top);
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
        boolean changed = restrictStateBounds(mTmpState, null, pivotX, pivotY, allowOverscroll, allowOverzoom);
        return changed ? mTmpState.copy() : null;
    }

    /**
     * Restricts state's translation and zoom bounds. If {@code prevState} is not null and
     * {@code allowOverscroll (allowOverzoom)} parameter is true than resilience will be applied to translation (zoom)
     * changes if they are out of bounds.
     *
     * @return true if state was changed, false otherwise
     */
    public boolean restrictStateBounds(State state, State prevState, float pivotX, float pivotY,
                                       boolean allowOverscroll, boolean allowOverzoom) {

        if (!mSettings.isRestrictBounds()) return false;

        if (prevState != null && !State.equals(state.getRotation(), prevState.getRotation())) {
            // Rotation will change view bounds, so we should adjust zoom levels
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

        Rect bounds = getMovingBounds(state);
        float overscrollX = allowOverscroll ? mSettings.getOverscrollDistanceX() : 0f;
        float overscrollY = allowOverscroll ? mSettings.getOverscrollDistanceY() : 0f;

        float x = restrict(state.getX(), bounds.left - overscrollX, bounds.right + overscrollX);
        float y = restrict(state.getY(), bounds.top - overscrollY, bounds.bottom + overscrollY);

        if (zoom < mMinZoom) {
            // Decreasing overscroll if zooming less than minimum zoom
            float minZoom = mMinZoom / overzoom;
            float factor = (zoom - minZoom) / (mMinZoom - minZoom);
            factor = (float) Math.sqrt(factor);

            float strictX = restrict(x, bounds.left, bounds.right);
            float strictY = restrict(y, bounds.top, bounds.bottom);

            x = strictX + factor * (x - strictX);
            y = strictY + factor * (y - strictY);
        }

        if (prevState != null) {
            x = applyTranslationResilience(x, prevState.getX(), bounds.left, bounds.right, overscrollX);
            y = applyTranslationResilience(y, prevState.getY(), bounds.top, bounds.bottom, overscrollY);
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
     * Calculating bounds for {@link State#x} & {@link State#y} values to keep view inside viewport
     * and taking into account view's gravity (see {@link Settings#setGravity(int)})
     * <p/>
     * Do note store returned rectangle, since it will be reused next time this method is called.
     */
    public Rect getMovingBounds(State state) {
        final Rect pos = getPositionWithGravity(state);

        // Calculating moving bounds for top-left corner of the scaled view

        // horizontal bounds
        if (mSettings.getViewportW() < pos.width()) {
            // view is bigger that viewport -> restricting view movement with viewport bounds
            mMovingBounds.left = mSettings.getViewportW() - pos.width();
            mMovingBounds.right = 0;
        } else {
            // view is smaller than viewport -> positioning view according to calculated gravity
            // and restricting view movement in this direction
            mMovingBounds.left = mMovingBounds.right = pos.left;
        }

        // vertical bounds
        if (mSettings.getViewportH() < pos.height()) {
            // view is bigger that viewport -> restricting view movement with viewport bounds
            mMovingBounds.top = mSettings.getViewportH() - pos.height();
            mMovingBounds.bottom = 0;
        } else {
            // view is smaller than viewport -> positioning view according to calculated gravity
            // and restricting view movement in this direction
            mMovingBounds.top = mMovingBounds.bottom = pos.top;
        }

        // We should also take rotation into account
        state.get(mMatrix);

        mRectF.set(0, 0, mSettings.getViewW(), mSettings.getViewH());
        mMatrix.mapRect(mRectF);

        mPointF[0] = 0;
        mPointF[1] = 0;
        mMatrix.mapPoints(mPointF);

        mMovingBounds.offset(Math.round(mPointF[0] - mRectF.left), Math.round(mPointF[1] - mRectF.top));

        return mMovingBounds;
    }

    /**
     * Returns view position within the viewport area with gravity applied, not taking into account view position
     * (specified with {@link com.alexvasilkov.gestures.State#x} & {@link com.alexvasilkov.gestures.State#y}).
     * <p/>
     * Do note store returned rectangle, since it will be reused next time this method is called.
     */
    private Rect getPositionWithGravity(State state) {
        state.get(mMatrix);
        mRectF.set(0, 0, mSettings.getViewW(), mSettings.getViewH());
        mMatrix.mapRect(mRectF);
        final int w = Math.round(mRectF.width());
        final int h = Math.round(mRectF.height());

        // Calculating view position basing on gravity
        mRectContainer.set(0, 0, mSettings.getViewportW(), mSettings.getViewportH());
        Gravity.apply(mSettings.getGravity(), w, h, mRectContainer, mRectOut);

        return mRectOut;
    }

    /**
     * Adjusting min and max zoom levels.
     *
     * @return true if zoom levels was correctly updated (viewport and view size are known), false otherwise
     */
    private boolean adjustZoomLevels(State state) {
        mMaxZoom = mSettings.getMaxZoom();

        // Computing bounds taking rotation into account
        mMatrix.reset();
        mMatrix.postRotate(state.getRotation());
        mRectF.set(0, 0, mSettings.getViewW(), mSettings.getViewH());
        mMatrix.mapRect(mRectF);

        final float w = mRectF.width(), h = mRectF.height();
        final float vpW = mSettings.getViewportW(), vpH = mSettings.getViewportH();
        boolean isCorrectSize = w != 0 && h != 0 && vpW != 0 && vpH != 0;

        float fittingZoom;
        if (isCorrectSize) {
            switch (mSettings.getFitMethod()) {
                case HORIZONTAL:
                    fittingZoom = vpW / w;
                    break;
                case VERTICAL:
                    fittingZoom = vpH / h;
                    break;
                case OUTSIDE:
                    fittingZoom = Math.max(vpW / w, vpH / h);
                    break;
                case INSIDE:
                default:
                    fittingZoom = Math.min(vpW / w, vpH / h);
                    break;
            }
        } else {
            fittingZoom = 1f;
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
     * Interpolates from start state to end state by given factor (from 0 to 1), storing result into out state.
     */
    public static void interpolate(State out, State start, State end, float factor) {
        float x = interpolate(start.getX(), end.getX(), factor);
        float y = interpolate(start.getY(), end.getY(), factor);
        float zoom = interpolate(start.getZoom(), end.getZoom(), factor);
        float rotation = interpolate(start.getRotation(), end.getRotation(), factor);
        out.set(x, y, zoom, rotation);
    }

    private static float interpolate(float start, float end, float factor) {
        return start + (end - start) * factor;
    }

}
