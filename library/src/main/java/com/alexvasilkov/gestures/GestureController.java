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
    private static final float FLING_COEFFICIENT = 0.9f;

    // Control constants converted to pixels
    private final float zoomGestureMinSpan;
    private final int touchSlop;
    private final int minimumVelocity;
    private final int maximumVelocity;

    private final List<OnStateChangeListener> stateListeners = new ArrayList<>();
    private OnGestureListener gestureListener;

    private final AnimationEngine animationEngine;

    // Various gesture detectors
    private final GestureDetector gestureDetector;
    private final ScaleGestureDetector scaleDetector;
    private final RotationGestureDetector rotateDetector;

    private boolean isScrollDetected;
    private boolean isScaleDetected;
    private float pivotX = Float.NaN;
    private float pivotY = Float.NaN;
    private boolean isStateChangedDuringTouch;
    private boolean isRestrictZoomRequested;
    private boolean isRestrictRotationRequested;
    private boolean isAnimatingInBounds;

    private final OverScroller flingScroller;
    private final FloatScroller stateScroller;

    private final MovementBounds flingBounds = new MovementBounds();
    private final State prevState = new State();
    private final State stateStart = new State();
    private final State stateEnd = new State();

    private final Settings settings;
    private final State state = new State();
    private final StateController stateController;

    public GestureController(@NonNull View view) {
        Context context = view.getContext();
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        zoomGestureMinSpan = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, ZOOM_GESTURE_MIN_SPAN_DP, metrics);

        settings = new Settings();
        stateController = new StateController(settings);

        animationEngine = new LocalAnimationEngine(view);
        InternalGesturesListener internalListener = new InternalGesturesListener();
        gestureDetector = new GestureDetector(context, internalListener);
        gestureDetector.setIsLongpressEnabled(false);
        scaleDetector = new ScaleGestureDetectorFixed(context, internalListener);
        rotateDetector = new RotationGestureDetector(context, internalListener);

        flingScroller = new OverScroller(context);
        stateScroller = new FloatScroller();

        final ViewConfiguration configuration = ViewConfiguration.get(context);
        touchSlop = configuration.getScaledTouchSlop();
        minimumVelocity = configuration.getScaledMinimumFlingVelocity();
        maximumVelocity = configuration.getScaledMaximumFlingVelocity();
    }

    /**
     * Sets listener for basic touch events.
     * <p/>
     * See also {@link GestureController.OnGestureListener}
     */
    @SuppressWarnings("unused") // Public API
    public void setOnGesturesListener(@Nullable OnGestureListener listener) {
        gestureListener = listener;
    }

    /**
     * Adds listener for state changes.
     * <p/>
     * See also {@link OnStateChangeListener}
     */
    public void addOnStateChangeListener(OnStateChangeListener listener) {
        stateListeners.add(listener);
    }

    /**
     * Removes listener for state changes.
     * <p/>
     * See also {@link #addOnStateChangeListener(OnStateChangeListener)}
     */
    @SuppressWarnings("unused") // Public API
    public void removeOnStateChangeListener(OnStateChangeListener listener) {
        stateListeners.remove(listener);
    }

    /**
     * Sets whether long press is enabled or not. Long press is disabled by default.
     * <p/>
     * See also {@link GestureController.OnGestureListener#onLongPress(android.view.MotionEvent)}
     */
    @SuppressWarnings("unused") // Public API
    public void setLongPressEnabled(boolean enabled) {
        gestureDetector.setIsLongpressEnabled(enabled);
    }

    /**
     * Returns settings that can be updated.
     * <p/>
     * Note: call {@link #updateState()}, {@link #resetState()} or {@link #animateKeepInBounds()}
     * after settings was changed to correctly apply state restrictions.
     */
    public Settings getSettings() {
        return settings;
    }

    /**
     * Returns current state. In most cases you should not modify state directly,
     * use one of the methods provided in {@link StateController} instead.
     * <p/>
     * If current state was changed outside {@link GestureController}
     * you should call {@link GestureController#updateState()} or {@link #animateKeepInBounds()}
     * to properly apply changes.
     */
    public State getState() {
        return state;
    }

    /**
     * Returns state controller that can be used externally.
     */
    public StateController getStateController() {
        return stateController;
    }

    /**
     * Applies state restrictions and notifies
     * {@link OnStateChangeListener} listeners.
     */
    public void updateState() {
        boolean reset = stateController.updateState(state);
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
        boolean reset = stateController.resetState(state);
        if (reset) {
            notifyStateReset();
        } else {
            notifyStateUpdated();
        }
    }

    /**
     * Sets pivot point for zooming when keeping image in bounds.
     *
     * @see #animateKeepInBounds()
     * @see #animateStateTo(State)
     */
    public void setPivot(float pivotX, float pivotY) {
        this.pivotX = pivotX;
        this.pivotY = pivotY;
    }

    /**
     * Animates to correct position withing bounds.
     *
     * @return {@code true} if animation started, {@code false} otherwise. Animation may
     * not be started if image already withing bounds.
     */
    public boolean animateKeepInBounds() {
        return animateStateTo(state, true);
    }

    /**
     * Animates current state to provided end state.
     *
     * @return {@code true} if animation started, {@code false} otherwise. Animation may
     * not be started if end state is {@code null} or equals to current state (after bounds
     * restrictions are applied).
     */
    public boolean animateStateTo(@Nullable State endState) {
        return animateStateTo(endState, true);
    }

    private boolean animateStateTo(@Nullable State endState, boolean keepInBounds) {
        if (endState == null) {
            return false;
        }

        State endStateFixed = null;
        if (keepInBounds) {
            endStateFixed = stateController.restrictStateBoundsCopy(
                    endState, prevState, pivotX, pivotY, false, false, true);
        }
        if (endStateFixed == null) {
            endStateFixed = endState;
        }

        if (endStateFixed.equals(state)) {
            return false; // Nothing to animate
        }

        stopAllAnimations();

        isAnimatingInBounds = keepInBounds;
        stateStart.set(state);
        stateEnd.set(endStateFixed);
        stateScroller.startScroll(0f, 1f);
        animationEngine.start();

        return true;
    }

    public boolean isAnimatingState() {
        return !stateScroller.isFinished();
    }

    public boolean isAnimatingFling() {
        return !flingScroller.isFinished();
    }

    @SuppressWarnings("unused") // Public API
    public boolean isAnimating() {
        return isAnimatingState() || isAnimatingFling();
    }

    public void stopStateAnimation() {
        if (isAnimatingState()) {
            stateScroller.forceFinished();
            onStateAnimationFinished(true);
        }
    }

    public void stopFlingAnimation() {
        if (isAnimatingFling()) {
            flingScroller.forceFinished(true);
            onFlingAnimationFinished(true);
        }
    }

    public void stopAllAnimations() {
        stopStateAnimation();
        stopFlingAnimation();
    }

    @SuppressWarnings("UnusedParameters") // Public API (can be overridden)
    protected void onStateAnimationFinished(boolean forced) {
        isAnimatingInBounds = false;
    }

    protected void onFlingAnimationFinished(boolean forced) {
        if (!forced) {
            animateKeepInBounds();
        }
    }

    protected void notifyStateUpdated() {
        prevState.set(state);
        for (OnStateChangeListener listener : stateListeners) {
            listener.onStateChanged(state);
        }
    }

    protected void notifyStateReset() {
        for (OnStateChangeListener listener : stateListeners) {
            listener.onStateReset(prevState, state);
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

        boolean result = gestureDetector.onTouchEvent(viewportEvent);
        result |= scaleDetector.onTouchEvent(viewportEvent);
        result |= rotateDetector.onTouchEvent(viewportEvent);

        if (isStateChangedDuringTouch) {
            isStateChangedDuringTouch = false;

            stateController.restrictStateBounds(
                    state, prevState, pivotX, pivotY, true, true, false);

            if (!state.equals(prevState)) {
                notifyStateUpdated();
            }
        }

        if (isRestrictZoomRequested || isRestrictRotationRequested) {
            isRestrictZoomRequested = false;
            isRestrictRotationRequested = false;

            State restrictedState = stateController.restrictStateBoundsCopy(
                    state, prevState, pivotX, pivotY, true, false, true);
            animateStateTo(restrictedState, false);
        }

        if (viewportEvent.getActionMasked() == MotionEvent.ACTION_UP
                || viewportEvent.getActionMasked() == MotionEvent.ACTION_CANCEL) {
            onUpOrCancel(viewportEvent);
        }

        viewportEvent.recycle();

        return result;
    }

    protected boolean onDown(@NonNull MotionEvent event) {
        stopFlingAnimation();

        isScrollDetected = false;
        isScaleDetected = false;

        if (gestureListener != null) {
            gestureListener.onDown(event);
        }

        return settings.isEnabled();
    }

    protected void onUpOrCancel(@NonNull MotionEvent event) {
        if (!isAnimatingFling() && !isAnimatingInBounds) {
            animateKeepInBounds();
        }

        if (gestureListener != null) {
            gestureListener.onUpOrCancel(event);
        }
    }

    protected boolean onSingleTapUp(@NonNull MotionEvent event) {
        return gestureListener != null && gestureListener.onSingleTapUp(event);
    }

    protected void onLongPress(@NonNull MotionEvent event) {
        if (gestureListener != null) {
            gestureListener.onLongPress(event);
        }
    }

    protected boolean onScroll(@NonNull MotionEvent e1, @NonNull MotionEvent e2,
            float dx, float dy) {

        if (!settings.isPanEnabled() || isAnimatingState()) {
            return false;
        }

        if (!isScrollDetected) {
            isScrollDetected = Math.abs(e2.getX() - e1.getX()) > touchSlop
                    || Math.abs(e2.getY() - e1.getY()) > touchSlop;

            // First scroll event can stutter a bit, so we will ignore it for smoother scrolling
            if (isScrollDetected) {
                return true;
            }
        }

        if (isScrollDetected) {
            // Only scrolling if we are not zoomed less than min zoom
            if (State.compare(state.getZoom(), stateController.getEffectiveMinZoom()) >= 0) {
                state.translateBy(-dx, -dy);
                isStateChangedDuringTouch = true;
            }
        }

        return isScrollDetected;
    }

    protected boolean onFling(@NonNull MotionEvent e1, @NonNull MotionEvent e2,
            float vx, float vy) {

        if (!settings.isPanEnabled() || isAnimatingState()) {
            return false;
        }

        stopFlingAnimation();

        // Fling bounds including current position
        flingBounds.set(stateController.getMovementBounds(state));
        flingBounds.union(state.getX(), state.getY());

        flingScroller.fling(
                Math.round(state.getX()), Math.round(state.getY()),
                limitFlingVelocity(vx * FLING_COEFFICIENT),
                limitFlingVelocity(vy * FLING_COEFFICIENT),
                Integer.MIN_VALUE, Integer.MAX_VALUE,
                Integer.MIN_VALUE, Integer.MAX_VALUE);
        animationEngine.start();

        return true;
    }

    private int limitFlingVelocity(float velocity) {
        if (Math.abs(velocity) < minimumVelocity) {
            return 0;
        } else if (Math.abs(velocity) >= maximumVelocity) {
            return (int) Math.signum(velocity) * maximumVelocity;
        } else {
            return Math.round(velocity);
        }
    }

    /**
     * @return true if state was changed, false otherwise.
     */
    protected boolean onFlingScroll(int dx, int dy) {
        float prevX = state.getX();
        float prevY = state.getY();
        float toX = prevX + dx;
        float toY = prevY + dy;

        if (settings.isRestrictBounds()) {
            PointF pos = flingBounds.restrict(toX, toY);
            toX = pos.x;
            toY = pos.y;
        }

        state.translateTo(toX, toY);
        return !State.equals(prevX, toX) || !State.equals(prevY, toY);
    }

    protected boolean onSingleTapConfirmed(MotionEvent event) {
        return gestureListener != null && gestureListener.onSingleTapConfirmed(event);
    }

    protected boolean onDoubleTapEvent(MotionEvent event) {
        if (!settings.isDoubleTapEnabled()) {
            return false;
        }

        if (event.getActionMasked() != MotionEvent.ACTION_UP) {
            return false;
        }

        // ScaleGestureDetector can perform zoom by "double tap & drag" since KITKAT,
        // so we should suppress our double tap in this case
        if (isScaleDetected) {
            return false;
        }

        // Let user redefine double tap
        if (gestureListener != null && gestureListener.onDoubleTap(event)) {
            return true;
        }

        animateStateTo(stateController.toggleMinMaxZoom(state, event.getX(), event.getY()));
        return true;
    }

    protected boolean onScaleBegin(ScaleGestureDetector detector) {
        isScaleDetected = settings.isZoomEnabled();
        return isScaleDetected;
    }

    protected boolean onScale(ScaleGestureDetector detector) {
        if (settings.isZoomEnabled() && !isAnimatingState()) {
            if (detector.getCurrentSpan() > zoomGestureMinSpan) {
                pivotX = detector.getFocusX();
                pivotY = detector.getFocusY();
                state.zoomBy(detector.getScaleFactor(), pivotX, pivotY);
                isStateChangedDuringTouch = true;
            }
        }

        return true;
    }

    @SuppressWarnings("UnusedParameters") // Public API (can be overridden)
    protected void onScaleEnd(ScaleGestureDetector detector) {
        isScaleDetected = false;
        isRestrictZoomRequested = true;
    }

    protected boolean onRotationBegin(RotationGestureDetector detector) {
        return settings.isRotationEnabled();
    }

    protected boolean onRotate(RotationGestureDetector detector) {
        if (settings.isRotationEnabled() && !isAnimatingState()) {
            pivotX = detector.getFocusX();
            pivotY = detector.getFocusY();
            state.rotateBy(detector.getRotationDelta(), pivotX, pivotY);
            isStateChangedDuringTouch = true;
        }

        return true;
    }

    @SuppressWarnings("UnusedParameters") // Public API (can be overridden)
    protected void onRotationEnd(RotationGestureDetector detector) {
        isRestrictRotationRequested = true;
    }

    /**
     * Animation engine implementation to animate state changes.
     */
    private class LocalAnimationEngine extends AnimationEngine {
        LocalAnimationEngine(@NonNull View view) {
            super(view);
        }

        @Override
        public boolean onStep() {
            boolean shouldProceed = false;

            if (isAnimatingFling()) {
                int prevX = flingScroller.getCurrX();
                int prevY = flingScroller.getCurrY();

                if (flingScroller.computeScrollOffset()) {
                    int dx = flingScroller.getCurrX() - prevX;
                    int dy = flingScroller.getCurrY() - prevY;

                    if (!onFlingScroll(dx, dy)) {
                        stopFlingAnimation();
                    }

                    shouldProceed = true;
                }

                if (!isAnimatingFling()) {
                    onFlingAnimationFinished(false);
                }
            }

            if (isAnimatingState()) {
                stateScroller.computeScroll();
                float factor = stateScroller.getCurr();
                StateController.interpolate(state, stateStart, stateEnd, factor);
                shouldProceed = true;

                if (!isAnimatingState()) {
                    onStateAnimationFinished(false);
                }
            }

            if (shouldProceed) {
                notifyStateUpdated();
            }

            return shouldProceed;
        }
    }


    // -------------------
    //  Listeners
    // -------------------

    /**
     * State changes listener.
     */
    public interface OnStateChangeListener {
        void onStateChanged(State state);

        void onStateReset(State oldState, State newState);
    }

    /**
     * Listener for different touch events.
     */
    public interface OnGestureListener {
        /**
         * See {@link GestureDetector.OnGestureListener#onDown(MotionEvent)}.
         */
        void onDown(@NonNull MotionEvent event);

        /**
         * See {@link GestureDetector.OnGestureListener#onDown(MotionEvent)}.
         */
        void onUpOrCancel(@NonNull MotionEvent event);

        /**
         * See {@link GestureDetector.OnGestureListener#onSingleTapUp(MotionEvent)}.
         *
         * @return true if event was consumed, false otherwise.
         */
        boolean onSingleTapUp(@NonNull MotionEvent event);

        /**
         * See {@link GestureDetector.OnDoubleTapListener#onSingleTapConfirmed(MotionEvent)}.
         *
         * @return true if event was consumed, false otherwise.
         */
        boolean onSingleTapConfirmed(@NonNull MotionEvent event);

        /**
         * See {@link GestureDetector.OnGestureListener#onLongPress(MotionEvent)}.
         * <p/>
         * Note, that long press is disabled by default, use {@link #setLongPressEnabled(boolean)}
         * to enable it.
         */
        void onLongPress(@NonNull MotionEvent event);

        /**
         * See {@link GestureDetector.OnDoubleTapListener#onDoubleTap(MotionEvent)}.
         *
         * @return true if event was consumed, false otherwise.
         */
        boolean onDoubleTap(@NonNull MotionEvent event);
    }

    /**
     * Simple implementation of {@link GestureController.OnGestureListener}.
     */
    public static class SimpleOnGestureListener implements OnGestureListener {
        /**
         * {@inheritDoc}
         */
        @Override
        public void onDown(@NonNull MotionEvent event) {
            // no-op
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void onUpOrCancel(@NonNull MotionEvent event) {
            // no-op
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean onSingleTapUp(@NonNull MotionEvent event) {
            return false;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean onSingleTapConfirmed(@NonNull MotionEvent event) {
            return false;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void onLongPress(@NonNull MotionEvent event) {
            // no-op
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean onDoubleTap(@NonNull MotionEvent event) {
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
        public boolean onSingleTapConfirmed(@NonNull MotionEvent event) {
            return GestureController.this.onSingleTapConfirmed(event);
        }

        @Override
        public boolean onDoubleTap(@NonNull MotionEvent event) {
            return false;
        }

        @Override
        public boolean onDoubleTapEvent(@NonNull MotionEvent event) {
            return GestureController.this.onDoubleTapEvent(event);
        }

        @Override
        public boolean onDown(@NonNull MotionEvent event) {
            return GestureController.this.onDown(event);
        }

        @Override
        public void onShowPress(@NonNull MotionEvent event) {
            // No-op
        }

        @Override
        public boolean onSingleTapUp(@NonNull MotionEvent event) {
            return GestureController.this.onSingleTapUp(event);
        }

        @Override
        public boolean onScroll(@NonNull MotionEvent e1, @NonNull MotionEvent e2,
                float distanceX, float distanceY) {
            return GestureController.this.onScroll(e1, e2, distanceX, distanceY);
        }

        @Override
        public void onLongPress(@NonNull MotionEvent event) {
            GestureController.this.onLongPress(event);
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
