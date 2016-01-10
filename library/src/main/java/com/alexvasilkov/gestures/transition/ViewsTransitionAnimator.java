package com.alexvasilkov.gestures.transition;

import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;

import com.alexvasilkov.gestures.animation.ViewPosition;
import com.alexvasilkov.gestures.animation.ViewPositionAnimator;
import com.alexvasilkov.gestures.animation.ViewPositionAnimator.PositionUpdateListener;
import com.alexvasilkov.gestures.internal.GestureDebug;
import com.alexvasilkov.gestures.views.interfaces.AnimatorView;

import java.util.ArrayList;
import java.util.List;

/**
 * Extension of {@link ViewsCoordinator} that allows requesting {@link #enter(Object, boolean)} or
 * {@link #exit(boolean)} animations, keeps track of {@link PositionUpdateListener} listeners
 * and provides correct implementation of {@link #isLeaving()}.
 * <p/>
 * Usage of this class should be similar to {@link ViewPositionAnimator} class.
 */
public class ViewsTransitionAnimator<ID> extends ViewsCoordinator<ID> {

    private static final String TAG = ViewsTransitionAnimator.class.getSimpleName();

    private final List<PositionUpdateListener> mListeners = new ArrayList<>();

    private ID mEnterId;
    private boolean mIsReady;
    private boolean mEnterWithAnimation;

    private boolean mExitRequested;
    private boolean mExitWithAnimation;

    public ViewsTransitionAnimator() {
        addPositionUpdateListener(new PositionUpdateListener() {
            @Override
            public void onPositionUpdate(float state, boolean isLeaving) {
                if (state == 0f && isLeaving) {
                    clear();
                }
            }
        });
    }

    /**
     * Requests 'from' and 'to' views for given id
     * (see {@link OnRequestViewListener#onRequestView(Object)}),
     * and starts enter animation when both views are ready.
     */
    public void enter(@NonNull ID id, boolean withAnimation) {
        if (GestureDebug.isDebugAnimator()) {
            Log.d(TAG, "Enter requested for " + id + ", with animation = " + withAnimation);
        }

        clear();
        mEnterId = id;
        mEnterWithAnimation = withAnimation;
        request(id);
    }

    /**
     * Plays exit animation, should only be called after corresponding call to
     * {@link #enter(Object, boolean)}.
     *
     * @see #isLeaving()
     */
    public void exit(boolean withAnimation) {
        if (mEnterId == null) {
            throw new IllegalStateException("You should call enter(...) before calling exit(...)");
        }

        if (GestureDebug.isDebugAnimator()) {
            Log.d(TAG, "Exit requested from " + mEnterId + ", with animation = " + withAnimation);
        }

        mExitRequested = true;
        mExitWithAnimation = withAnimation;
        exitIfRequested();
    }

    private void exitIfRequested() {
        if (mExitRequested && mIsReady) {
            mExitRequested = false;

            if (GestureDebug.isDebugAnimator()) {
                Log.d(TAG, "Perform exit from " + mEnterId);
            }

            getToView().getPositionAnimator().exit(mExitWithAnimation);
        }
    }

    /**
     * @return Whether 'enter' was not requested recently or animator is in leaving state.
     * Means that animation direction is from final (to) position back to initial (from) position.
     */
    public boolean isLeaving() {
        return mEnterId == null || mExitRequested
                || (getToView() != null && getToView().getPositionAnimator().isLeaving());
    }

    @Override
    public void setFromView(@NonNull ID id, @NonNull View fromView) {
        if (mEnterId == null || !mEnterId.equals(id)) {
            return;
        }

        super.setFromView(id, fromView);

        if (mIsReady) {
            if (GestureDebug.isDebugAnimator()) {
                Log.d(TAG, "Updating 'from' view for " + mEnterId);
            }
            getToView().getPositionAnimator().update(fromView);
        }
    }

