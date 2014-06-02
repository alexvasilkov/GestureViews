package com.alexvasilkov.gestures;

import android.graphics.Rect;
import android.view.Gravity;

public class StateController {

    private final Settings mSettings;

    // Temporary objects
    private final Rect mRectContainer = new Rect(), mRectOut = new Rect();
    private final Rect mMovingBounds = new Rect();

    private boolean mIsResetRequired;

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
            boolean updated = adjustZoomLevels();
            applyInitialState(state);
            mIsResetRequired = !updated;
        } else {
            restrictZoom(state);
            restrictPosition(state);
        }
    }

    public void translateBy(State state, float dx, float dy) {
        state.x += dx;
        state.y += dy;
        restrictPosition(state);
    }

    public void translateTo(State state, float x, float y) {
        state.x = x;
        state.y = y;
        restrictPosition(state);
    }

    /**
     * Correctly applying zoom factor to state object
     */
    public void zoomBy(State state, float factor, float pivotX, float pivotY, boolean allowOverZoom) {
        float validFactor = restrictZoomFactor(state, factor, allowOverZoom);
        state.zoom *= validFactor;
        // Point (pivotX, pivotY) should stay still after scaling, so we need to adjust x & y:
        state.x = (state.x - pivotX) * validFactor + pivotX;
        state.y = (state.y - pivotY) * validFactor + pivotY;
        restrictPosition(state);
    }

    /**
     * Correctly setting zoom level to state object
     */
    public void zoomTo(State state, float zoom, float pivotX, float pivotY, boolean allowOverZoom) {
        zoomBy(state, zoom / state.zoom, pivotX, pivotY, allowOverZoom);
    }

    /**
     * Correctly applying rotation delta to state object
     */
    public void rotateBy(State state, float angle, float pivotX, float pivotY) {
        state.rotation += angle;
        // Point (pivotX, pivotY) should stay still after rotation, so we need to adjust x & y:



        // TODO
        restrictPosition(state);
    }

    /**
     * Interpolates from start state to end state by given factor (from 0 to 1), storing result into out state.
     */
    public void interpolate(State out, State start, State end, float factor) {
        out.x = interpolate(start.x, end.x, factor);
        out.y = interpolate(start.y, end.y, factor);
        out.zoom = interpolate(start.zoom, end.zoom, factor);
        out.rotation = interpolate(start.rotation, end.rotation, factor);
    }

    /**
     * Calculating bounds for {@link State#x}
     * & {@link State#y} values to keep view inside viewport
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

        return mMovingBounds;
    }

    /**
     * Returns view position within the viewport area with gravity applied, not taking into account view position
     * (specified with {@link com.alexvasilkov.gestures.State#x} & {@link com.alexvasilkov.gestures.State#y}).
     * <p/>
     * Do note store returned rectangle, since it will be reused next time this method is called.
     */
    public Rect getPositionWithGravity(State state) {
        final int w = (int) (mSettings.getViewW() * state.zoom);
        final int h = (int) (mSettings.getViewH() * state.zoom);

        // Calculating view position basing on gravity
        mRectContainer.set(0, 0, mSettings.getViewportW(), mSettings.getViewportH());
        Gravity.apply(mSettings.getGravity(), w, h, mRectContainer, mRectOut);

        return mRectOut;
    }

    /**
     * Returns calculated min zoom level
     */
    public float getMinZoomLevel() {
        return mMinZoom;
    }

    /**
     * Returns calculated max zoom level
     */
    public float getMaxZoomLevel() {
        return mMaxZoom;
    }

    /**
     * Restricting position to keep view inside viewport. See {@link #getMovingBounds(State)})
     */
    private void restrictPosition(State state) {
        Rect bounds = getMovingBounds(state);
        state.x = restrict(state.x, bounds.left, bounds.right);
        state.y = restrict(state.y, bounds.top, bounds.bottom);
    }

    /**
     * Restricting zoom factor to keep zoom in min/max bounds
     */
    private float restrictZoomFactor(State state, float factor, boolean allowOverZoom) {
        float overZoomFactor = allowOverZoom ? mSettings.getZoomBouncingFactor() : 1f;
        float validNextZoom = restrict(state.zoom * factor, mMinZoom / overZoomFactor, mMaxZoom * overZoomFactor);
        return validNextZoom == 0 || state.zoom == 0 ? 1f : validNextZoom / state.zoom;
    }

    /**
     * Keeping zoom in min/max bounds
     */
    private void restrictZoom(State state) {
        state.zoom = restrict(state.zoom, mMinZoom, mMaxZoom);
    }

    /**
     * Adjusting min and max zoom levels.
     *
     * @return true if zoom levels was correctly updated (viewport and view size are known), false otherwise
     */
    private boolean adjustZoomLevels() {
        mMinZoom = 1f;
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

    private void applyInitialState(State state) {
        state.zoom = mMinZoom;
        state.rotation = 0;

        final Rect pos = getPositionWithGravity(state);
        state.x = pos.left;
        state.y = pos.top;
    }


    private static float restrict(float value, float minValue, float maxValue) {
        return Math.max(minValue, Math.min(value, maxValue));
    }

    private static float interpolate(float start, float end, float factor) {
        return start + (end - start) * factor;
    }

}
