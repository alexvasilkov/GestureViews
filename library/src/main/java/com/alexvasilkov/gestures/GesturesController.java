package com.alexvasilkov.gestures;

import android.content.Context;
import android.graphics.PointF;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.OverScroller;

import com.alexvasilkov.gestures.detectors.RotationGestureDetector;
import com.alexvasilkov.gestures.detectors.ScaleGestureDetectorFixed;
import com.alexvasilkov.gestures.utils.FloatScroller;
import com.alexvasilkov.gestures.utils.MovementBounds;

import java.util.LinkedList;
import java.util.List;

/**
 * Main logic to update view state ({@link State}) basing on screen touches.
 * <p/>
 * This class implements {@link android.view.View.OnTouchListener} and provides
 * {@link GesturesController.OnStateChangedListener} to listen for state changes.
 * <p/>
 * Settings can be obtained through {@link #getSettings()}. Note, that some settings are required,
 * see {@link Settings}.
 */
public class GesturesController extends GesturesAdapter {

    private static final float ZOOM_GESTURE_MIN_SPAN_DP = 20f;
    private static final float FLING_COEFFICIENT = 0.75f;

    // Control constants converted to pixels
    private final float mZoomGestureMinSpan;
    private final int mTouchSlop, mMinimumVelocity, mMaximumVelocity;

    private final List<OnStateChangedListener> mStateListeners = new LinkedList<>();

    private final AnimationTick mAnimationTick;

    // Various gesture detectors
    private final GestureDetector mGestureDetector;
    private final ScaleGestureDetector mScaleDetector;
    private final RotationGestureDetector mRotateDetector;

    private boolean mIsDoubleTapDetected;
    private boolean mIsScrollDetected;
    private boolean mIsFlingDetected;
    private boolean mIsScaleDetected;
    private float mPivotX, mPivotY;

    private final OverScroller mFlingScroller;
    private final FloatScroller mStateScroller;

    private final MovementBounds mFlingBounds = new MovementBounds();
    private final State mPrevState = new State();
    private final State mStateStart = new State();
    private final State mStateEnd = new State();

    private final Settings mSettings;
    private final State mState = new State();
    private final StateController mStateController;

    private OnGestureListener mGestureListener;

