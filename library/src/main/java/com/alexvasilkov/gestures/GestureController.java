package com.alexvasilkov.gestures;

import android.content.Context;
import android.graphics.PointF;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.OverScroller;

import com.alexvasilkov.gestures.internal.AnimationEngine;
import com.alexvasilkov.gestures.internal.FloatScroller;
import com.alexvasilkov.gestures.internal.MovementBounds;
import com.alexvasilkov.gestures.internal.detectors.RotationGestureDetector;
import com.alexvasilkov.gestures.internal.detectors.ScaleGestureDetectorFixed;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles touch events to update view's position state ({@link State}) based on current
 * setup ({@link Settings}).<br/>
 * Settings can be obtained and altered through {@link #getSettings()}.<br/>
 * Note, that some settings are required in order to correctly update state, see {@link Settings}.
 * <p/>
 * This class implements {@link View.OnTouchListener} to delegate touches from view to controller.
 * <p/>
 * State can also be manipulated directly with {@link #getState()}, {@link #updateState()}
 * and {@link #resetState()}. You can also access {@link #getStateController()} for some additional
 * stuff.
 * <p/>
 * State can be animated with {@link #animateStateTo(State)} method.<br/>
 * See also {@link #stopFlingAnimation()}, {@link #stopStateAnimation()}
 * and {@link #stopAllAnimations()} methods.
 * <p/>
 * All state changes will be passed to {@link OnStateChangeListener OnStateChangeListener}.
 * See {@link #addOnStateChangeListener(OnStateChangeListener) addOnStateChangeListener} and
 * {@link #removeOnStateChangeListener(OnStateChangeListener) removeOnStateChangeListener} methods.
 * <p/>
 * Additional touch events can be listened with {@link OnGestureListener OnGestureListener} and
 * {@link SimpleOnGestureListener SimpleOnGestureListener} using
 * {@link #setOnGesturesListener(OnGestureListener) setOnGesturesListener} method.
 * <p/>
 */
public class GestureController implements View.OnTouchListener {

    private static final float ZOOM_GESTURE_MIN_SPAN_DP = 20f;
    private static final float FLING_COEFFICIENT = 0.75f;

    // Control constants converted to pixels
    private final float mZoomGestureMinSpan;
    private final int mTouchSlop, mMinimumVelocity, mMaximumVelocity;

    private final List<OnStateChangeListener> mStateListeners = new ArrayList<>();
    private OnGestureListener mGestureListener;

    private final AnimationEngine mAnimationEngine;

    // Various gesture detectors
    private final GestureDetector mGestureDetector;
    private final ScaleGestureDetector mScaleDetector;
    private final RotationGestureDetector mRotateDetector;

    private boolean mIsDoubleTapDetected;
    private boolean mIsScrollDetected;
    private boolean mIsFlingDetected;
    private boolean mIsScaleDetected;
    private float mPivotX = Float.NaN, mPivotY = Float.NaN;

    private final OverScroller mFlingScroller;
    private final FloatScroller mStateScroller;

    private final MovementBounds mFlingBounds = new MovementBounds();
    private final State mPrevState = new State();
    private final State mStateStart = new State();
    private final State mStateEnd = new State();

    private final Settings mSettings;
    private final State mState = new State();
    private final StateController mStateController;

    public GestureController(@NonNull View view) {
        Context context = view.getContext();
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        mZoomGestureMinSpan = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, ZOOM_GESTURE_MIN_SPAN_DP, metrics);

        mSettings = new Settings();
        mStateController = new StateController(mSettings);

        mAnimationEngine = new LocalAnimationEngine(view);
        InternalGesturesListener internalListener = new InternalGesturesListener();
        mGestureDetector = new GestureDetector(context, internalListener);
        mGestureDetector.setIsLongpressEnabled(false);
        mScaleDetector = new ScaleGestureDetectorFixed(context, internalListener);
        mRotateDetector = new RotationGestureDetector(context, internalListener);

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
     * See also {@link GestureController.OnGestureListener}
     */
    public void setOnGesturesListener(@Nullable OnGestureListener listener) {
        mGestureListener = listener;
    }

    /**
     * Adds listener for state changes.
     * <p/>
     * See also {@link OnStateChangeListener}
     */
    public void addOnStateChangeListener(OnStateChangeListener listener) {
        mStateListeners.add(listener);
    }

    /**
     * Removes listener for state changes.
     * <p/>
     * See also {@link #addOnStateChangeListener(OnStateChangeListener)}
     */
    public void removeOnStateChangeListener(OnStateChangeListener listener) {
        mStateListeners.remove(listener);
    }

    /**
     * Sets whether long press is enabled or not. Long press is disabled by default.
     * <p/>
     * See also {@link GestureController.OnGestureListener#onLongPress(android.view.MotionEvent)}
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
     * If current state was changed outside {@link GestureController}
     * you should call {@link GestureController#updateState()} to properly apply changes.
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
     * {@link OnStateChangeListener} listeners.
     */
    public void updateState() {
        boolean reset = mStateController.updateState(mState);
        if (reset) {
            notifyStateReset();
        } else {
            notifyStateUpdated();
        }
    }

    /**
     * Resets to initial state (default position, min zoom level) and notifies
     * {@link OnStateChangeListener} listeners.
     * <p/>
     * Should be called when image size is changed.
     * <p/>
     * See {@link Settings#setImage(int, int)}.
     */
    public void resetState() {
        stopAllAnimations();
        boolean reset = mStateController.resetState(mState);
        if (reset) {
            notifyStateReset();
        } else {
            notifyStateUpdated();
        }
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

        stopAllAnimations();

        mStateStart.set(mState);
        mStateEnd.set(endState);
        mStateScroller.startScroll(0f, 1f);
        mAnimationEngine.start();
    }

    public void stopStateAnimation() {
        mStateScroller.forceFinished();
    }

    public void stopFlingAnimation() {
        mFlingScroller.forceFinished(true);
    }

    public void stopAllAnimations() {
        stopStateAnimation();
        stopFlingAnimation();
    }

    protected void notifyStateUpdated() {
        mPrevState.set(mState);
        for (OnStateChangeListener listener : mStateListeners) {
            listener.onStateChanged(mState);
        }
    }

    protected void notifyStateReset() {
        for (OnStateChangeListener listener : mStateListeners) {
            listener.onStateReset(mPrevState, mState);
        }
        notifyStateUpdated();
    }


    // -------------------
    //  Gestures handling
    // -------------------

    @Override
    public boolean onTouch(@NonNull View view, @NonNull MotionEvent event) {
        MotionEvent viewportEvent = MotionEvent.obtain(event);
        viewportEvent.offsetLocation(-view.getPaddingLeft(), -view.getPaddingTop());

        boolean result = mGestureDetector.onTouchEvent(viewportEvent);
        result |= mScaleDetector.onTouchEvent(viewportEvent);
        result |= mRotateDetector.onTouchEvent(viewportEvent);

        if (viewportEvent.getActionMasked() == MotionEvent.ACTION_UP
                || viewportEvent.getActionMasked() == MotionEvent.ACTION_CANCEL) {
            onUpOrCancel(viewportEvent);
        }

        if (mStateScroller.isFinished()) {
            mStateController.restrictStateBounds(mState, mPrevState, mPivotX, mPivotY, true, true);
        }

        if (!mState.equals(mPrevState)) {
            mPrevState.set(mState);
            notifyStateUpdated();
        }

        viewportEvent.recycle();

        return result;
    }

    protected boolean onDown(MotionEvent e) {
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
        mPivotX = mPivotY = Float.NaN;
        animateStateTo(s);
    }

    protected boolean onSingleTapUp(MotionEvent e) {
        return mGestureListener != null && mGestureListener.onSingleTapUp(e);
    }

    protected void onLongPress(MotionEvent e) {
        if (mGestureListener != null) mGestureListener.onLongPress(e);
    }

    protected boolean onScroll(MotionEvent e1, MotionEvent e2, float dX, float dY) {
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

    protected boolean onFling(MotionEvent e1, MotionEvent e2, float vX, float vY) {
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
        mAnimationEngine.start();

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

    protected boolean onSingleTapConfirmed(MotionEvent e) {
        return mGestureListener != null && mGestureListener.onSingleTapConfirmed(e);
    }

    protected boolean onDoubleTapEvent(MotionEvent e) {
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

    protected boolean onScaleBegin(ScaleGestureDetector detector) {
        mIsScaleDetected = mSettings.isZoomEnabled();
        return mIsScaleDetected;
    }

    protected boolean onScale(ScaleGestureDetector detector) {
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

    protected void onScaleEnd(ScaleGestureDetector detector) {
        mIsScaleDetected = false;

        if (!mSettings.isZoomEnabled()) return;

        // Scroll can still be in place, so we should preserver overscroll
        State s = mStateController.restrictStateBoundsCopy(mState, mPivotX, mPivotY, true, false);
        animateStateTo(s);
    }

    protected boolean onRotationBegin(RotationGestureDetector detector) {
        return mSettings.isRotationEnabled();
    }

    protected boolean onRotate(RotationGestureDetector detector) {
        if (!mSettings.isRotationEnabled() || !mStateScroller.isFinished()) return true;

        mState.rotateBy(detector.getRotationDelta(), detector.getFocusX(), detector.getFocusY());

        return true;
    }

    protected void onRotationEnd(RotationGestureDetector detector) {
        // No-op
    }

    protected void onFlingAnimationFinished() {
        State endState = mStateController.restrictStateBoundsCopy(mState);
        animateStateTo(endState);
    }

    protected void onStateAnimationFinished() {
        // no-op
    }

    /**
     * Animation engine implementation to animate state changes
     */
    private class LocalAnimationEngine extends AnimationEngine {
        public LocalAnimationEngine(@NonNull View view) {
            super(view);
        }

        @Override
        public boolean onStep() {
            boolean shouldProceed = false;

            if (!mFlingScroller.isFinished()) {
                if (mFlingScroller.computeScrollOffset()) {
                    float lastX = mState.getX(), lastY = mState.getY();

                    float x = mFlingScroller.getCurrX();
                    float y = mFlingScroller.getCurrY();

                    onFlingScroll(lastX, lastY, x, y);

                    if (State.equals(lastX, mState.getX()) && State.equals(lastY, mState.getY())) {
                        stopFlingAnimation();
                    }

                    shouldProceed = true;
                }

                if (mFlingScroller.isFinished()) {
                    onFlingAnimationFinished();
                }
            }

            if (!mStateScroller.isFinished()) {
                mStateScroller.computeScroll();
                float factor = mStateScroller.getCurr();
                StateController.interpolate(mState, mStateStart, mStateEnd, factor);
                shouldProceed = true;

                if (mStateScroller.isFinished()) {
                    onStateAnimationFinished();
                }
            }

            if (shouldProceed) notifyStateUpdated();

            return shouldProceed;
        }
    }


    // -------------------
    //  Listeners
    // -------------------

    /**
     * State changes listener
     */
    public interface OnStateChangeListener {
        void onStateChanged(State state);

        void onStateReset(State oldState, State newState);
    }

    /**
     * Listener for different touch events
     */
    public interface OnGestureListener {
        /**
         * See {@link GestureDetector.OnGestureListener#onDown(MotionEvent)}
         */
        void onDown(MotionEvent e);

        /**
         * See {@link GestureDetector.OnGestureListener#onSingleTapUp(MotionEvent)}
         */
        boolean onSingleTapUp(MotionEvent e);

        /**
         * See {@link GestureDetector.OnDoubleTapListener#onSingleTapConfirmed(MotionEvent)}
         */
        boolean onSingleTapConfirmed(MotionEvent e);

        /**
         * See {@link GestureDetector.OnGestureListener#onLongPress(MotionEvent)}.
         * <p/>
         * Note, that long press is disabled by default, use {@link #setLongPressEnabled(boolean)}
         * to enable it.
         */
        void onLongPress(MotionEvent e);

        /**
         * See {@link GestureDetector.OnDoubleTapListener#onDoubleTap(MotionEvent)}
         */
        boolean onDoubleTap(MotionEvent e);
    }

    /**
     * Simple implementation of {@link GestureController.OnGestureListener}
     */
    public static class SimpleOnGestureListener implements OnGestureListener {
        /**
         * {@inheritDoc}
         */
        @Override
        public void onDown(MotionEvent e) {
            // no-op
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            return false;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            return false;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void onLongPress(MotionEvent e) {
            // no-op
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            return false;
        }
    }


    /**
     * All listeners in one class.
     * It will also allow us to make all methods protected to cleanup public API.
     */
    private class InternalGesturesListener implements
            GestureDetector.OnGestureListener,
            GestureDetector.OnDoubleTapListener,
            ScaleGestureDetector.OnScaleGestureListener,
            RotationGestureDetector.OnRotationGestureListener {

        @Override
        public boolean onSingleTapConfirmed(@NonNull MotionEvent e) {
            return GestureController.this.onSingleTapConfirmed(e);
        }

        @Override
        public boolean onDoubleTap(@NonNull MotionEvent e) {
            return false;
        }

        @Override
        public boolean onDoubleTapEvent(@NonNull MotionEvent e) {
            return GestureController.this.onDoubleTapEvent(e);
        }

        @Override
        public boolean onDown(@NonNull MotionEvent e) {
            return GestureController.this.onDown(e);
        }

        @Override
        public void onShowPress(@NonNull MotionEvent e) {
            // No-op
        }

        @Override
        public boolean onSingleTapUp(@NonNull MotionEvent e) {
            return GestureController.this.onSingleTapUp(e);
        }

        @Override
        public boolean onScroll(@NonNull MotionEvent e1, @NonNull MotionEvent e2,
                                float distanceX, float distanceY) {
            return GestureController.this.onScroll(e1, e2, distanceX, distanceY);
        }

        @Override
        public void onLongPress(@NonNull MotionEvent e) {
            GestureController.this.onLongPress(e);
        }

        @Override
        public boolean onFling(@NonNull MotionEvent e1, @NonNull MotionEvent e2,
                               float velocityX, float velocityY) {
            return GestureController.this.onFling(e1, e2, velocityX, velocityY);
        }

        @Override
        public boolean onRotate(@NonNull RotationGestureDetector detector) {
            return GestureController.this.onRotate(detector);
        }

        @Override
        public boolean onRotationBegin(@NonNull RotationGestureDetector detector) {
            return GestureController.this.onRotationBegin(detector);
        }

        @Override
        public void onRotationEnd(@NonNull RotationGestureDetector detector) {
            GestureController.this.onRotationEnd(detector);
        }

        @Override
        public boolean onScale(@NonNull ScaleGestureDetector detector) {
            return GestureController.this.onScale(detector);
        }

        @Override
        public boolean onScaleBegin(@NonNull ScaleGestureDetector detector) {
            return GestureController.this.onScaleBegin(detector);
        }

        @Override
        public void onScaleEnd(@NonNull ScaleGestureDetector detector) {
            GestureController.this.onScaleEnd(detector);
        }

    }

}
