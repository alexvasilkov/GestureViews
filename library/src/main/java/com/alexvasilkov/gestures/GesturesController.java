package com.alexvasilkov.gestures;

import android.content.Context;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.*;
import android.widget.OverScroller;
import com.alexvasilkov.gestures.detectors.RotateGestureDetector;

/**
 * Main logic to update view state ({@link State}) basing on screen touches.
 * <p/>
 * This class implements {@link android.view.View.OnTouchListener} and provides
 * {@link GesturesController.OnStateChangedListener} to listen for state changes.
 * <p/>
 * Settings can be provided through {@link #getSettings()}. Note, that some settings are required,
 * see {@link Settings}.
 */
public class GesturesController extends GesturesAdapter {

    private static final float ZOOM_GESTURE_MIN_SPAN_DP = 20f;

    // Control constants converted to pixels
    private final float mZoomGestureMinSpan;
    private final int mTouchSlop, mMaximumVelocity;

    private final OnStateChangedListener mStateListener;

    private final AnimationTick mAnimationTick;

    // Various gesture detectors
    private final GestureDetector mGestureDetector;
    private final ScaleGestureDetector mScaleDetector;
    private final RotateGestureDetector mRotateDetector;
    private boolean mIsScrollDetected;
    private boolean mIsScaleDetected;

    private final OverScroller mScroller;
    private final FloatScroller mZoomScroller;

    private State mStateStart, mStateEnd;

    private final Settings mSettings = new Settings();
    private final State mState = new State();
    private final StateController mStateController = new StateController(mSettings);

    private OnGestureListener mGestureListener;

