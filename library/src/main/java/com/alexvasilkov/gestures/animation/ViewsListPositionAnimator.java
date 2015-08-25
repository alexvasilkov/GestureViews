package com.alexvasilkov.gestures.animation;

import android.os.Handler;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Extension of {@link ViewsSyncHelper} that allows request {@link #enter(int, boolean)} or
 * {@link #exit(boolean)} animations and keeps track of {@link ViewPositionAnimator.PositionUpdateListener}
 * listeners and provides correct implementation of {@link #isLeaving()}.
 * <p/>
 * Usage of this class should be similar to {@link ViewPositionAnimator} class.
 */
public class ViewsListPositionAnimator extends ViewsSyncHelper {

    private final Handler mHandler = new Handler();
    private final List<ViewPositionAnimator.PositionUpdateListener> mListeners = new ArrayList<>();

    private int mEnterIndex = NO_INDEX;
    private boolean mEnterWithAnimation;

    private boolean mExitRequested;
    private boolean mExitWithAnimation;

    private ViewPositionAnimator mCurrentAnimator;

    private final Runnable mClearAction = new Runnable() {
        @Override
        public void run() {
            clear();
        }
    };

    public ViewsListPositionAnimator(@NonNull RequestsListener listener) {
        setRequestsListener(listener);

        addPositionUpdateListener(new ViewPositionAnimator.PositionUpdateListener() {
            @Override
            public void onPositionUpdate(float state, boolean isLeaving) {
                if (state == 0f && isLeaving) {
                    // We can't remove listeners while inside listener callback,
                    // so we will clear internal state a bit later.
                    mHandler.postDelayed(mClearAction, 1L);
                }
            }
        });
    }

    /**
     * Requests 'from' and 'to' views for given index (see {@link RequestsListener#requestFromView(int)},
     * {@link RequestsListener#requestToView(int)}) and starts enter animation when both views are ready.
     */
    public void enter(int index, boolean withAnimation) {
        mEnterIndex = index;
        mEnterWithAnimation = withAnimation;

        if (mCurrentAnimator != null) cleanupAnimator(mCurrentAnimator);
        mCurrentAnimator = null;

        request(index);
    }

    /**
     * Plays exit animation, should only be called after corresponding call to {@link #enter(int, boolean)}.
     */
    public void exit(boolean withAnimation) {
        mExitRequested = true;
        mExitWithAnimation = withAnimation;

        exitIfRequested();
    }

    private void exitIfRequested() {
        if (mExitRequested && mCurrentAnimator != null) {
            mExitRequested = false;
            mCurrentAnimator.exit(mExitWithAnimation);
        }
    }

    public boolean isLeaving() {
        return mEnterIndex == NO_INDEX || mExitRequested
                || (mCurrentAnimator != null && mCurrentAnimator.isLeaving());
    }

    @Override
    protected void onReady(int index) {
        if (index == mEnterIndex && getToView() != null) {
            if (mCurrentAnimator != getToView().getPositionAnimator()) {

                if (mCurrentAnimator == null) {
                    mCurrentAnimator = getToView().getPositionAnimator();
                    initAnimator(mCurrentAnimator);

                    if (getFromView() != null) {
                        mCurrentAnimator.enter(getFromView(), mEnterWithAnimation);
                    } else if (getFromPos() != null) {
                        mCurrentAnimator.enter(getFromPos(), mEnterWithAnimation);
                    }
                } else {
                    swipeAnimator();
                }

                exitIfRequested();
            } else {
                if (getFromView() != null) {
                    mCurrentAnimator.update(getFromView());
                } else if (getFromPos() != null) {
                    mCurrentAnimator.update(getFromPos());
                }
            }
        }

        super.onReady(index);
    }

    /**
     * Replaces old animator with new one preserving state
     */
    private void swipeAnimator() {
        if (mCurrentAnimator == null) return; // Nothing to swipe

        float state = mCurrentAnimator.getPositionState();
        boolean isLeaving = mCurrentAnimator.isLeaving();
        boolean isAnimating = mCurrentAnimator.isAnimating();

        cleanupAnimator(mCurrentAnimator);
        mCurrentAnimator = getToView().getPositionAnimator();
        initAnimator(mCurrentAnimator);

        if (getFromView() != null) {
            mCurrentAnimator.enter(getFromView(), false);
        } else if (getFromPos() != null) {
            mCurrentAnimator.enter(getFromPos(), false);
        }

        mCurrentAnimator.setState(state, isLeaving, isAnimating);
    }

    /**
     * Adds listener to the set of position updates listeners that will be notified during
     * any position changes.
     *
     * @see ViewPositionAnimator#addPositionUpdateListener(ViewPositionAnimator.PositionUpdateListener)
     */
    public void addPositionUpdateListener(ViewPositionAnimator.PositionUpdateListener listener) {
        mListeners.add(listener);
        if (mCurrentAnimator != null) mCurrentAnimator.addPositionUpdateListener(listener);
    }

    /**
     * Removes listener added by {@link #addPositionUpdateListener(ViewPositionAnimator.PositionUpdateListener)}.
     *
     * @see ViewPositionAnimator#removePositionUpdateListener(ViewPositionAnimator.PositionUpdateListener)
     */
    public void removePositionUpdateListener(ViewPositionAnimator.PositionUpdateListener listener) {
        mListeners.remove(listener);
        if (mCurrentAnimator != null) mCurrentAnimator.removePositionUpdateListener(listener);
    }

    private void initAnimator(ViewPositionAnimator animator) {
        for (ViewPositionAnimator.PositionUpdateListener listener : mListeners) {
            animator.addPositionUpdateListener(listener);
        }
    }

    private void cleanupAnimator(ViewPositionAnimator animator) {
        for (ViewPositionAnimator.PositionUpdateListener listener : mListeners) {
            animator.removePositionUpdateListener(listener);
        }
        animator.cleanup();
    }

    private void clear() {
        mEnterIndex = NO_INDEX;
        if (mCurrentAnimator != null) cleanupAnimator(mCurrentAnimator);
        mCurrentAnimator = null;
        cleanupRequest();
        cancelRequests();
    }

}
