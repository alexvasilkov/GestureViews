package com.alexvasilkov.gestures.animation;

import android.graphics.Matrix;
import android.graphics.RectF;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

import com.alexvasilkov.gestures.GesturesController;
import com.alexvasilkov.gestures.Settings;
import com.alexvasilkov.gestures.State;
import com.alexvasilkov.gestures.StateController;
import com.alexvasilkov.gestures.internal.AnimationEngine;
import com.alexvasilkov.gestures.internal.FloatScroller;
import com.alexvasilkov.gestures.views.GestureImageView;
import com.alexvasilkov.gestures.views.interfaces.ClipView;
import com.alexvasilkov.gestures.views.interfaces.GestureView;

/**
 * Helper class to animate views from one position on screen to another.
 * <p/>
 * Animation can be performed from any view (e.g. {@link ImageView}) to any gestures controlled view
 * implementing {@link GestureView} (e.g. {@link GestureImageView}).
 * <p/>
 * Note, that initial and final
 * views should have same aspect ratio. In case of {@link ImageView} intial and final images should
 * have same aspect, but actual views can have different aspects (e.g. animating from square thumb
 * view with scale type = {@link ScaleType#CENTER_CROP} to rectangular full image view).
 * <p/>
 * To use this class first create an instance and than call {@link #reset(ViewPosition, GestureView)}
 * method whenever initial or final view position is changed. After that you can call
 * {@link #enter(boolean)} and {@link #exit(boolean)} methods and listen for transition changes
 * using {@link #setOnPositionChangeListener(OnPositionChangeListener)}.
 */
public class ViewPositionAnimator {

    private static final Matrix TMP_MATRIX = new Matrix();

    private OnPositionChangeListener mListener;
    private long mDuration = FloatScroller.DEFAULT_DURATION;

    private final FloatScroller mStateScroller = new FloatScroller();
    private final AnimationEngine mAnimationEngine = new LocalAnimationEngine();

    private boolean mIsAnimationStarted;
    private float mAnimationState;

    private final State mFromState = new State(), mToState = new State();
    private final RectF mFromClip = new RectF(), mToClip = new RectF();
    private final RectF mClipRect = new RectF();

    private boolean mIsFromUpdated, mIsToUpdated; // Marks that update for 'From' or 'To' are needed

    private boolean mOrigRestrictBoundsFlag;
    private boolean mIsFinishing;

    private ViewPosition mFromPos, mToPos;
    private GesturesController mToController;
    private View mToView;
    private ClipView mToClipView;


