package com.alexvasilkov.gestures.animation;

import android.graphics.Matrix;
import android.graphics.RectF;
import android.support.annotation.FloatRange;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

import com.alexvasilkov.gestures.GestureController;
import com.alexvasilkov.gestures.Settings;
import com.alexvasilkov.gestures.State;
import com.alexvasilkov.gestures.StateController;
import com.alexvasilkov.gestures.internal.AnimationEngine;
import com.alexvasilkov.gestures.internal.FloatScroller;
import com.alexvasilkov.gestures.views.GestureImageView;
import com.alexvasilkov.gestures.views.interfaces.ClipView;
import com.alexvasilkov.gestures.views.interfaces.GestureView;

import java.util.ArrayList;

/**
 * Helper class to animate views from one position on screen to another.
 * <p/>
 * Animation can be performed from any view (e.g. {@link ImageView}) to any gestures controlled view
 * implementing {@link GestureView} (e.g. {@link GestureImageView}).
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
 * You can listen for position changes using {@link #addPositionUpdateListener(PositionUpdateListener)}.<br/>
 * If initial view was changed you should call {@link #update(View)} method to update to new view.
 * You can also manually update initial view position using {@link #update(ViewPosition)} method.
 */
public class ViewPositionAnimator {

    private static final Matrix TMP_MATRIX = new Matrix();

    private ArrayList<PositionUpdateListener> mListeners;

    private long mDuration = FloatScroller.DEFAULT_DURATION;

    private final FloatScroller mStateScroller = new FloatScroller();
    private final AnimationEngine mAnimationEngine;

    private final GestureController mToController;
    private final ClipView mToClipView;

    private final State mFromState = new State(), mToState = new State();
    private final RectF mFromClip = new RectF(), mToClip = new RectF();
    private final RectF mClipRect = new RectF();
    private ViewPosition mFromPos, mToPos;

    private View mFromView;

    private boolean mOrigRestrictBoundsFlag;

    private boolean mIsAnimating;
    private float mPositionState = 0f;
    private boolean mIsLeaving = true; // Leaving by default

    private boolean mIsFromUpdated, mIsToUpdated; // Marks that update for 'From' or 'To' is needed

    private ViewPositionHolder mFromPosHolder = new ViewPositionHolder();
    @SuppressWarnings("FieldCanBeLocal") // We need to cache it to prevent GC
    private ViewPositionHolder mToPosHolder = new ViewPositionHolder();

    private final ViewPositionHolder.OnViewPositionChangeListener mFromPositionListener =
            new ViewPositionHolder.OnViewPositionChangeListener() {
                @Override
                public void onViewPositionChanged(@NonNull ViewPosition position) {
                    mFromPos = position;
                    requestUpdateFromState();
                }
            };


