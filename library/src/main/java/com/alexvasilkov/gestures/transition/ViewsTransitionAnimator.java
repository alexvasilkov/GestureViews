package com.alexvasilkov.gestures.transition;

import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.alexvasilkov.gestures.animation.ViewPosition;
import com.alexvasilkov.gestures.animation.ViewPositionAnimator;
import com.alexvasilkov.gestures.animation.ViewPositionAnimator.PositionUpdateListener;
import com.alexvasilkov.gestures.internal.GestureDebug;
import com.alexvasilkov.gestures.transition.tracker.FromTracker;
import com.alexvasilkov.gestures.transition.tracker.IntoTracker;
import com.alexvasilkov.gestures.views.interfaces.AnimatorView;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Extension of {@link ViewsCoordinator} that allows requesting {@link #enter(Object, boolean)} or
 * {@link #exit(boolean)} animations, keeps track of {@link PositionUpdateListener} listeners
 * and provides correct implementation of {@link #isLeaving()}.
 * <p>
 * Usage of this class should be similar to {@link ViewPositionAnimator} class.
 */
public class ViewsTransitionAnimator<ID> extends ViewsCoordinator<ID> {

    private static final Object NONE = new Object();

    private static final String TAG = ViewsTransitionAnimator.class.getSimpleName();

    private final List<PositionUpdateListener> listeners = new ArrayList<>();

    private boolean enterWithAnimation;
    private boolean isEntered;

    private boolean exitRequested;
    private boolean exitWithAnimation;

    /**
     * @deprecated Use {@link GestureTransitions} instead.
     */
    @SuppressWarnings({ "WeakerAccess", "DeprecatedIsStillUsed" }) // Public temporary API
    @Deprecated
    public ViewsTransitionAnimator() {
        addPositionUpdateListener(new PositionUpdateListener() {
            @Override
            public void onPositionUpdate(float position, boolean isLeaving) {
                if (position == 0f && isLeaving) {
                    cleanupRequest();
                }
            }
        });
    }

    /**
     * Requests 'from' and 'to' views for given ID and starts enter animation when views are ready.
     *
     * @param id Item ID for views lookup
     * @param withAnimation Whether to animate entering or immediately jump to entered state
     * @see ViewsCoordinator
     */
    public void enter(@NonNull ID id, boolean withAnimation) {
        if (GestureDebug.isDebugAnimator()) {
            Log.d(TAG, "Enter requested for " + id + ", with animation = " + withAnimation);
        }

        enterWithAnimation = withAnimation;
        request(id);
    }

    /**
     * Similar to {@link #enter(Object, boolean) enter(ID, boolean)} but starts entering from no
     * specific id.<br>
     * <b>Do not use this method if you are actually going to use items ids in {@link FromTracker}
     * or {@link IntoTracker}.</b>
     * <p>
     * Can be used if your have single 'from' item with no specific id, like:<br>
     * {@code GestureTransitions.from(imageView).into(gestureImageView).enterSingle(true)}
     *
     * @param withAnimation Whether to animate entering or immediately jump to entered state
     */
    @SuppressWarnings({ "unchecked", "SameParameterValue" })
    public void enterSingle(boolean withAnimation) {
        // Passing 'NONE' Object instead of ID. Will fail if ID will be actually used.
        enter((ID) NONE, withAnimation);
    }

    /**
     * Plays exit animation, should only be called after corresponding call to
     * {@link #enter(Object, boolean)}.
     *
     * @param withAnimation Whether to animate exiting or immediately jump to initial state
     * @see #isLeaving()
     */
    public void exit(boolean withAnimation) {
        if (getRequestedId() == null) {
            throw new IllegalStateException("You should call enter(...) before calling exit(...)");
        }

        if (GestureDebug.isDebugAnimator()) {
            Log.d(TAG, "Exit requested from " + getRequestedId()
                    + ", with animation = " + withAnimation);
        }

        exitRequested = true;
        exitWithAnimation = withAnimation;
        exitIfRequested();
    }

    private void exitIfRequested() {
        if (exitRequested && isReady()) {
            exitRequested = false;

            if (GestureDebug.isDebugAnimator()) {
                Log.d(TAG, "Perform exit from " + getRequestedId());
            }

            getToView().getPositionAnimator().exit(exitWithAnimation);
        }
    }

    /**
     * @return Whether 'enter' was not requested recently or animator is in leaving state.
     * Means that animation direction is from final (to) position back to initial (from) position.
     */
    public boolean isLeaving() {
        return exitRequested || getRequestedId() == null
                || (isReady() && getToView().getPositionAnimator().isLeaving());
    }


    /**
     * Adds position state changes listener that will be notified during animations.
     *
     * @param listener Position listener
     * @see ViewPositionAnimator#addPositionUpdateListener(PositionUpdateListener)
     */
    public void addPositionUpdateListener(@NonNull PositionUpdateListener listener) {
        listeners.add(listener);
        if (isReady()) {
            getToView().getPositionAnimator().addPositionUpdateListener(listener);
        }
    }

    /**
     * Removes position state changes listener as added by addPositionUpdateListener(...).
     *
     * @param listener Position listener to be removed
     * @see ViewPositionAnimator#removePositionUpdateListener(PositionUpdateListener)
     */
    @SuppressWarnings("unused") // Public API
    public void removePositionUpdateListener(@NonNull PositionUpdateListener listener) {
        listeners.remove(listener);
        if (isReady()) {
            getToView().getPositionAnimator().removePositionUpdateListener(listener);
        }
    }


    @Override
    public void setFromListener(@NonNull OnRequestViewListener<ID> listener) {
        super.setFromListener(listener);
        if (listener instanceof RequestListener) {
            ((RequestListener<ID>) listener).initAnimator(this);
        }
    }

    @Override
    public void setToListener(@NonNull OnRequestViewListener<ID> listener) {
        super.setToListener(listener);
        if (listener instanceof RequestListener) {
            ((RequestListener<ID>) listener).initAnimator(this);
        }
    }

    @Override
    protected void onFromViewChanged(@Nullable View fromView, @Nullable ViewPosition fromPos) {
        super.onFromViewChanged(fromView, fromPos);

        if (isReady()) {
            if (GestureDebug.isDebugAnimator()) {
                Log.d(TAG, "Updating 'from' view for " + getRequestedId());
            }

            if (fromView != null) {
                getToView().getPositionAnimator().update(fromView);
            } else if (fromPos != null) {
                getToView().getPositionAnimator().update(fromPos);
            } else {
                getToView().getPositionAnimator().updateToNone();
            }
        }
    }

    @Override
    protected void onToViewChanged(@Nullable AnimatorView old, @NonNull AnimatorView view) {
        super.onToViewChanged(old, view);

        if (isReady() && old != null) {
            // Animation is in place, we should carefully swap animators
            swapAnimator(old.getPositionAnimator(), view.getPositionAnimator());
        } else {
            if (old != null) {
                cleanupAnimator(old.getPositionAnimator());
            }
            initAnimator(view.getPositionAnimator());
        }
    }

    @Override
    protected void onViewsReady(@NonNull ID id) {
        if (!isEntered) {
            isEntered = true;

            if (GestureDebug.isDebugAnimator()) {
                Log.d(TAG, "Ready to enter for " + getRequestedId());
            }

            if (getFromView() != null) {
                getToView().getPositionAnimator().enter(getFromView(), enterWithAnimation);
            } else if (getFromPos() != null) {
                getToView().getPositionAnimator().enter(getFromPos(), enterWithAnimation);
            } else {
                getToView().getPositionAnimator().enter(enterWithAnimation);
            }

            exitIfRequested();
        }

        if (getFromView() instanceof ImageView && getToView() instanceof ImageView) {
            // Pre-setting 'to' image with 'from' image to prevent flickering
            ImageView from = (ImageView) getFromView();
            ImageView to = (ImageView) getToView();
            if (to.getDrawable() == null) {
                to.setImageDrawable(from.getDrawable());
            }
        }

        super.onViewsReady(id);
    }

    @Override
    protected void cleanupRequest() {
        if (getToView() != null) {
            cleanupAnimator(getToView().getPositionAnimator());
        }

        isEntered = false;
        exitRequested = false;

        super.cleanupRequest();
    }


    private void initAnimator(ViewPositionAnimator animator) {
        for (PositionUpdateListener listener : listeners) {
            animator.addPositionUpdateListener(listener);
        }
    }

    private void cleanupAnimator(ViewPositionAnimator animator) {
        for (PositionUpdateListener listener : listeners) {
            animator.removePositionUpdateListener(listener);
        }

        if (!animator.isLeaving() || animator.getPosition() != 0f) {
            if (GestureDebug.isDebugAnimator()) {
                Log.d(TAG, "Exiting from cleaned animator for " + getRequestedId());
            }

            animator.exit(false);
        }
    }

    // Replaces old animator with new one preserving state.
    private void swapAnimator(ViewPositionAnimator old, ViewPositionAnimator next) {
        final float position = old.getPosition();
        final boolean isLeaving = old.isLeaving();
        final boolean isAnimating = old.isAnimating();

        if (GestureDebug.isDebugAnimator()) {
            Log.d(TAG, "Swapping animator for " + getRequestedId());
        }

        cleanupAnimator(old);

        if (getFromView() != null) {
            next.enter(getFromView(), false);
        } else if (getFromPos() != null) {
            next.enter(getFromPos(), false);
        }

        initAnimator(next);

        next.setState(position, isLeaving, isAnimating);
    }


    public abstract static class RequestListener<ID> implements OnRequestViewListener<ID> {
        private ViewsTransitionAnimator<ID> animator;

        protected void initAnimator(ViewsTransitionAnimator<ID> animator) {
            this.animator = animator;
        }

        protected ViewsTransitionAnimator<ID> getAnimator() {
            return animator;
        }
    }

}