    private ViewTreeObserver.OnPreDrawListener mToPreDrawListener =
            new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    mToView.getViewTreeObserver().removeOnPreDrawListener(this);
                    mToPos = ViewPosition.from(mToView);
                    requestUpdateToState();
                    applyAnimationState();
                    return true;
                }
            };

    private GesturesController.OnStateChangeListener mStateListener =
            new GesturesController.OnStateChangeListener() {
                @Override
                public void onStateChanged(State state) {
                    if (!mIsAnimationStarted) requestUpdateToState();
                }

                @Override
                public void onStateReset(State oldState, State newState) {
                    requestUpdateToState();
                    requestUpdateFromState();
                    applyAnimationState();
                }
            };


    public void reset(ViewPosition from, GestureView to) {
        // Cleaning up
        finishAnimation();

        if (mToView != null)
            mToView.getViewTreeObserver().removeOnPreDrawListener(mToPreDrawListener);

        if (mToController != null)
            mToController.removeOnStateChangeListener(mStateListener);

        mIsFinishing = false;
        mAnimationState = 0f;

        // Setting new values
        mFromPos = from;

        mToController = to.getController();
        mToController.addOnStateChangeListener(mStateListener);

        if (!(to instanceof View))
            throw new IllegalArgumentException("Argument 'to' should be an instance of View");
        mToView = (View) to;
        mToView.getViewTreeObserver().addOnPreDrawListener(mToPreDrawListener);
        mAnimationEngine.attachToView(mToView);

        mToClipView = to instanceof ClipView ? (ClipView) to : null;

        requestUpdateFromState();
        requestUpdateToState();
        applyAnimationState();
    }

    public ViewPositionAnimator setOnPositionChangeListener(OnPositionChangeListener listener) {
        mListener = listener;
        return this;
    }

    public long getDuration() {
        return mDuration;
    }

    public ViewPositionAnimator setDuration(long duration) {
        this.mDuration = duration;
        return this;
    }

    public void enter(boolean withAnimation) {
        mIsFinishing = false;

        if (withAnimation) {
            startAnimation();
        } else {
            // Applying final state without animation
            mAnimationState = 1f;
            applyAnimationState();
        }
    }

    public void exit(boolean withAnimation) {
        mIsFinishing = true;

        if (withAnimation) {
            startAnimation();
        } else {
            // Applying initial state without animation
            mAnimationState = 0f;
            applyAnimationState();
        }
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

        mToState.set(mToController.getState());

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

    private void startAnimation() {
        finishAnimation();

        float durationFraction = mIsFinishing ? mAnimationState : 1f - mAnimationState;

        mStateScroller.startScroll(mAnimationState, mIsFinishing ? 0f : 1f);
        mStateScroller.setDuration((long) (mDuration * durationFraction));
        mAnimationEngine.start();
        onAnimationStarted();
    }

    private void finishAnimation() {
        mStateScroller.forceFinished();
        onAnimationFinished();
    }

    private void applyAnimationState() {
        if (!mIsToUpdated) updateToState();
        if (!mIsFromUpdated) updateFromState();

        if (mIsToUpdated && mIsFromUpdated) {
            State state = mToController.getState();
            StateController.interpolate(state, mFromState, mToState, mAnimationState);
            mToController.updateState();

            interpolate(mClipRect, mFromClip, mToClip, mAnimationState);
            if (mToClipView != null) mToClipView.clipView(mAnimationState == 1f ? null : mClipRect);
        }

        if (mListener != null) mListener.onPositionChanged(mAnimationState, mIsFinishing);
    }

    private void onAnimationStarted() {
        if (mIsAnimationStarted) return;
        mIsAnimationStarted = true;

        // Saving bounds restrictions states
        mOrigRestrictBoundsFlag = mToController.getSettings().isRestrictBounds();
        // Disabling bounds restrictions & any gestures
        mToController.getSettings().setRestrictBounds(false).disableGestures();
        // Stopping all currently playing animations
        mToController.stopAllAnimations();
    }

    private void onAnimationFinished() {
        if (!mIsAnimationStarted) return;
        mIsAnimationStarted = false;

        // Restoring original settings
        mToController.getSettings().setRestrictBounds(mOrigRestrictBoundsFlag).enableGestures();
        // Only updating state in entered state to prevent unwanted flickering
        if (mAnimationState == 1f) mToController.updateState();

        // Removing view clip when we entered, but preserve clip when we exited
        // to ensure there is no edges lag when switching back to initial view.
        if (!mIsFinishing && mToClipView != null) mToClipView.clipView(null);
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
        @Override
        public boolean onStep() {
            if (!mStateScroller.isFinished()) {
                mStateScroller.computeScroll();
                mAnimationState = mStateScroller.getCurr();
                applyAnimationState();

                if (mStateScroller.isFinished()) {
                    onAnimationFinished();
                }

                return true;
            }
            return false;
        }
    }


    public interface OnPositionChangeListener {
        /**
         * @param state       Transition state within range {@code [0, 1]}, where {@code 0} is for
         *                    initial (from) position and {@code 1} is for final (to) position.
         * @param isFinishing {@code false} if transitioning from initial to final position
         *                    (entering) or {@code true} for reverse transition (exiting).
         */
        void onPositionChanged(float state, boolean isFinishing);
    }

}
