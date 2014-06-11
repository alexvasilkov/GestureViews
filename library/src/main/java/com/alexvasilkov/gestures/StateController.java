package com.alexvasilkov.gestures;

import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.Gravity;

public class StateController {

    private final Settings mSettings;

    // Temporary objects
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
            // (waiting for layout, waiting for image to be loaded)
            boolean updated = adjustZoomLevels();
            applyInitialState(state);
            mIsResetRequired = !updated;
        } else {
            restrictStateBounds(state);
        }
    }

    private void applyInitialState(State state) {
        state.set(0f, 0f, mMinZoom, 0f);
        Rect pos = getPositionWithGravity(state);
        state.translateTo(pos.left, pos.top);
    }

    public void translateByWithResilience(State state, float dx, float dy) {
        float fixedX = dx;
        float fixedY = dy;

        if (mSettings.isRestrictBounds()) {
            Rect bounds = getMovingBounds(state);
            fixedX = applyTranslationResilience(state.getX(), fixedX,
                    bounds.left, bounds.right, mSettings.getOverscrollDistanceX());
            fixedY = applyTranslationResilience(state.getY(), fixedY,
                    bounds.top, bounds.bottom, mSettings.getOverscrollDistanceX());
        }

        state.translateBy(fixedX, fixedY);
        restrictStateBounds(state, 0f, 0f, true, true);
    }

    private float applyTranslationResilience(float value, float delta,
                                             float boundsMin, float boundsMax, float overscroll) {
        if (overscroll == 0) {
            return delta;
        } else if (value < boundsMin && delta < 0) {
            float resilience = (boundsMin - value) / overscroll;
            return delta * (1f - resilience) / 2;
        } else if (value > boundsMax && delta > 0) {
            float resilience = (value - boundsMax) / overscroll;
            return delta * (1f - resilience) / 2;
        } else {
            return delta;
        }
    }

    /**
     * Correctly applying zoom factor to state object
     */
    public void zoomByWithResilience(State state, float factor, float pivotX, float pivotY) {
        float fixedFactor = factor;

        if (mSettings.isRestrictBounds()) {
            // adding overzoom viscosity
            float zoom = state.getZoom();
            float overZoomFactor = mSettings.getOverzoomFactor();
            float minZoom = mMinZoom / overZoomFactor;
            float maxZoom = mMaxZoom * overZoomFactor;

            if (zoom < mMinZoom && fixedFactor < 1) {
                float resilience = (mMinZoom - zoom) / (mMinZoom - minZoom);
                fixedFactor += resilience * (1f - fixedFactor);
            } else if (zoom > mMaxZoom && fixedFactor > 1) {
                float resilience = (zoom - mMaxZoom) / (maxZoom - mMaxZoom);
                fixedFactor += resilience * (1f - fixedFactor);
            }
        }

        state.zoomBy(fixedFactor, pivotX, pivotY);
        restrictStateBounds(state, pivotX, pivotY, true, true);
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
     * Restricts state's translation and zoom bounds.
     */
    public void restrictStateBounds(State state) {
        if (!mSettings.isRestrictBounds()) return;
        restrictStateBounds(state, 0f, 0f, false, false);
    }

    /**
     * Restricts state's translation and zoom bounds.
     */
    public void restrictStateBounds(State state, float pivotX, float pivotY) {
        if (!mSettings.isRestrictBounds()) return;
        restrictStateBounds(state, pivotX, pivotY, false, false);
    }

    /**
     * Restricts state's translation and zoom bounds.
     *
     * @return End state to animate changes or null if no changes are required
     */
    public State restrictStateBoundsCopy(State state, float pivotX, float pivotY,
                                         boolean allowOverscroll, boolean allowOverzoom) {

        if (!mSettings.isRestrictBounds()) return null;

        State out = state.copy();
        boolean changed = restrictStateBounds(out, pivotX, pivotY, allowOverscroll, allowOverzoom);
        return changed ? out : null;
    }

    /**
     * Restricts state's translation and zoom bounds.
     *
     * @return true if state was changed, false otherwise
     */
    public boolean restrictStateBounds(State state, float pivotX, float pivotY,
                                       boolean allowOverscroll, boolean allowOverzoom) {

        if (!mSettings.isRestrictBounds()) return false;

        boolean isStateChanged = false;

        float overzoom = allowOverzoom ? mSettings.getOverzoomFactor() : 1f;
        float zoom = restrict(state.getZoom(), mMinZoom / overzoom, mMaxZoom * overzoom);
        if (zoom != state.getZoom()) {
            state.zoomTo(zoom, pivotX, pivotY);
            isStateChanged = true;
        }

        Rect bounds = getMovingBounds(state);
        float overscrollX = allowOverscroll ? mSettings.getOverscrollDistanceX() : 0f;
        float overscrollY = allowOverscroll ? mSettings.getOverscrollDistanceY() : 0f;
        float x = restrict(state.getX(), bounds.left - overscrollX, bounds.right + overscrollX);
        float y = restrict(state.getY(), bounds.top - overscrollY, bounds.bottom + overscrollY);

        if (x != state.getX() || y != state.getY()) {
            state.translateTo(x, y);
            isStateChanged = true;
        }

        return isStateChanged;
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
        state.applyTo(mMatrix);

        mRectF.set(0, 0, mSettings.getViewW(), mSettings.getViewH());
        mMatrix.mapRect(mRectF);

        mPointF[0] = 0;
        mPointF[1] = 0;
        mMatrix.mapPoints(mPointF);

        mMovingBounds.offset((int) (mPointF[0] - mRectF.left + 0.5f), (int) (mPointF[1] - mRectF.top + 0.5f));

        return mMovingBounds;
    }

    /**
     * Returns view position within the viewport area with gravity applied, not taking into account view position
     * (specified with {@link com.alexvasilkov.gestures.State#x} & {@link com.alexvasilkov.gestures.State#y}).
     * <p/>
     * Do note store returned rectangle, since it will be reused next time this method is called.
     */
    private Rect getPositionWithGravity(State state) {
        state.applyTo(mMatrix);
        mRectF.set(0, 0, mSettings.getViewW(), mSettings.getViewH());
        mMatrix.mapRect(mRectF);
        final int w = (int) (mRectF.width() + 0.5);
        final int h = (int) (mRectF.height() + 0.5);

        // Calculating view position basing on gravity
        mRectContainer.set(0, 0, mSettings.getViewportW(), mSettings.getViewportH());
        Gravity.apply(mSettings.getGravity(), w, h, mRectContainer, mRectOut);

        return mRectOut;
    }

    /**
     * Interpolates from start state to end state by given factor (from 0 to 1), storing result into out state.
     */
    public void interpolate(State out, State start, State end, float factor) {
        float x = interpolate(start.getX(), end.getX(), factor);
        float y = interpolate(start.getY(), end.getY(), factor);
        float zoom = interpolate(start.getZoom(), end.getZoom(), factor);
        float rotation = interpolate(start.getRotation(), end.getRotation(), factor);
        out.set(x, y, zoom, rotation);
    }

    /**
     * Adjusting min and max zoom levels.
     *
     * @return true if zoom levels was correctly updated (viewport and view size are known), false otherwise
     */
    private boolean adjustZoomLevels() {
        mMaxZoom = mSettings.getMaxZoom();

        final float w = mSettings.getViewW(), h = mSettings.getViewH();
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

    private static float interpolate(float start, float end, float factor) {
        return start + (end - start) * factor;
    }

}
