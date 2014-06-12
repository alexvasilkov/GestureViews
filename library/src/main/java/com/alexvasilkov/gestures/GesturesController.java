package com.alexvasilkov.gestures;

import android.content.Context;
import android.graphics.Rect;
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
    private static final float FLING_COEFFICIENT = 0.75f;

    // Control constants converted to pixels
    private final float mZoomGestureMinSpan;
    private final int mTouchSlop, mMinimumVelocity, mMaximumVelocity;

    private final OnStateChangedListener mStateListener;

    private final AnimationTick mAnimationTick;

    // Various gesture detectors
    private final GestureDetector mGestureDetector;
    private final ScaleGestureDetector mScaleDetector;
    private final RotateGestureDetector mRotateDetector;

    private boolean mIsDoubleTapDetected;
    private boolean mIsScrollDetected;
    private boolean mIsFlingDetected;
    private boolean mIsScaleDetected;
    private float mPivotX, mPivotY;

    private final OverScroller mFlingScroller;
    private final FloatScroller mStateScroller;

    private final Rect mFlingBounds = new Rect();
    private final State mPrevState = new State(), mStateStart = new State(), mStateEnd = new State();

    private final Settings mSettings;
    private final State mState = new State();
    private final StateController mStateController;

    private OnGestureListener mGestureListener;

    public GesturesController(Context context, OnStateChangedListener listener) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        mZoomGestureMinSpan = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, ZOOM_GESTURE_MIN_SPAN_DP, metrics);

        mSettings = new Settings(context);
        mStateController = new StateController(mSettings);

        mStateListener = listener;

        mAnimationTick = new AnimationTick();
        mGestureDetector = new GestureDetector(context, this);
        mScaleDetector = new ScaleGestureDetector(context, this);
        warmUpScaleDetector();
        mRotateDetector = new RotateGestureDetector(context, this);

        mFlingScroller = new OverScroller(context);
        mStateScroller = new FloatScroller(context);

        final ViewConfiguration configuration = ViewConfiguration.get(context);
        mTouchSlop = configuration.getScaledTouchSlop();
        mMinimumVelocity = configuration.getScaledMinimumFlingVelocity();
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

    /**
     * Sets listener for basic touch events. See {@link com.alexvasilkov.gestures.GesturesController.OnGestureListener}
     */
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

    /**
     * Animates current state to provided end state
     */
    public void animateStateTo(State endState) {
        if (endState == null) return;

        stopFlingAnimation();
        stopStateAnimation();

        mStateStart.set(mState);
        mStateEnd.set(endState);
        mStateScroller.startScroll(0f, 1f);
        mAnimationTick.startAnimation();
    }

    public void stopStateAnimation() {
        mStateScroller.forceFinished(true);
    }

    public void notifyStateUpdated() {
        mPrevState.set(mState);
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

        if (event.getActionMasked() == MotionEvent.ACTION_UP || event.getActionMasked() == MotionEvent.ACTION_CANCEL) {
            onUp(event);
        }

        mStateController.restrictStateBounds(mState, mPrevState, mPivotX, mPivotY, true, true);

        if (!mState.equals(mPrevState)) {
            mPrevState.set(mState);
            notifyStateUpdated();
        }

        return result;
    }

    @Override
    public boolean onDown(MotionEvent e) {
        stopFlingAnimation();
        stopStateAnimation();

        mIsDoubleTapDetected = false;
        mIsScrollDetected = false;
        mIsFlingDetected = false;
        mIsScaleDetected = false;

        if (mGestureListener != null) mGestureListener.onDown(e);

        return mSettings.isEnabled() && mStateScroller.isFinished();
    }

    protected void onUp(MotionEvent e) {
        if (mIsFlingDetected || mIsDoubleTapDetected) return;

        State endState = mStateController.restrictStateBoundsCopy(mState, e.getX(), e.getY(), false, false);
        animateStateTo(endState);
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
        if (!mSettings.isEnabled() || !mStateScroller.isFinished()) return false;

        if (!mIsScrollDetected) {
            mIsScrollDetected = Math.abs(e2.getX() - e1.getX()) > mTouchSlop
                    || Math.abs(e2.getY() - e1.getY()) > mTouchSlop;

            // First scroll event can jerk a bit, so we will ignore it for smoother scrolling
            if (mIsScrollDetected) return true;
        }

        if (mIsScrollDetected) mState.translateBy(-distanceX, -distanceY);

        return mIsScrollDetected;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        if (!mSettings.isEnabled() || !mStateScroller.isFinished()) return false;

        mIsFlingDetected = true;

        int x = Math.round(mState.getX());
        int y = Math.round(mState.getY());

        // Fling bounds including current position
        mFlingBounds.set(mStateController.getMovingBounds(mState));
        mFlingBounds.union(x, y);

        stopFlingAnimation();
        mFlingScroller.fling(
                x, y,
                limitFlingVelocity(velocityX * FLING_COEFFICIENT),
                limitFlingVelocity(velocityY * FLING_COEFFICIENT),
                Integer.MIN_VALUE, Integer.MAX_VALUE,
                Integer.MIN_VALUE, Integer.MAX_VALUE);
        mAnimationTick.startAnimation();

        return true;
    }

    private int limitFlingVelocity(float velocity) {
        if (Math.abs(velocity) < mMinimumVelocity) return 0;
        if (Math.abs(velocity) >= mMaximumVelocity) return (int) Math.signum(velocity) * mMaximumVelocity;
        return Math.round(velocity);
    }

    protected void onFlingScroll(float fromX, float fromY, float toX, float toY) {
        float x = toX, y = toY;
        if (mSettings.isRestrictBounds()) {
            x = StateController.restrict(x, mFlingBounds.left, mFlingBounds.right);
            y = StateController.restrict(y, mFlingBounds.top, mFlingBounds.bottom);
        }

        mState.translateTo(x, y);
    }

    protected void stopFlingAnimation() {
        mFlingScroller.forceFinished(true);
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        return mGestureListener != null && mGestureListener.onSingleTapConfirmed(e);
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
        if (e.getActionMasked() != MotionEvent.ACTION_UP) return false;
        // ScaleGestureDetector can perform zoom by "double tap & drag" since KITKAT,
        // so we should suppress our double tap in this case
        if (mIsScaleDetected) return false;

        // Let user redefine double tap
        if (mGestureListener != null && mGestureListener.onDoubleTap(e)) return true;

        if (!mSettings.isEnabled() || !mSettings.isDoubleTapEnabled()) return false;

        mIsDoubleTapDetected = true;

        State endState = mStateController.toggleMinMaxZoom(mState, e.getX(), e.getY());
        mStateController.restrictStateBounds(endState, null, e.getX(), e.getY(), false, false);
        animateStateTo(endState);

        return true;
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        mIsScaleDetected = mSettings.isEnabled();
        return mIsScaleDetected;
    }

    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        if (!mSettings.isEnabled() || !mStateScroller.isFinished()) return true;

        if (detector.getCurrentSpan() > mZoomGestureMinSpan) {
            // When scale is end (in onScaleEnd method),
            // scale detector will return wrong focus point, so we should save it here
            mPivotX = detector.getFocusX();
            mPivotY = detector.getFocusY();
            mState.zoomBy(detector.getScaleFactor(), mPivotX, mPivotY);
        }

        return true;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {
        mIsScaleDetected = false;

        if (!mSettings.isEnabled()) return;

        // Scroll can still be in place, so we should preserver overscroll
        State endState = mStateController.restrictStateBoundsCopy(mState, mPivotX, mPivotY, true, false);
        animateStateTo(endState);

        mPivotX = mPivotY = 0f;
    }

    @Override
    public boolean onRotateBegin(RotateGestureDetector detector) {
        return mSettings.isEnabled() && mSettings.isRotationEnabled();
    }

    @Override
    public boolean onRotate(RotateGestureDetector detector) {
        if (!mSettings.isEnabled() || !mStateScroller.isFinished()) return true;

        mState.rotateBy(detector.getRotationDegreesDelta(), detector.getFocusX(), detector.getFocusY());

        return true;
    }

    protected void onFlingAnimationFinished() {
        State endState = mStateController.restrictStateBoundsCopy(mState, 0f, 0f, false, false);
        animateStateTo(endState);
    }

    protected void onStateAnimationFinished() {
        // no-op
    }

    /**
     * Runnable implementation to animate state changes
     */
    private class AnimationTick implements Runnable {

        private final Handler mHandler = new Handler();

        @Override
        public void run() {
            boolean needsInvalidate = false;

            if (!mFlingScroller.isFinished()) {
                if (mFlingScroller.computeScrollOffset()) {
                    float lastX = mState.getX(), lastY = mState.getY();

                    float x = mFlingScroller.getCurrX();
                    float y = mFlingScroller.getCurrY();

                    onFlingScroll(lastX, lastY, x, y);

                    if (State.equals(lastX, mState.getX()) && State.equals(lastY, mState.getY())) {
                        stopFlingAnimation();
                    }

                    needsInvalidate = true;
                }

                if (mFlingScroller.isFinished()) {
                    onFlingAnimationFinished();
                }
            }

            if (!mStateScroller.isFinished()) {
                if (mStateScroller.computeScroll()) {
                    float factor = mStateScroller.getCurr();
                    StateController.interpolate(mState, mStateStart, mStateEnd, factor);
                    needsInvalidate = true;
                }

                if (mStateScroller.isFinished()) {
                    onStateAnimationFinished();
                }
            }

            if (needsInvalidate) {
                notifyStateUpdated();
                startAnimation();
            }
        }

        void startAnimation() {
            mHandler.removeCallbacks(this);
            mHandler.postDelayed(this, 10);
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
        void onDown(MotionEvent e);

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
        public void onDown(MotionEvent e) {
            // no-op
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