    @Override
    public void setFromPos(@NonNull ID id, @NonNull ViewPosition fromPos) {
        if (mEnterId == null || !mEnterId.equals(id)) {
            return;
        }

        super.setFromPos(id, fromPos);

        if (mIsReady) {
            if (GestureDebug.isDebugAnimator()) {
                Log.d(TAG, "Updating 'from' pos for " + mEnterId);
            }
            getToView().getPositionAnimator().update(fromPos);
        }
    }

    @Override
    public void setToView(@NonNull ID id, @NonNull AnimatorView toView) {
        if (mEnterId == null || !mEnterId.equals(id)) {
            return;
        }

        AnimatorView old = getToView();

        if (old != toView) {
            if (old != null && mIsReady) {
                // Animation is in place, we should carefully swap animators
                swapAnimator(old.getPositionAnimator(), toView.getPositionAnimator());
            } else {
                if (old != null) {
                    cleanupAnimator(old.getPositionAnimator());
                }
                initAnimator(toView.getPositionAnimator());
            }
        }

        super.setToView(id, toView);
    }

    @Override
    protected void onViewsReady(@NonNull ID id) {
        if (mEnterId == null || !mEnterId.equals(id)) {
            return;
        }

        if (!mIsReady) {
            mIsReady = true;

            if (GestureDebug.isDebugAnimator()) {
                Log.d(TAG, "Ready to enter for " + mEnterId);
            }

            if (getFromView() != null) {
                getToView().getPositionAnimator().enter(getFromView(), mEnterWithAnimation);
            } else if (getFromPos() != null) {
                getToView().getPositionAnimator().enter(getFromPos(), mEnterWithAnimation);
            }

            exitIfRequested();
        }

        super.onViewsReady(id);
    }

    /**
     * Adds listener to the set of position updates listeners that will be notified during
     * any position changes.
     *
     * @see ViewPositionAnimator#addPositionUpdateListener(PositionUpdateListener)
     */
    public void addPositionUpdateListener(PositionUpdateListener listener) {
        mListeners.add(listener);
        if (mIsReady) {
            getToView().getPositionAnimator().addPositionUpdateListener(listener);
        }
    }

    /**
     * Removes listener added by {@link #addPositionUpdateListener(PositionUpdateListener)}.
     *
     * @see ViewPositionAnimator#removePositionUpdateListener(PositionUpdateListener)
     */
    @SuppressWarnings("unused") // Public API
    public void removePositionUpdateListener(PositionUpdateListener listener) {
        mListeners.remove(listener);
        if (mIsReady) {
            getToView().getPositionAnimator().removePositionUpdateListener(listener);
        }
    }

    private void initAnimator(ViewPositionAnimator animator) {
        for (PositionUpdateListener listener : mListeners) {
            animator.addPositionUpdateListener(listener);
        }
    }

    private void cleanupAnimator(ViewPositionAnimator animator) {
        for (PositionUpdateListener listener : mListeners) {
            animator.removePositionUpdateListener(listener);
        }

        if (!animator.isLeaving() || animator.getPositionState() != 0f) {
            if (GestureDebug.isDebugAnimator()) {
                Log.d(TAG, "Exiting from cleaned animator for " + mEnterId);
            }

            animator.exit(false);
        }
    }

    /**
     * Replaces old animator with new one preserving state
     */
    private void swapAnimator(ViewPositionAnimator old, ViewPositionAnimator next) {
        float state = old.getPositionState();
        boolean isLeaving = old.isLeaving();
        boolean isAnimating = old.isAnimating();

        if (GestureDebug.isDebugAnimator()) {
            Log.d(TAG, "Swapping animator for " + mEnterId);
        }

        cleanupAnimator(old);

        if (getFromView() != null) {
            next.enter(getFromView(), false);
        } else if (getFromPos() != null) {
            next.enter(getFromPos(), false);
        }

        initAnimator(next);

        next.setState(state, isLeaving, isAnimating);
    }

    private void clear() {
        if (getToView() != null) {
            cleanupAnimator(getToView().getPositionAnimator());
        }
        mEnterId = null;
        mIsReady = false;
        mExitRequested = false;
        cleanupRequest();
    }

}
