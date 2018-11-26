package com.alexvasilkov.gestures;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.PointF;
import android.graphics.RectF;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewParent;
import android.widget.OverScroller;

import com.alexvasilkov.gestures.internal.AnimationEngine;
import com.alexvasilkov.gestures.internal.ExitController;
import com.alexvasilkov.gestures.internal.MovementBounds;
import com.alexvasilkov.gestures.internal.detectors.RotationGestureDetector;
import com.alexvasilkov.gestures.internal.detectors.ScaleGestureDetectorFixed;
import com.alexvasilkov.gestures.utils.FloatScroller;
import com.alexvasilkov.gestures.utils.MathUtils;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Handles touch events to update view's position state ({@link State}) based on current
 * setup ({@link Settings}).<br>
 * Settings can be obtained and altered through {@link #getSettings()}.<br>
 * Note, that some settings are required in order to correctly update state, see {@link Settings}.
 * <p>
 * This class implements {@link View.OnTouchListener} to delegate touches from view to controller.
 * <p>
 * State can also be manipulated directly with {@link #getState()}, {@link #updateState()}
 * and {@link #resetState()}. You can also access {@link #getStateController()} for some additional
 * stuff.
 * <p>
 * State can be animated with {@link #animateStateTo(State)} method.<br>
 * See also {@link #stopFlingAnimation()}, {@link #stopStateAnimation()}
 * and {@link #stopAllAnimations()} methods.
 * <p>
 * All state changes will be passed to {@link OnStateChangeListener OnStateChangeListener}.
 * See {@link #addOnStateChangeListener(OnStateChangeListener) addOnStateChangeListener} and
 * {@link #removeOnStateChangeListener(OnStateChangeListener) removeOnStateChangeListener} methods.
 * <p>
 * Additional touch events can be listened with {@link OnGestureListener OnGestureListener} and
 * {@link SimpleOnGestureListener SimpleOnGestureListener} using
 * {@link #setOnGesturesListener(OnGestureListener) setOnGesturesListener} method.
 * <p>
 * State source changes (whether state is being changed by user or by animation) can be
 * listened with {@link OnStateSourceChangeListener} using
 * {@link #setOnStateSourceChangeListener(OnStateSourceChangeListener)} method.
 */
public class GestureController implements View.OnTouchListener {

    private static final float FLING_COEFFICIENT = 0.9f;

    // Temporary objects
    private static final PointF tmpPointF = new PointF();
    private static final RectF tmpRectF = new RectF();
    private static final float[] tmpPointArr = new float[2];

    // Control constants converted to pixels
    private final int touchSlop;
    private final int minVelocity;
    private final int maxVelocity;

    private OnGestureListener gestureListener;
    private OnStateSourceChangeListener sourceListener;
    private final List<OnStateChangeListener> stateListeners = new ArrayList<>();

    private final AnimationEngine animationEngine;

    // Various gesture detectors
    private final GestureDetector gestureDetector;
    private final ScaleGestureDetector scaleDetector;
    private final RotationGestureDetector rotateDetector;

    private boolean isInterceptTouchCalled;
    private boolean isInterceptTouchDisallowed;
    private boolean isScrollDetected;
    private boolean isScaleDetected;
    private boolean isRotationDetected;

    private float pivotX = Float.NaN;
    private float pivotY = Float.NaN;
    private float endPivotX = Float.NaN;
    private float endPivotY = Float.NaN;

    private boolean isStateChangedDuringTouch;
    private boolean isRestrictZoomRequested;
    private boolean isRestrictRotationRequested;
    private boolean isAnimatingInBounds;

    private StateSource stateSource = StateSource.NONE;

    private final OverScroller flingScroller;
    private final FloatScroller stateScroller;

    private final MovementBounds flingBounds;
    private final State stateStart = new State();
    private final State stateEnd = new State();

    private final View targetView;
    private final Settings settings;
    private final State state = new State();
    private final State prevState = new State();
    private final StateController stateController;
    private final ExitController exitController;

    public GestureController(@NonNull View view) {
        final Context context = view.getContext();

        targetView = view;
        settings = new Settings();
        stateController = new StateController(settings);

        animationEngine = new LocalAnimationEngine(view);
        InternalGesturesListener internalListener = new InternalGesturesListener();
        gestureDetector = new GestureDetector(context, internalListener);
        scaleDetector = new ScaleGestureDetectorFixed(context, internalListener);
        rotateDetector = new RotationGestureDetector(context, internalListener);

        exitController = new ExitController(view, this);

        flingScroller = new OverScroller(context);
        stateScroller = new FloatScroller();

        flingBounds = new MovementBounds(settings);

        final ViewConfiguration configuration = ViewConfiguration.get(context);
        touchSlop = configuration.getScaledTouchSlop();
        minVelocity = configuration.getScaledMinimumFlingVelocity();
        maxVelocity = configuration.getScaledMaximumFlingVelocity();
    }

    /**
     * Sets listener for basic touch events.
     *
     * @param listener Gestures listener
     * @see GestureController.OnGestureListener
     */
    @SuppressWarnings({ "unused", "WeakerAccess" }) // Public API
    public void setOnGesturesListener(@Nullable OnGestureListener listener) {
        gestureListener = listener;
    }

    /**
     * Sets listener for state source changes.
     *
     * @param listener State's source changes listener
     * @see OnStateSourceChangeListener
     */
    @SuppressWarnings({ "unused", "WeakerAccess" }) // Public API
    public void setOnStateSourceChangeListener(@Nullable OnStateSourceChangeListener listener) {
        sourceListener = listener;
    }

    /**
     * Adds listener for state changes.
     *
     * @param listener State changes listener
     * @see OnStateChangeListener
     */
    public void addOnStateChangeListener(@NonNull OnStateChangeListener listener) {
        stateListeners.add(listener);
    }

    /**
     * Removes listener for state changes.
     *
     * @param listener State changes listener to be removed
     * @see #addOnStateChangeListener(OnStateChangeListener)
     */
    @SuppressWarnings({ "unused", "WeakerAccess" }) // Public API
    public void removeOnStateChangeListener(OnStateChangeListener listener) {
        stateListeners.remove(listener);
    }

    /**
     * @param enabled Whether long press should be enabled or not
     * @deprecated In order to enable long clicks you should either set
     * {@link View#setOnLongClickListener(View.OnLongClickListener)} or use
     * {@link View#setLongClickable(boolean)}.
     */
    @SuppressWarnings({ "unused", "WeakerAccess" }) // Public API
    @Deprecated
    public void setLongPressEnabled(boolean enabled) {
        targetView.setLongClickable(true);
    }

    /**
     * Returns settings that can be updated.
     * <p>
     * Note: call {@link #updateState()}, {@link #resetState()} or {@link #animateKeepInBounds()}
     * after settings was changed to correctly apply state restrictions.
     *
     * @return Gesture view's settings
     */
    public Settings getSettings() {
        return settings;
    }

    /**
     * Current state.
     * <p>
     * If this state is changed from outside you should call
     * {@link GestureController#updateState()} or {@link #animateKeepInBounds()}
     * to properly apply changes.
     *
     * @return Current state
     */
    public State getState() {
        return state;
    }

    /**
     * @return State controller to get computed min/max zoom levels or calculate movement area
     */
    @SuppressWarnings("WeakerAccess") // Public API
    public StateController getStateController() {
        return stateController;
    }

    /**
     * Applies state restrictions and notifies {@link OnStateChangeListener} listeners.
     */
    public void updateState() {
        // Applying zoom patch (needed in case if image size is changed)
        stateController.applyZoomPatch(state);
        stateController.applyZoomPatch(prevState);
        stateController.applyZoomPatch(stateStart);
        stateController.applyZoomPatch(stateEnd);
        exitController.applyZoomPatch();

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
     * <p>
     * Should be called when image size is changed.
     * <p>
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
     * @param pivotX Pivot point's X coordinate
     * @param pivotY Pivot point's Y coordinate
     * @see #animateKeepInBounds()
     * @see #animateStateTo(State)
     */
    public void setPivot(float pivotX, float pivotY) {
        this.pivotX = pivotX;
        this.pivotY = pivotY;
    }

    /**
     * Animates to correct position withing the bounds.
     *
     * @return {@code true} if animation started, {@code false} otherwise. Animation may
     * not be started if image already withing the bounds.
     */
    @SuppressWarnings("WeakerAccess") // Public API
    public boolean animateKeepInBounds() {
        return animateStateTo(state, true);
    }

    /**
     * Animates current state to provided end state.
     *
     * @param endState End state
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

        State endStateRestricted = null;
        if (keepInBounds) {
            endStateRestricted = stateController.restrictStateBoundsCopy(
                    endState, prevState, pivotX, pivotY, false, false, true);
        }
        if (endStateRestricted == null) {
            endStateRestricted = endState;
        }

        if (endStateRestricted.equals(state)) {
            return false; // Nothing to animate
        }

        stopAllAnimations();

        isAnimatingInBounds = keepInBounds;
        stateStart.set(state);
        stateEnd.set(endStateRestricted);

        // Computing new position of pivot point for correct state interpolation
        if (!Float.isNaN(pivotX) && !Float.isNaN(pivotY)) {
            tmpPointArr[0] = pivotX;
            tmpPointArr[1] = pivotY;
            MathUtils.computeNewPosition(tmpPointArr, stateStart, stateEnd);
            endPivotX = tmpPointArr[0];
            endPivotY = tmpPointArr[1];
        }

        stateScroller.setDuration(settings.getAnimationsDuration());
        stateScroller.startScroll(0f, 1f);
        animationEngine.start();

        notifyStateSourceChanged();

        return true;
    }

    @SuppressWarnings("WeakerAccess") // Public API
    public boolean isAnimatingState() {
        return !stateScroller.isFinished();
    }

    @SuppressWarnings("WeakerAccess") // Public API
    public boolean isAnimatingFling() {
        return !flingScroller.isFinished();
    }

    @SuppressWarnings({ "unused", "WeakerAccess" }) // Public API
    public boolean isAnimating() {
        return isAnimatingState() || isAnimatingFling();
    }

    @SuppressWarnings("WeakerAccess") // Public API
    public void stopStateAnimation() {
        if (isAnimatingState()) {
            stateScroller.forceFinished();
            onStateAnimationFinished(true);
        }
    }

    @SuppressWarnings("WeakerAccess") // Public API
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

    @SuppressWarnings({ "UnusedParameters", "WeakerAccess" }) // Public API (can be overridden)
    protected void onStateAnimationFinished(boolean forced) {
        isAnimatingInBounds = false;
        pivotX = Float.NaN;
        pivotY = Float.NaN;
        notifyStateSourceChanged();
    }

    @SuppressWarnings("WeakerAccess") // Public API (can be overridden)
    protected void onFlingAnimationFinished(boolean forced) {
        if (!forced) {
            animateKeepInBounds();
        }
        notifyStateSourceChanged();
    }

    @SuppressWarnings("WeakerAccess") // Public API (can be overridden)
    protected void notifyStateUpdated() {
        prevState.set(state);
        for (OnStateChangeListener listener : stateListeners) {
            listener.onStateChanged(state);
        }
    }

    @SuppressWarnings("WeakerAccess") // Public API (can be overridden)
    protected void notifyStateReset() {
        exitController.stopDetection();
        for (OnStateChangeListener listener : stateListeners) {
            listener.onStateReset(prevState, state);
        }
        notifyStateUpdated();
    }

    private void notifyStateSourceChanged() {
        StateSource type = StateSource.NONE;
        if (isAnimating()) {
            type = StateSource.ANIMATION;
        } else if (isScrollDetected || isScaleDetected || isRotationDetected) {
            type = StateSource.USER;
        }

        if (stateSource != type) {
            stateSource = type;
            if (sourceListener != null) {
                sourceListener.onStateSourceChanged(type);
            }
        }
    }


    // -------------------
    //  Gestures handling
    // -------------------

    public boolean onInterceptTouch(@NonNull View view, @NonNull MotionEvent event) {
        isInterceptTouchCalled = true;
        return onTouchInternal(view, event);
    }

    @SuppressLint("ClickableViewAccessibility") // performClick is called in gestures callbacks
    @Override
    public boolean onTouch(@NonNull View view, @NonNull MotionEvent event) {
        if (!isInterceptTouchCalled) { // Preventing duplicate events
            onTouchInternal(view, event);
        }
        isInterceptTouchCalled = false;
        return settings.isEnabled();
    }

    protected boolean onTouchInternal(@NonNull View view, @NonNull MotionEvent event) {
        MotionEvent viewportEvent = MotionEvent.obtain(event);
        viewportEvent.offsetLocation(-view.getPaddingLeft(), -view.getPaddingTop());

        gestureDetector.setIsLongpressEnabled(view.isLongClickable());

        boolean result = gestureDetector.onTouchEvent(viewportEvent);
        scaleDetector.onTouchEvent(viewportEvent);
        rotateDetector.onTouchEvent(viewportEvent);
        result = result || isScaleDetected || isRotationDetected;

        notifyStateSourceChanged();

        if (exitController.isExitDetected()) {
            if (!state.equals(prevState)) {
                notifyStateUpdated();
            }
        }

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

            if (!exitController.isExitDetected()) {
                State restrictedState = stateController.restrictStateBoundsCopy(
                        state, prevState, pivotX, pivotY, true, false, true);
                animateStateTo(restrictedState, false);
            }
        }

        if (viewportEvent.getActionMasked() == MotionEvent.ACTION_UP
                || viewportEvent.getActionMasked() == MotionEvent.ACTION_CANCEL) {
            onUpOrCancel(viewportEvent);
            notifyStateSourceChanged();
        }

        if (!isInterceptTouchDisallowed && shouldDisallowInterceptTouch(viewportEvent)) {
            isInterceptTouchDisallowed = true;

            final ViewParent parent = view.getParent();
            if (parent != null) {
                parent.requestDisallowInterceptTouchEvent(true);
            }
        }

        viewportEvent.recycle();

        return result;
    }

    protected boolean shouldDisallowInterceptTouch(MotionEvent event) {
        if (exitController.isExitDetected()) {
            return true;
        }

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE: {
                // If view can be panned then parent should not intercept touch events.
                // We should check it on DOWN event since parent may quickly take control over us
                // in case of a very fast MOVE action.
                stateController.getMovementArea(state, tmpRectF);
                final boolean isPannable = State.compare(tmpRectF.width(), 0f) > 0
                        || State.compare(tmpRectF.height(), 0f) > 0;

                if (settings.isPanEnabled() && (isPannable || !settings.isRestrictBounds())) {
                    return true;
                }
                break;
            }
            case MotionEvent.ACTION_POINTER_DOWN: {
                // If view can be zoomed or rotated then parent should not intercept touch events.
                return settings.isZoomEnabled() || settings.isRotationEnabled();
            }
            default:
        }

        return false;
    }

    protected boolean onDown(@NonNull MotionEvent event) {
        isInterceptTouchDisallowed = false;

        stopFlingAnimation();

        if (gestureListener != null) {
            gestureListener.onDown(event);
        }

        return false;
    }

    protected void onUpOrCancel(@NonNull MotionEvent event) {
        isScrollDetected = false;
        isScaleDetected = false;
        isRotationDetected = false;

        exitController.onUpOrCancel();

        if (!isAnimatingFling() && !isAnimatingInBounds) {
            animateKeepInBounds();
        }

        if (gestureListener != null) {
            gestureListener.onUpOrCancel(event);
        }
    }

    @SuppressWarnings("WeakerAccess") // Public API (can be overridden)
    protected boolean onSingleTapUp(@NonNull MotionEvent event) {
        // If double tap is not enabled then it should be safe to propagate click event from here
        if (!settings.isDoubleTapEnabled()) {
            targetView.performClick();
        }
        return gestureListener != null && gestureListener.onSingleTapUp(event);
    }

    @SuppressWarnings("WeakerAccess") // Public API (can be overridden)
    protected void onLongPress(@NonNull MotionEvent event) {
        if (settings.isEnabled()) {
            targetView.performLongClick();

            if (gestureListener != null) {
                gestureListener.onLongPress(event);
            }
        }
    }

    protected boolean onScroll(@NonNull MotionEvent e1, @NonNull MotionEvent e2,
            float dx, float dy) {

        if (!settings.isPanEnabled() || isAnimatingState()) {
            return false;
        }

        boolean scrollConsumed = exitController.onScroll(-dx, -dy);
        if (scrollConsumed) {
            return true;
        }

        if (!isScrollDetected) {
            isScrollDetected = Math.abs(e2.getX() - e1.getX()) > touchSlop
                    || Math.abs(e2.getY() - e1.getY()) > touchSlop;

            // First scroll event can stutter a bit, so we will ignore it for smoother scrolling
            if (isScrollDetected) {
                // By returning false here we give children views a chance to intercept this scroll
                return false;
            }
        }

        if (isScrollDetected) {
            state.translateBy(-dx, -dy);
            isStateChangedDuringTouch = true;
        }

        return isScrollDetected;
    }

    protected boolean onFling(@NonNull MotionEvent e1, @NonNull MotionEvent e2,
            float vx, float vy) {

        if (!settings.isPanEnabled() || !settings.isFlingEnabled() || isAnimatingState()) {
            return false;
        }

        boolean flingConsumed = exitController.onFling();
        if (flingConsumed) {
            return true;
        }

        stopFlingAnimation();

        // Fling bounds including current position
        flingBounds.set(state).extend(state.getX(), state.getY());

        flingScroller.fling(
                Math.round(state.getX()), Math.round(state.getY()),
                limitFlingVelocity(vx * FLING_COEFFICIENT),
                limitFlingVelocity(vy * FLING_COEFFICIENT),
                Integer.MIN_VALUE, Integer.MAX_VALUE,
                Integer.MIN_VALUE, Integer.MAX_VALUE);
        animationEngine.start();

        notifyStateSourceChanged();

        return true;
    }

    private int limitFlingVelocity(float velocity) {
        if (Math.abs(velocity) < minVelocity) {
            return 0;
        } else if (Math.abs(velocity) >= maxVelocity) {
            return (int) Math.signum(velocity) * maxVelocity;
        } else {
            return Math.round(velocity);
        }
    }

    /**
     * @param dx Current X offset in the fling scroll
     * @param dy Current Y offset in the fling scroll
     * @return true if state was changed, false otherwise.
     */
    @SuppressWarnings("WeakerAccess") // Public API (can be overridden)
    protected boolean onFlingScroll(int dx, int dy) {
        float prevX = state.getX();
        float prevY = state.getY();
        float toX = prevX + dx;
        float toY = prevY + dy;

        if (settings.isRestrictBounds()) {
            flingBounds.restrict(toX, toY, tmpPointF);
            toX = tmpPointF.x;
            toY = tmpPointF.y;
        }

        state.translateTo(toX, toY);
        return !State.equals(prevX, toX) || !State.equals(prevY, toY);
    }

    @SuppressWarnings("WeakerAccess") // Public API (can be overridden)
    protected boolean onSingleTapConfirmed(MotionEvent event) {
        // If double tap is enabled we should propagate click only if we aren't in a double tap now
        if (settings.isDoubleTapEnabled()) {
            targetView.performClick();
        }
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
        if (isScaleDetected) {
            exitController.onScaleBegin();
        }
        return isScaleDetected;
    }

    @SuppressWarnings("WeakerAccess") // Public API (can be overridden)
    protected boolean onScale(ScaleGestureDetector detector) {
        if (!settings.isZoomEnabled() || isAnimatingState()) {
            return false; // Ignoring scroll if animation is in progress
        }

        final float scaleFactor = detector.getScaleFactor();

        boolean scaleConsumed = exitController.onScale(scaleFactor);
        if (scaleConsumed) {
            return true;
        }

        pivotX = detector.getFocusX();
        pivotY = detector.getFocusY();
        state.zoomBy(scaleFactor, pivotX, pivotY);
        isStateChangedDuringTouch = true;

        return true;
    }

    @SuppressWarnings({ "UnusedParameters", "WeakerAccess" }) // Public API (can be overridden)
    protected void onScaleEnd(ScaleGestureDetector detector) {
        if (isScaleDetected) {
            exitController.onScaleEnd();
        }
        isScaleDetected = false;
        isRestrictZoomRequested = true;
    }

    protected boolean onRotationBegin(RotationGestureDetector detector) {
        isRotationDetected = settings.isRotationEnabled();
        if (isRotationDetected) {
            exitController.onRotationBegin();
        }
        return isRotationDetected;
    }

    @SuppressWarnings("WeakerAccess") // Public API (can be overridden)
    protected boolean onRotate(RotationGestureDetector detector) {
        if (!settings.isRotationEnabled() || isAnimatingState()) {
            return false;
        }

        boolean rotateConsumed = exitController.onRotate();
        if (rotateConsumed) {
            return true;
        }

        pivotX = detector.getFocusX();
        pivotY = detector.getFocusY();
        state.rotateBy(detector.getRotationDelta(), pivotX, pivotY);
        isStateChangedDuringTouch = true;

        return true;
    }

    @SuppressWarnings({ "UnusedParameters", "WeakerAccess" }) // Public API (can be overridden)
    protected void onRotationEnd(RotationGestureDetector detector) {
        if (isRotationDetected) {
            exitController.onRotationEnd();
        }
        isRotationDetected = false;
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

                if (Float.isNaN(pivotX) || Float.isNaN(pivotY)
                        || Float.isNaN(endPivotX) || Float.isNaN(endPivotY)) {
                    MathUtils.interpolate(state, stateStart, stateEnd, factor);
                } else {
                    MathUtils.interpolate(state, stateStart, pivotX, pivotY,
                            stateEnd, endPivotX, endPivotY, factor);
                }

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
     * State source changes listener.
     *
     * @see StateSource
     */
    @SuppressWarnings("WeakerAccess") // Public API
    public interface OnStateSourceChangeListener {
        void onStateSourceChanged(StateSource source);
    }

    /**
     * Source of state changes. Values: {@link #NONE}, {@link #USER}, {@link #ANIMATION}.
     */
    @SuppressWarnings("WeakerAccess") // Public API
    public enum StateSource {
        NONE, USER, ANIMATION
    }

    /**
     * Listener for different touch events.
     */
    @SuppressWarnings("WeakerAccess") // Public API
    public interface OnGestureListener {
        /**
         * @param event Motion event
         * @see GestureDetector.OnGestureListener#onDown(MotionEvent)
         */
        void onDown(@NonNull MotionEvent event);

        /**
         * @param event Motion event
         */
        void onUpOrCancel(@NonNull MotionEvent event);

        /**
         * @param event Motion event
         * @return true if event was consumed, false otherwise.
         * @see GestureDetector.OnGestureListener#onSingleTapUp(MotionEvent)
         */
        boolean onSingleTapUp(@NonNull MotionEvent event);

        /**
         * @param event Motion event
         * @return true if event was consumed, false otherwise.
         * @see GestureDetector.OnDoubleTapListener#onSingleTapConfirmed(MotionEvent)
         */
        boolean onSingleTapConfirmed(@NonNull MotionEvent event);

        /**
         * Note, that long press is disabled by default, use {@link View#setLongClickable(boolean)}
         * to enable it.
         *
         * @param event Motion event
         * @see GestureDetector.OnGestureListener#onLongPress(MotionEvent)
         */
        void onLongPress(@NonNull MotionEvent event);

        /**
         * @param event Motion event
         * @return true if event was consumed, false otherwise.
         * @see GestureDetector.OnDoubleTapListener#onDoubleTap(MotionEvent)
         */
        boolean onDoubleTap(@NonNull MotionEvent event);
    }

    /**
     * Simple implementation of {@link GestureController.OnGestureListener}.
     */
    @SuppressWarnings("WeakerAccess") // Public API
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
