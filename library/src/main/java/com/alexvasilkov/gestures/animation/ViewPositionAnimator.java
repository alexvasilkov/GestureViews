package com.alexvasilkov.gestures.animation;

import android.graphics.Matrix;
import android.graphics.RectF;
import android.support.annotation.FloatRange;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

import com.alexvasilkov.gestures.GestureController;
import com.alexvasilkov.gestures.GestureControllerForPager;
import com.alexvasilkov.gestures.Settings;
import com.alexvasilkov.gestures.State;
import com.alexvasilkov.gestures.StateController;
import com.alexvasilkov.gestures.internal.AnimationEngine;
import com.alexvasilkov.gestures.internal.FloatScroller;
import com.alexvasilkov.gestures.internal.GestureDebug;
import com.alexvasilkov.gestures.views.GestureImageView;
import com.alexvasilkov.gestures.views.interfaces.ClipView;
import com.alexvasilkov.gestures.views.interfaces.GestureView;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper class to animate views from one position on screen to another.
 * <p/>
 * Animation can be performed from any view (e.g. {@link ImageView}) to any gestures controlled
 * view implementing {@link GestureView} (e.g. {@link GestureImageView}).
 * <p/>
 * Note, that initial and final views should have same aspect ratio for correct animation.
 * In case of {@link ImageView} initial and final images should have same aspect, but actual views
 * can have different aspects (e.g. animating from square thumb view with scale type
 * {@link ScaleType#CENTER_CROP} to rectangular full image view).
 * <p/>
 * To use this class first create an instance and than call {@link #enter(View, boolean)}.<br/>
 * Alternatively you can manually pass initial view position using
 * {@link #enter(ViewPosition, boolean)} method. <br/>
 * To exit back to initial view call {@link #exit(boolean)} method.<br/>
 * You can listen for position changes using
 * {@link #addPositionUpdateListener(PositionUpdateListener)}.<br/>
 * If initial view was changed you should call {@link #update(View)} method to update to new view.
 * You can also manually update initial view position using {@link #update(ViewPosition)} method.
 */
public class ViewPositionAnimator {

    private static final String TAG = "ViewPositionAnimator";

    private static final Matrix tmpMatrix = new Matrix();
    private static final float[] tmpPoint = new float[2];

    private final List<PositionUpdateListener> listeners = new ArrayList<>();
    private final List<PositionUpdateListener> listenersToRemove = new ArrayList<>();
    private boolean iteratingListeners;

    private long duration = FloatScroller.DEFAULT_DURATION;

    private final FloatScroller dtateScroller = new FloatScroller();
    private final AnimationEngine animationEngine;

    private final GestureController toController;
    private final ClipView toClipView;

    private final State fromState = new State();
    private final State toState = new State();
    private float fromPivotX;
    private float fromPivotY;
    private float toPivotX;
    private float toPivotY;
    private final RectF fromClip = new RectF();
    private final RectF toClip = new RectF();
    private final RectF clipRect = new RectF();
    private ViewPosition fromPos;
    private ViewPosition toPos;

    private View fromView;

    private boolean origRestrictBoundsFlag;

    private boolean isActivated = false;

    private float positionState = 0f;
    private boolean isLeaving = true; // Leaving by default
    private boolean isAnimating = false;
    private boolean isApplyingPositionState;
    private boolean isApplyingPositionStateScheduled;

    // Marks that update for 'From' or 'To' is needed
    private boolean isFromUpdated;
    private boolean isToUpdated;

    private final ViewPositionHolder fromPosHolder = new ViewPositionHolder();
    private final ViewPositionHolder toPosHolder = new ViewPositionHolder();

    private final ViewPositionHolder.OnViewPositionChangeListener fromPositionListener =
            new ViewPositionHolder.OnViewPositionChangeListener() {
                @Override
                public void onViewPositionChanged(@NonNull ViewPosition position) {
                    if (GestureDebug.isDebugAnimator()) {
                        Log.d(TAG, "'From' view position updated: " + position.pack());
                    }

                    fromPos = position;
                    requestUpdateFromState();
                    applyPositionState();
                }
            };


    public ViewPositionAnimator(@NonNull GestureView to) {
        if (!(to instanceof View)) {
            throw new IllegalArgumentException("Argument 'to' should be an instance of View");
        }

        View toView = (View) to;
        toClipView = to instanceof ClipView ? (ClipView) to : null;
        animationEngine = new LocalAnimationEngine(toView);

        toController = to.getController();
        toController.addOnStateChangeListener(new GestureController.OnStateChangeListener() {
            @Override
            public void onStateChanged(State state) {
                // No-op
            }

            @Override
            public void onStateReset(State oldState, State newState) {
                if (!isActivated) {
                    return;
                }

                if (GestureDebug.isDebugAnimator()) {
                    Log.d(TAG, "State reset in listener: " + newState);
                }

                resetToState();
                applyPositionState();
            }
        });

        toPosHolder.init(toView, new ViewPositionHolder.OnViewPositionChangeListener() {
            @Override
            public void onViewPositionChanged(@NonNull ViewPosition position) {
                if (GestureDebug.isDebugAnimator()) {
                    Log.d(TAG, "'To' view position updated: " + position.pack());
                }

                toPos = position;
                requestUpdateToState();
                requestUpdateFromState(); // Depends on 'to' position
                applyPositionState();
            }
        });
    }

    /**
     * Starts 'enter' animation from {@code from} view to {@code to}.
     * <p/>
     * Note, if {@code from} view was changed (i.e. during list adapter refresh) you should
     * update to new view using {@link #update(View)} method.
     */
    public void enter(@NonNull View from, boolean withAnimation) {
        if (GestureDebug.isDebugAnimator()) {
            Log.d(TAG, "Entering from view, with animation = " + withAnimation);
        }

        enterInternal(withAnimation);
        updateInternal(from);
    }

    /**
     * Starts 'enter' animation from {@code from} position to {@code to} view.
     * <p/>
     * Note, if {@code from} view position was changed (i.e. during list adapter refresh) you
     * should
     * update to new view using {@link #update(ViewPosition)} method.
     */
    public void enter(@NonNull ViewPosition fromPos, boolean withAnimation) {
        if (GestureDebug.isDebugAnimator()) {
            Log.d(TAG, "Entering from view position, with animation = " + withAnimation);
        }

        enterInternal(withAnimation);
        updateInternal(fromPos);
    }

    /**
     * Updates initial view in case it was changed. You should not call this method if view stays
     * the same since animator should automatically detect view position changes.
     */
    public void update(@NonNull View from) {
        if (fromView == null) {
            throw new IllegalStateException("Animation was not started using "
                    + "enter(View, boolean) method, cannot update 'from' view");
        }

        if (GestureDebug.isDebugAnimator()) {
            Log.d(TAG, "Updating view");
        }

        updateInternal(from);
    }

    /**
     * Updates position of initial view in case it was changed.
     */
    public void update(@NonNull ViewPosition fromPos) {
        if (this.fromPos == null) {
            throw new IllegalStateException("Animation was not started using "
                    + "enter(ViewPosition, boolean) method, cannot update 'from' position");
        }

        if (GestureDebug.isDebugAnimator()) {
            Log.d(TAG, "Updating view position: " + fromPos.pack());
        }

        updateInternal(fromPos);
    }

    /**
     * Starts 'exit' animation from {@code to} view back to {@code from}.
     */
    public void exit(boolean withAnimation) {
        if (GestureDebug.isDebugAnimator()) {
            Log.d(TAG, "Exiting, with animation = " + withAnimation);
        }

        if (!isActivated) {
            throw new IllegalStateException("You should call enter(...) before calling exit(...)");
        }

        if (!isAnimating) {
            resetToState(); // Only resetting if not animating
        }

        // Starting animation from current position or applying initial state without animation
        setState(withAnimation ? positionState : 0f, true, withAnimation);
    }

    private void enterInternal(boolean withAnimation) {
        isActivated = true;

        // Starting animation from initial position or applying final state without animation
        setState(withAnimation ? 0f : 1f, false, withAnimation);
    }

    private void updateInternal(@NonNull View from) {
        if (!isActivated) {
            throw new IllegalStateException(
                    "You should call enter(...) before calling update(...)");
        }

        cleanup();
        resetToState();

        fromView = from;
        fromPosHolder.init(from, fromPositionListener);
        from.setVisibility(View.INVISIBLE); // We don't want to have duplicate view during animation
    }

    private void updateInternal(@NonNull ViewPosition fromPos) {
        if (!isActivated) {
            throw new IllegalStateException(
                    "You should call enter(...) before calling update(...)");
        }

        cleanup();
        resetToState();

        this.fromPos = fromPos;
    }

    private void cleanup() {
        if (GestureDebug.isDebugAnimator()) {
            Log.d(TAG, "Cleaning up");
        }

        if (fromView != null) {
            fromView.setVisibility(View.VISIBLE); // Switching back to visible
        }
        if (toClipView != null) {
            toClipView.clipView(null, 0f);
        }

        fromPosHolder.clear();
        fromView = null;
        fromPos = null;
        isFromUpdated = isToUpdated = false;
    }

    /**
     * Adds listener to the set of position updates listeners that will be notified during
     * any position changes.
     */
    public void addPositionUpdateListener(PositionUpdateListener listener) {
        listeners.add(listener);
        listenersToRemove.remove(listener);
    }

    /**
     * Removes listener added by {@link #addPositionUpdateListener(PositionUpdateListener)}.
     * <p/>
     * Note, this method may be called inside listener's callback without throwing
     * {@link IndexOutOfBoundsException}.
     */
    public void removePositionUpdateListener(PositionUpdateListener listener) {
        if (iteratingListeners) {
            listenersToRemove.add(listener);
        } else {
            listeners.remove(listener);
        }
    }

    private void ensurePositionUpdateListenersRemoved() {
        listeners.removeAll(listenersToRemove);
        listenersToRemove.clear();
    }

    @SuppressWarnings("unused") // Public API
    public long getDuration() {
        return duration;
    }

    @SuppressWarnings("unused") // Public API
    public void setDuration(long duration) {
        this.duration = duration;
    }

    /**
     * @return Current position state within range {@code [0, 1]}, where {@code 0} is for
     * initial (from) position and {@code 1} is for final (to) position.
     */
    public float getPositionState() {
        return positionState;
    }

    /**
     * @return Whether animator is in leaving state. Means that animation direction is
     * from final (to) position back to initial (from) position.
     */
    public boolean isLeaving() {
        return isLeaving;
    }

    /**
     * Stops current animation and sets position state to particular values.
     * <p/>
     * Note, that once animator reaches {@code state = 0f} and {@code isLeaving = true}
     * it will cleanup all internal stuff. So you will need to call {@link #enter(View, boolean)}
     * or {@link #enter(ViewPosition, boolean)} again in order to continue using animator.
     */
    public void setState(@FloatRange(from = 0f, to = 1f) float state,
            boolean isLeaving, boolean isAnimating) {
        stopAnimation();
        positionState = state;
        this.isLeaving = isLeaving;
        if (isAnimating) {
            startAnimationInternal();
        }
        applyPositionState();
    }

    /**
     * Whether view position animation is in progress or not.
     */
    public boolean isAnimating() {
        return isAnimating;
    }

    /**
     * Starts animation from current position state ({@link #getPositionState()}) and in current
     * direction ({@link #isLeaving()}).
     */
    private void startAnimationInternal() {
        stopAnimation();

        float durationFraction = isLeaving ? positionState : 1f - positionState;

        dtateScroller.startScroll(positionState, isLeaving ? 0f : 1f);
        dtateScroller.setDuration((long) (duration * durationFraction));
        animationEngine.start();
        onAnimationStarted();
    }

    /**
     * Stops current animation, if any.
     */
    public void stopAnimation() {
        dtateScroller.forceFinished();
        onAnimationStopped();
    }

    private void applyPositionState() {
        if (isApplyingPositionState) {
            // Excluding possible nested calls, scheduling sequential call instead
            isApplyingPositionStateScheduled = true;
            return;
        }
        isApplyingPositionState = true;

        // We do not need to update while 'to' view is fully visible or fully closed
        boolean paused = isLeaving ? positionState == 0f : positionState == 1f;
        fromPosHolder.pause(paused);
        toPosHolder.pause(paused);

        // Perform state updates if needed
        if (!isToUpdated) {
            updateToState();
        }
        if (!isFromUpdated) {
            updateFromState();
        }

        if (GestureDebug.isDebugAnimator()) {
            Log.d(TAG, "Applying state: " + positionState + " / " + isLeaving
                    + ", 'to' ready = " + isToUpdated + ", 'from' ready = " + isFromUpdated);
        }

        if (isToUpdated && isFromUpdated) {
            State state = toController.getState();

            StateController.interpolate(state, fromState, fromPivotX, fromPivotY,
                    toState, toPivotX, toPivotY, positionState);

            toController.updateState();

            interpolate(clipRect, fromClip, toClip, positionState);
            if (toClipView != null) {
                boolean skipClip = positionState == 1f || (positionState == 0f && isLeaving);
                toClipView.clipView(skipClip ? null : clipRect, state.getRotation());
            }
        }

        iteratingListeners = true;
        for (int i = 0, size = listeners.size(); i < size; i++) {
            if (isApplyingPositionStateScheduled) {
                break; // No need to call listeners anymore
            }
            listeners.get(i).onPositionUpdate(positionState, isLeaving);
        }
        iteratingListeners = false;
        ensurePositionUpdateListenersRemoved();

        if (positionState == 0f && isLeaving) {
            cleanup();
            isActivated = false;
            toController.resetState(); // Switching to initial state
        }

        isApplyingPositionState = false;

        if (isApplyingPositionStateScheduled) {
            isApplyingPositionStateScheduled = false;
            applyPositionState();
        }
    }

    private void onAnimationStarted() {
        if (isAnimating) {
            return;
        }
        isAnimating = true;

        if (GestureDebug.isDebugAnimator()) {
            Log.d(TAG, "Animation started");
        }

        // Saving bounds restrictions states
        origRestrictBoundsFlag = toController.getSettings().isRestrictBounds();
        // Disabling bounds restrictions & any gestures
        toController.getSettings().setRestrictBounds(false).disableGestures();
        // Stopping all currently playing animations
        toController.stopAllAnimations();

        // Disabling ViewPager scroll
        if (toController instanceof GestureControllerForPager) {
            ((GestureControllerForPager) toController).disableViewPager(true);
        }
    }

    private void onAnimationStopped() {
        if (!isAnimating) {
            return;
        }
        isAnimating = false;

        if (GestureDebug.isDebugAnimator()) {
            Log.d(TAG, "Animation stopped");
        }

        // Restoring original settings
        toController.getSettings().setRestrictBounds(origRestrictBoundsFlag).enableGestures();
        toController.updateState();

        // Enabling ViewPager scroll
        if (toController instanceof GestureControllerForPager) {
            ((GestureControllerForPager) toController).disableViewPager(false);
        }
    }

    private void resetToState() {
        if (GestureDebug.isDebugAnimator()) {
            Log.d(TAG, "State reset internal: " + toController.getState());
        }

        toState.set(toController.getState());
        requestUpdateToState();
        requestUpdateFromState();
    }

    private void requestUpdateToState() {
        isToUpdated = false;
    }

    private void requestUpdateFromState() {
        isFromUpdated = false;
    }

    private void updateToState() {
        if (isToUpdated) {
            return;
        }

        Settings settings = toController == null ? null : toController.getSettings();

        if (toPos == null || settings == null || !settings.hasImageSize()) {
            return;
        }

        toState.get(tmpMatrix);

        // 'To' clip is a 'To' image rect in 'To' view coordinates
        toClip.set(0, 0, settings.getImageW(), settings.getImageH());

        // Computing pivot point as center of the image after transformation
        tmpPoint[0] = toClip.centerX();
        tmpPoint[1] = toClip.centerY();
        tmpMatrix.mapPoints(tmpPoint);

        toPivotX = tmpPoint[0];
        toPivotY = tmpPoint[1];

        // Computing clip rect in 'To' view coordinates without rotation
        tmpMatrix.postRotate(-toState.getRotation(), toPivotX, toPivotY);
        tmpMatrix.mapRect(toClip);
        toClip.offset(toPos.viewport.left - toPos.view.left, toPos.viewport.top - toPos.view.top);

        isToUpdated = true;

        if (GestureDebug.isDebugAnimator()) {
            Log.d(TAG, "'To' state updated");
        }
    }

    private void updateFromState() {
        if (isFromUpdated) {
            return;
        }

        Settings settings = toController == null ? null : toController.getSettings();

        if (toPos == null || fromPos == null || settings == null || !settings.hasImageSize()) {
            return;
        }

        // 'From' pivot point is a center of image in 'To' viewport coordinates
        fromPivotX = fromPos.image.centerX() - toPos.viewport.left;
        fromPivotY = fromPos.image.centerY() - toPos.viewport.top;

        // Computing starting zoom level
        float imageWidth = settings.getImageW();
        float imageHeight = settings.getImageH();
        float zoomW = imageWidth == 0f ? 1f : fromPos.image.width() / imageWidth;
        float zoomH = imageHeight == 0f ? 1f : fromPos.image.height() / imageHeight;
        float zoom = Math.max(zoomW, zoomH);

        // Computing 'From' image in 'To' viewport coordinates.
        // If 'To' image has different aspect ratio it will be centered within the 'From' image.
        float fromX = fromPos.image.centerX() - 0.5f * imageWidth * zoom - toPos.viewport.left;
        float fromY = fromPos.image.centerY() - 0.5f * imageHeight * zoom - toPos.viewport.top;

        fromState.set(fromX, fromY, zoom, 0f);

        // 'From' clip is a 'From' view rect in 'To' view coordinates
        fromClip.set(fromPos.viewport);
        fromClip.offset(-toPos.view.left, -toPos.view.top);

        isFromUpdated = true;

        if (GestureDebug.isDebugAnimator()) {
            Log.d(TAG, "'From' state updated");
        }
    }

    /**
     * Interpolates from start rect to the end rect by given factor (from 0 to 1),
     * storing result into out rect.
     */
    private static void interpolate(RectF out, RectF start, RectF end, float factor) {
        out.left = StateController.interpolate(start.left, end.left, factor);
        out.top = StateController.interpolate(start.top, end.top, factor);
        out.right = StateController.interpolate(start.right, end.right, factor);
        out.bottom = StateController.interpolate(start.bottom, end.bottom, factor);
    }


    private class LocalAnimationEngine extends AnimationEngine {
        LocalAnimationEngine(@NonNull View view) {
            super(view);
        }

        @Override
        public boolean onStep() {
            if (!dtateScroller.isFinished()) {
                dtateScroller.computeScroll();
                positionState = dtateScroller.getCurr();
                applyPositionState();

                if (dtateScroller.isFinished()) {
                    onAnimationStopped();
                }

                return true;
            }
            return false;
        }
    }


    public interface PositionUpdateListener {
        /**
         * @param state Position state within range {@code [0, 1]}, where {@code 0} is for
         * initial (from) position and {@code 1} is for final (to) position.
         * @param isLeaving {@code false} if transitioning from initial to final position
         * (entering) or {@code true} for reverse transition.
         */
        void onPositionUpdate(float state, boolean isLeaving);
    }

}