    public GesturesController(Context context, OnStateChangedListener listener) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        mZoomGestureMinSpan = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, ZOOM_GESTURE_MIN_SPAN_DP, metrics);

        mSettings = new Settings();
        mStateController = new StateController(mSettings);

        mStateListeners.add(listener);

        mAnimationTick = new AnimationTick();
        mGestureDetector = new GestureDetector(context, this);
        mGestureDetector.setIsLongpressEnabled(false);
        mScaleDetector = new ScaleGestureDetectorFixed(context, this);
        mRotateDetector = new RotationGestureDetector(context, this);

        mFlingScroller = new OverScroller(context);
        mStateScroller = new FloatScroller();

        final ViewConfiguration configuration = ViewConfiguration.get(context);
        mTouchSlop = configuration.getScaledTouchSlop();
        mMinimumVelocity = configuration.getScaledMinimumFlingVelocity();
        mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
    }


    /**
     * Sets listener for basic touch events.
     * <p/>
     * See also {@link GesturesController.OnGestureListener}
     */
    public void setOnGesturesListener(OnGestureListener listener) {
        mGestureListener = listener;
    }

    /**
     * Adds listener for state changes.
     * <p/>
     * See also {@link GesturesController.OnStateChangedListener}
     */
    public void addOnStateChangedListener(OnStateChangedListener listener) {
        mStateListeners.add(listener);
    }

    /**
     * Removes listener for state changes.
     * <p/>
     * See also {@link #addOnStateChangedListener(GesturesController.OnStateChangedListener)}
     */
    public void removeOnStateChangedListener(OnStateChangedListener listener) {
        mStateListeners.remove(listener);
    }

    /**
     * Sets whether long press is enabled or not. Long press is disabled by default.
     * <p/>
     * See also {@link GesturesController.OnGestureListener#onLongPress(android.view.MotionEvent)}
     */
    public void setLongPressEnabled(boolean enabled) {
        mGestureDetector.setIsLongpressEnabled(enabled);
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
     * use one of the methods provided in {@link StateController} instead.
     * <p/>
     * If current state was changed outside {@link GesturesController}
     * you should call {@link GesturesController#updateState()} to properly apply changes.
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
     * Applies state restrictions and notifies
     * {@link GesturesController.OnStateChangedListener} listeners.
     */
    public void updateState() {
        mStateController.updateState(mState);
        notifyStateUpdated();
    }

    /**
     * Resets to initial state (default position, min zoom level) and notifies
     * {@link GesturesController.OnStateChangedListener} listeners.
     * <p/>
     * Should be called after view size is changed.
     * <p/>
     * See {@link Settings#setSize(int, int)}.
     */
    public void resetState() {
        mStateController.resetState(mState);
        notifyStateReset();
    }

    /**
     * Animates current state to provided end state. Note, that no state restrictions
     * will be applied during animation, so you should ensure end state is within bounds.
     */
    public void animateStateTo(State endState) {
        if (endState == null) return;

        // Ensuring we always starts in correct state
        if (mStateScroller.isFinished()) {
            mStateController.restrictStateBounds(mState, mPrevState, mPivotX, mPivotY, true, true);
        }

        stopFlingAnimation();
        stopStateAnimation();

        mStateStart.set(mState);
        mStateEnd.set(endState);
        mStateScroller.startScroll(0f, 1f);
        mAnimationTick.startAnimation();
    }

    public void stopStateAnimation() {
        mStateScroller.forceFinished();
    }

    protected void notifyStateUpdated() {
        mPrevState.set(mState);
        for (OnStateChangedListener listener : mStateListeners) {
            listener.onStateChanged(mState);
        }
    }

    protected void notifyStateReset() {
        for (OnStateChangedListener listener : mStateListeners) {
            listener.onStateReset(mPrevState, mState);
        }
        notifyStateUpdated();
    }


    // -------------------
    //  Gestures handling
    // -------------------

    @Override
    public boolean onTouch(@NonNull View view, @NonNull MotionEvent event) {
        boolean result = mGestureDetector.onTouchEvent(event);
        result |= mScaleDetector.onTouchEvent(event);
        result |= mRotateDetector.onTouchEvent(event);

        if (event.getActionMasked() == MotionEvent.ACTION_UP
                || event.getActionMasked() == MotionEvent.ACTION_CANCEL) {
            onUpOrCancel(event);
        }

        if (mStateScroller.isFinished()) {
            mStateController.restrictStateBounds(mState, mPrevState, mPivotX, mPivotY, true, true);
        }

        if (!mState.equals(mPrevState)) {
            mPrevState.set(mState);
            notifyStateUpdated();
        }

        return result;
    }

    @Override
    public boolean onDown(@NonNull MotionEvent e) {
        stopFlingAnimation();

        mIsDoubleTapDetected = false;
        mIsScrollDetected = false;
        mIsFlingDetected = false;
        mIsScaleDetected = false;

        if (mGestureListener != null) mGestureListener.onDown(e);

        if (mSettings.isEnabled()) {
            stopStateAnimation();
            return true;
        } else {
            return false;
        }
    }

    protected void onUpOrCancel(MotionEvent e) {
        if (mIsFlingDetected || mIsDoubleTapDetected) return;

        State s = mStateController.restrictStateBoundsCopy(mState, mPivotX, mPivotY, false, false);
        animateStateTo(s);
    }

    @Override
    public boolean onSingleTapUp(@NonNull MotionEvent e) {
        return mGestureListener != null && mGestureListener.onSingleTapUp(e);
    }

    @Override
    public void onLongPress(@NonNull MotionEvent e) {
        if (mGestureListener != null) mGestureListener.onLongPress(e);
    }

    @Override
    public boolean onScroll(@NonNull MotionEvent e1, @NonNull MotionEvent e2, float dX, float dY) {
        if (!mSettings.isPanEnabled() || !mStateScroller.isFinished()) return false;

        if (!mIsScrollDetected) {
            mIsScrollDetected = Math.abs(e2.getX() - e1.getX()) > mTouchSlop
                    || Math.abs(e2.getY() - e1.getY()) > mTouchSlop;

            // First scroll event can jerk a bit, so we will ignore it for smoother scrolling
            if (mIsScrollDetected) return true;
        }

        if (mIsScrollDetected) mState.translateBy(-dX, -dY);

        return mIsScrollDetected;
    }

    @Override
    public boolean onFling(@NonNull MotionEvent e1, @NonNull MotionEvent e2, float vX, float vY) {
        if (!mSettings.isPanEnabled() || !mStateScroller.isFinished()) return false;

        mIsFlingDetected = true;

        int x = Math.round(mState.getX());
        int y = Math.round(mState.getY());

        // Fling bounds including current position
        mFlingBounds.set(mStateController.getMovementBounds(mState));
        mFlingBounds.union(x, y);

        stopFlingAnimation();
        mFlingScroller.fling(
                x, y,
                limitFlingVelocity(vX * FLING_COEFFICIENT),
                limitFlingVelocity(vY * FLING_COEFFICIENT),
                Integer.MIN_VALUE, Integer.MAX_VALUE,
                Integer.MIN_VALUE, Integer.MAX_VALUE);
        mAnimationTick.startAnimation();

        return true;
    }

    private int limitFlingVelocity(float velocity) {
        if (Math.abs(velocity) < mMinimumVelocity) {
            return 0;
        } else if (Math.abs(velocity) >= mMaximumVelocity) {
            return (int) Math.signum(velocity) * mMaximumVelocity;
        } else {
            return Math.round(velocity);
        }
    }

    @SuppressWarnings("UnusedParameters")
    protected void onFlingScroll(float fromX, float fromY, float toX, float toY) {
        float x = toX, y = toY;
        if (mSettings.isRestrictBounds()) {
            PointF pos = mFlingBounds.restrict(x, y);
            x = pos.x;
            y = pos.y;
        }

        mState.translateTo(x, y);
    }

    protected void stopFlingAnimation() {
        mFlingScroller.forceFinished(true);
    }

    @Override
    public boolean onSingleTapConfirmed(@NonNull MotionEvent e) {
        return mGestureListener != null && mGestureListener.onSingleTapConfirmed(e);
    }

    @Override
    public boolean onDoubleTapEvent(@NonNull MotionEvent e) {
        if (e.getActionMasked() != MotionEvent.ACTION_UP) return false;
        // ScaleGestureDetector can perform zoom by "double tap & drag" since KITKAT,
        // so we should suppress our double tap in this case
        if (mIsScaleDetected) return false;

        // Let user redefine double tap
        if (mGestureListener != null && mGestureListener.onDoubleTap(e)) return true;

        if (!mSettings.isDoubleTapEnabled()) return false;

        mIsDoubleTapDetected = true;

        State endState = mStateController.toggleMinMaxZoom(mState, e.getX(), e.getY());
        mStateController.restrictStateBounds(endState, null, e.getX(), e.getY(), false, false);
        animateStateTo(endState);

        return true;
    }

    @Override
    public boolean onScaleBegin(@NonNull ScaleGestureDetector detector) {
        mIsScaleDetected = mSettings.isZoomEnabled();
        return mIsScaleDetected;
    }

    @Override
    public boolean onScale(@NonNull ScaleGestureDetector detector) {
        if (!mSettings.isZoomEnabled() || !mStateScroller.isFinished()) return true;

        if (detector.getCurrentSpan() > mZoomGestureMinSpan) {
            // When scale is end (in onRotationEnd method),
            // scale detector will return wrong focus point, so we should save it here
            mPivotX = detector.getFocusX();
            mPivotY = detector.getFocusY();
            mState.zoomBy(detector.getScaleFactor(), mPivotX, mPivotY);
        }

        return true;
    }

    @Override
    public void onScaleEnd(@NonNull ScaleGestureDetector detector) {
        mIsScaleDetected = false;

        if (!mSettings.isZoomEnabled()) return;

        // Scroll can still be in place, so we should preserver overscroll
        State s = mStateController.restrictStateBoundsCopy(mState, mPivotX, mPivotY, true, false);
        animateStateTo(s);
    }

    @Override
    public boolean onRotationBegin(@NonNull RotationGestureDetector detector) {
        return mSettings.isRotationEnabled();
    }

    @Override
    public boolean onRotate(@NonNull RotationGestureDetector detector) {
        if (!mSettings.isRotationEnabled() || !mStateScroller.isFinished()) return true;

        mState.rotateBy(detector.getRotationDelta(), detector.getFocusX(), detector.getFocusY());

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
                mStateScroller.computeScroll();
                float factor = mStateScroller.getCurr();
                StateController.interpolate(mState, mStateStart, mStateEnd, factor);
                needsInvalidate = true;

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
            // Small delay is required (sometimes runnable can be called immediately)
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

        void onStateReset(State oldState, State newState);
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
     * Simple implementation of {@link GesturesController.OnGestureListener}
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