    public ViewPositionAnimator(@NonNull GestureView to) {
        if (!(to instanceof View))
            throw new IllegalArgumentException("Argument 'to' should be an instance of View");

        View toView = (View) to;
        mToClipView = to instanceof ClipView ? (ClipView) to : null;
        mAnimationEngine = new LocalAnimationEngine(toView);

        mToPosHolder.init(toView, new ViewPositionHolder.OnViewPositionChangeListener() {
            @Override
            public void onViewPositionChanged(@NonNull ViewPosition position) {
                mToPos = position;
                requestUpdateToState();
                requestUpdateFromState(); // Depends on 'to' position
            }
        });

        mToController = to.getController();
        mToController.addOnStateChangeListener(new GestureController.OnStateChangeListener() {
            @Override
            public void onStateChanged(State state) {
                // No-op
            }

            @Override
            public void onStateReset(State oldState, State newState) {
                resetToState();
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
        updateInternal(from);
        enterInternal(withAnimation);
    }

    /**
     * Starts 'enter' animation from {@code from} position to {@code to} view.
     * <p/>
     * Note, if {@code from} view position was changed (i.e. during list adapter refresh) you should
     * update to new view using {@link #update(ViewPosition)} method.
     */
    public void enter(@NonNull ViewPosition fromPos, boolean withAnimation) {
        updateInternal(fromPos);
        enterInternal(withAnimation);
    }

    /**
     * Updates initial view in case it was changed. You should not call this method if view stays
     * the same since animator should automatically detect view position changes.
     */
    public void update(@NonNull View from) {
        if (mFromView == null)
            throw new IllegalStateException("Animation was not started using " +
                    "enter(View, boolean) method, cannot update 'from' view");

        updateInternal(from);
    }

    /**
     * Updates position of initial view in case it was changed.
     */
    public void update(@NonNull ViewPosition fromPos) {
        if (mFromView != null || mFromPos == null)
            throw new IllegalStateException("Animation was not started using " +
                    "enter(ViewPosition, boolean) method, cannot update 'from' position");

        updateInternal(fromPos);
    }

    /**
     * Starts 'exit' animation from {@code to} view back to {@code from}
     */
    public void exit(boolean withAnimation) {
        if (!mIsAnimating) resetToState(); // Only resetting if not animating

        if (withAnimation) {
            setPositionState(mPositionState, true); // Starting from current position
            startAnimation();
        } else {
            setPositionState(0f, true); // Applying initial state without animation
        }
    }

    private void enterInternal(boolean withAnimation) {
        resetToState();

        if (withAnimation) {
            setPositionState(0f, false);
            startAnimation();
        } else {
            setPositionState(1f, false); // Applying final state without animation
        }
    }

    private void updateInternal(@NonNull View from) {
        cleanup();
        mFromView = from;
        mFromPosHolder.init(from, mFromPositionListener);
        from.setVisibility(View.INVISIBLE); // We don't want to have duplicate view during animation
    }

    private void updateInternal(@NonNull ViewPosition fromPos) {
        cleanup();
        mFromPos = fromPos;
    }

    private void cleanup() {
        if (mFromView != null) mFromView.setVisibility(View.VISIBLE); // Switching back to visible

        mFromPosHolder.destroy();
        mFromView = null;
        mFromPos = null;
        mIsFromUpdated = false;
    }

    public void addPositionUpdateListener(PositionUpdateListener listener) {
        if (mListeners == null) mListeners = new ArrayList<>();
        mListeners.add(listener);
    }

    public void removePositionUpdateListener(PositionUpdateListener listener) {
        if (mListeners == null) return;
        mListeners.remove(listener);
    }

    public long getDuration() {
        return mDuration;
    }

    public void setDuration(long duration) {
        mDuration = duration;
    }

    /**
     * @return Current position state within range {@code [0, 1]}, where {@code 0} is for
     * initial (from) position and {@code 1} is for final (to) position.
     */
    public float getPositionState() {
        return mPositionState;
    }

    /**
     * @return Whether animator is in leaving state. Means that animation direction is
     * from final (to) position back to initial (from) position.
     */
    public boolean isLeaving() {
        return mIsLeaving;
    }

    /**
     * Stops current animation and sets position state to particular values.
     * <p/>
     * Note, that once animator reaches {@code state = 0f} and {@code isLeaving = true}
     * it will cleanup all internal stuff. So you will need to call {@link #enter(View, boolean)}
     * or {@link #enter(ViewPosition, boolean)} again in order to continue using animator.
     */
    public void setPositionState(@FloatRange(from = 0f, to = 1f) float state, boolean isLeaving) {
        stopAnimation();
        mPositionState = state;
        mIsLeaving = isLeaving;
        applyPositionState();
    }

    /**
     * Whether view position animation is in progress or not.
     */
    public boolean isAnimating() {
        return mIsAnimating;
    }

    /**
     * Starts animation from current position state ({@link #getPositionState()}) and in current
     * direction ({@link #isLeaving()}).
     */
    public void startAnimation() {
        stopAnimation();

        float durationFraction = mIsLeaving ? mPositionState : 1f - mPositionState;

        mStateScroller.startScroll(mPositionState, mIsLeaving ? 0f : 1f);
        mStateScroller.setDuration((long) (mDuration * durationFraction));
        mAnimationEngine.start();
        onAnimationStarted();
    }

    /**
     * Stops current animation, if any.
     */
    public void stopAnimation() {
        mStateScroller.forceFinished();
        onAnimationStopped();
    }

    private void applyPositionState() {
        if (!mIsToUpdated) updateToState();
        if (!mIsFromUpdated) updateFromState();

        if (mIsToUpdated && mIsFromUpdated) {
            State state = mToController.getState();
            StateController.interpolate(state, mFromState, mToState, mPositionState);
            mToController.updateState();

            interpolate(mClipRect, mFromClip, mToClip, mPositionState);
            if (mToClipView != null) mToClipView.clipView(mPositionState == 1f ? null : mClipRect);
        }

        if (mListeners != null) {
            for (int i = 0, size = mListeners.size(); i < size; i++) {
                mListeners.get(i).onPositionUpdate(mPositionState, mIsLeaving);
            }
        }

        if (mPositionState == 0f && mIsLeaving) cleanup();
    }

    private void onAnimationStarted() {
        if (mIsAnimating) return;
        mIsAnimating = true;

        // Saving bounds restrictions states
        mOrigRestrictBoundsFlag = mToController.getSettings().isRestrictBounds();
        // Disabling bounds restrictions & any gestures
        mToController.getSettings().setRestrictBounds(false).disableGestures();
        // Stopping all currently playing animations
        mToController.stopAllAnimations();
    }

    private void onAnimationStopped() {
        if (!mIsAnimating) return;
        mIsAnimating = false;

        // Restoring original settings
        mToController.getSettings().setRestrictBounds(mOrigRestrictBoundsFlag).enableGestures();
        mToController.updateState();
    }

    private void resetToState() {
        mToState.set(mToController.getState());
        requestUpdateToState();
        requestUpdateFromState();
    }

    private void requestUpdateToState() {
        mIsToUpdated = false;
    }

    private void requestUpdateFromState() {
        mIsFromUpdated = false;
    }

    private void updateToState() {
        if (mIsToUpdated) return;

        Settings settings = mToController == null ? null : mToController.getSettings();

        if (mToPos == null || settings == null || !settings.hasImageSize())
            return;

        // Computing 'To' clip by getting current 'To' image rect in 'To' view coordinates
        // (including view paddings which are not part of viewport)
        mToClip.set(0, 0, settings.getImageW(), settings.getImageH());
        mToState.get(TMP_MATRIX);
        TMP_MATRIX.mapRect(mToClip);

        int paddingLeft = mToPos.viewport.left - mToPos.view.left;
        int paddingTop = mToPos.viewport.top - mToPos.view.top;
        mToClip.offset(paddingLeft, paddingTop);

        mIsToUpdated = true;
    }

    private void updateFromState() {
        if (mIsFromUpdated) return;

        Settings settings = mToController == null ? null : mToController.getSettings();

        if (mToPos == null || mFromPos == null || settings == null || !settings.hasImageSize())
            return;

        // Computing 'From' image in 'To' viewport coordinates
        float x = mFromPos.image.left - mToPos.viewport.left;
        float y = mFromPos.image.top - mToPos.viewport.top;
        float w = mToController.getSettings().getImageW();
        float h = mToController.getSettings().getImageH();
        float zoomW = w == 0f ? 1f : mFromPos.image.width() / w;
        float zoomH = h == 0f ? 1f : mFromPos.image.height() / h;
        float zoom = Math.max(zoomW, zoomH);
        mFromState.set(x, y, zoom, 0f);

        // 'From' clip is a 'From' viewport rect in coordinates of 'To' view.
        mFromClip.set(0, 0, mFromPos.viewport.width(), mFromPos.viewport.height());
        float left = mFromPos.viewport.left - mToPos.view.left;
        float top = mFromPos.viewport.top - mToPos.view.top;
        mFromClip.offset(left, top);

        mIsFromUpdated = true;
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
        public LocalAnimationEngine(@NonNull View view) {
            super(view);
        }

        @Override
        public boolean onStep() {
            if (!mStateScroller.isFinished()) {
                mStateScroller.computeScroll();
                mPositionState = mStateScroller.getCurr();
                applyPositionState();

                if (mStateScroller.isFinished()) {
                    onAnimationStopped();
                }

                return true;
            }
            return false;
        }
    }


    public interface PositionUpdateListener {
        /**
         * @param state     Position state within range {@code [0, 1]}, where {@code 0} is for
         *                  initial (from) position and {@code 1} is for final (to) position.
         * @param isLeaving {@code false} if transitioning from initial to final position
         *                  (entering) or {@code true} for reverse transition.
         */
        void onPositionUpdate(float state, boolean isLeaving);
    }

}