    public GesturesController(Context context, OnStateChangedListener listener) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        mZoomGestureMinSpan = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, ZOOM_GESTURE_MIN_SPAN_DP, metrics);

        mStateListener = listener;

        mAnimationTick = new AnimationTick();
        mGestureDetector = new GestureDetector(context, this);
        mScaleDetector = new ScaleGestureDetector(context, this);
        warmUpScaleDetector();
        mRotateDetector = new RotateGestureDetector(context, this);

        mScroller = new OverScroller(context);
        mZoomScroller = new FloatScroller(context);

        final ViewConfiguration configuration = ViewConfiguration.get(context);
        mTouchSlop = configuration.getScaledTouchSlop();
        mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
    }

    /**
     * Scale detector is a little buggy when first time scale is occurred.
     * So we will feed it with fake motion event to warm it up.
     */
    private void warmUpScaleDetector() {
        long time = System.currentTimeMillis();
        MotionEvent event = MotionEvent.obtain(time, time, MotionEvent.ACTION_CANCEL, 0f, 0f, 0);
        mScaleDetector.onTouchEvent(event);
        event.recycle();
    }

    public void setOnGesturesListener(OnGestureListener listener) {
        mGestureListener = listener;
    }

    /**
     * Returns settings that can be updated.
     * <p/>
     * Note: always call {@link #updateState()} or {@link #resetState()} after settings was changed
     * to correctly apply state restrictions.
     */
    public Settings getSettings() {
        return mSettings;
    }

    /**
     * Returns current state. In most cases you should not modify state directly,
     * use one of the methods provided in {@link com.alexvasilkov.gestures.StateController} instead.
     * <p/>
     * If current state was changed outside {@link com.alexvasilkov.gestures.GesturesController} you should
     * call {@link GesturesController#updateState()} to properly apply changes.
     */
    public State getState() {
        return mState;
    }

    /**
     * Returns state controller that can be used externally.
     */
    public StateController getStateController() {
        return mStateController;
    }

    /**
     * Applies state restrictions and notifies {@link com.alexvasilkov.gestures.GesturesController.OnStateChangedListener}
     * listener.
     */
    public void updateState() {
        mStateController.updateState(mState);
        notifyStateUpdated();
    }

    /**
     * Resets to initial state (default position, min zoom level) and notifies
     * {@link com.alexvasilkov.gestures.GesturesController.OnStateChangedListener} listener.
     * <p/>
     * Should be called after view size is changed.
     * <p/>
     * See {@link com.alexvasilkov.gestures.Settings#setSize(int, int)}.
     */
    public void resetState() {
        mStateController.resetState(mState);
        notifyStateUpdated();
    }

    protected void notifyStateUpdated() {
        mStateListener.onStateChanged(mState);
    }


    // -------------------
    //  Gestures handling
    // -------------------

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        boolean result = mGestureDetector.onTouchEvent(event);
        result |= mScaleDetector.onTouchEvent(event);
        result |= mRotateDetector.onTouchEvent(event);

        if (mIsScrollDetected || mScaleDetector.isInProgress() || mRotateDetector.isInProgress())
            notifyStateUpdated();

        return result;
    }

    @Override
    public boolean onDown(MotionEvent e) {
        mScroller.forceFinished(true);
        mIsScrollDetected = false;
        mIsScaleDetected = false;

        if (mGestureListener != null && mGestureListener.onDown(e)) return true;

        return mSettings.isEnabled();
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return mGestureListener != null && mGestureListener.onSingleTapUp(e);
    }

    @Override
    public void onLongPress(MotionEvent e) {
        if (mGestureListener != null) mGestureListener.onLongPress(e);
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        // Scrolling if panning is enabled and no zoom animation is in place
        if (!mSettings.isEnabled() || !mZoomScroller.isFinished()) return false;

        if (!mIsScrollDetected) {
            mIsScrollDetected = Math.abs(e2.getX() - e1.getX()) > mTouchSlop
                    || Math.abs(e2.getY() - e1.getY()) > mTouchSlop;

            // First scroll event can jerk a bit, so we will ignore it for smoother scrolling
            if (mIsScrollDetected) return true;
        }

        if (mIsScrollDetected) {
            mStateController.translateBy(mState, -distanceX, -distanceY);
        }

        return mIsScrollDetected;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        // Flinging if panning is enabled and no zoom animation is in place
        if (!mSettings.isEnabled() || !mZoomScroller.isFinished()) return false;
        if (!mIsScrollDetected) return false;

        mScroller.forceFinished(true);

        mScroller.fling(
                (int) mState.x,
                (int) mState.y,
                limitFlingVelocity(velocityX),
                limitFlingVelocity(velocityY),
                Integer.MIN_VALUE, Integer.MAX_VALUE,
                Integer.MIN_VALUE, Integer.MAX_VALUE);

        mAnimationTick.startAnimation();

        return true;
    }

    private int limitFlingVelocity(float velocity) {
        if (velocity >= mMaximumVelocity) return mMaximumVelocity;
        return (int) (velocity + 0.5f);
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        return mGestureListener != null && mGestureListener.onSingleTapConfirmed(e);
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
        if (e.getActionMasked() != MotionEvent.ACTION_UP) return false;
        // ScaleGestureDetector can perform zoom by double tap & drag since KITKAT,
        // so we should suppress our double tap in this case
        if (mIsScaleDetected) return false;

        if (mGestureListener != null && mGestureListener.onDoubleTap(e)) return true;
        if (!mSettings.isDoubleTapEnabled()) return false;

        final float middleZoom = (mStateController.getMaxZoomLevel() + mStateController.getMinZoomLevel()) / 2f;
        final float targetZoom;

        if (mState.zoom < middleZoom) { // zooming in
            targetZoom = mStateController.getMaxZoomLevel();
        } else { // zooming out
            targetZoom = mStateController.getMinZoomLevel();
        }

        animateZoomTo(targetZoom, e.getX(), e.getY());

        return true;
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        mIsScaleDetected = mSettings.isEnabled() && mZoomScroller.isFinished();
        return mIsScaleDetected;
    }

    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        if (detector.getCurrentSpan() > mZoomGestureMinSpan) {
            mStateController.zoomBy(mState, detector.getScaleFactor(),
                    detector.getFocusX(), detector.getFocusY(), true);
        }
        return true;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {
        if (mState.zoom < mStateController.getMinZoomLevel()) {
            animateZoomTo(mStateController.getMinZoomLevel(), detector.getFocusX(), detector.getFocusY());
        } else if (mState.zoom > mStateController.getMaxZoomLevel()) {
            animateZoomTo(mStateController.getMaxZoomLevel(), detector.getFocusX(), detector.getFocusY());
        }
    }

    @Override
    public boolean onRotateBegin(RotateGestureDetector detector) {
        return mSettings.isEnabled() && mSettings.isRotationEnabled() && mZoomScroller.isFinished();
    }

    @Override
    public boolean onRotate(RotateGestureDetector detector) {
        mStateController.rotateBy(mState, detector.getRotationDegreesDelta(),
                detector.getFocusX(), detector.getFocusY());
        return true;
    }

    private void animateZoomTo(float zoom, float pivotX, float pivotY) {
        mStateStart = mState.clone();
        mStateEnd = mState.clone();
        mStateController.zoomTo(mStateEnd, zoom, pivotX, pivotY, false);

        mZoomScroller.startScroll(0f, 1f);
        mAnimationTick.startAnimation();
    }


    /**
     * Runnable implementation to animate state changes
     */
    private class AnimationTick implements Runnable {

        private final Handler mHandler = new Handler();

        @Override
        public void run() {
            boolean needsInvalidate = false;

            if (mScroller.computeScrollOffset()) {
                // The scroller isn't finished, meaning a fling is currently active.
                mStateController.translateTo(mState, mScroller.getCurrX(), mScroller.getCurrY());
                needsInvalidate = true;
            }

            if (mZoomScroller.computeScroll()) {
                float factor = mZoomScroller.getCurr();
                mStateController.interpolate(mState, mStateStart, mStateEnd, factor);
                needsInvalidate = true;
            } else {
                mStateStart = mStateEnd = null;
            }

            if (needsInvalidate) {
                notifyStateUpdated();
                startAnimation();
            }
        }

        void startAnimation() {
            mHandler.removeCallbacks(this);
            mHandler.post(this);
        }

    }


    // -------------------
    //  Listeners
    // -------------------

    /**
     * State changes listener
     */
    public interface OnStateChangedListener {
        void onStateChanged(State state);
    }

    /**
     * Listener for different touch events
     */
    public interface OnGestureListener {
        boolean onDown(MotionEvent e);

        boolean onSingleTapUp(MotionEvent e);

        void onLongPress(MotionEvent e);

        boolean onSingleTapConfirmed(MotionEvent e);

        boolean onDoubleTap(MotionEvent e);
    }

    /**
     * Simple implementation of {@link com.alexvasilkov.gestures.GesturesController.OnGestureListener}
     */
    public static class SimpleOnGestureListener implements OnGestureListener {
        @Override
        public boolean onDown(MotionEvent e) {
            return false;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            return false;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            // no-op
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            return false;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            return false;
        }
    }

}
