package com.alexvasilkov.gestures.animation;

import android.os.Handler;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Extension of {@link ViewsSyncHelper} that allows to request {@link #enter(int, boolean)} or
 * {@link #exit(boolean)} animations and keeps track of {@link ViewPositionAnimator.PositionUpdateListener}
 * listeners and provides correct implementation of {@link #isLeaving()}.
 * <p/>
 * Usage of this class should be similar to {@link ViewPositionAnimator} class.
 */
public class ViewsListPositionAnimator extends ViewsSyncHelper {

    private final Handler handler = new Handler();
    private final List<ViewPositionAnimator.PositionUpdateListener> listeners = new ArrayList<>();

    private int enterIndex = NO_INDEX;
    private boolean enterWithAnimation;

    private boolean exitRequested;
    private boolean exitWithAnimation;

    private ViewPositionAnimator currentAnimator;

    private Runnable clearAction = new Runnable() {
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
                    handler.postDelayed(clearAction, 1L);
                }
            }
        });
    }

    /**
     * Requests 'from' and 'to' views for given index (see {@link RequestsListener#requestFromView(int)},
     * {@link RequestsListener#requestToView(int)}) and starts enter animation when both views are ready.
     */
    public void enter(int index, boolean withAnimation) {
        enterIndex = index;
        enterWithAnimation = withAnimation;

        if (currentAnimator != null) cleanupAnimator(currentAnimator);
        currentAnimator = null;

        request(index);
    }

    /**
     * Plays exit animation, should only be called after corresponding call to {@link #enter(int, boolean)}.
     */
    public void exit(boolean withAnimation) {
        exitRequested = true;
        exitWithAnimation = withAnimation;

        exitIfRequested();
    }

    private void exitIfRequested() {
        if (exitRequested && currentAnimator != null) {
            exitRequested = false;
            currentAnimator.exit(exitWithAnimation);
        }
    }

    public boolean isLeaving() {
        return enterIndex == NO_INDEX || exitRequested
                || (currentAnimator != null && currentAnimator.isLeaving());
    }

    @Override
    protected void onReady(int index) {
        if (index == enterIndex && getToView() != null) {
            if (currentAnimator != getToView().getPositionAnimator()) {

                if (currentAnimator == null) {
                    currentAnimator = getToView().getPositionAnimator();
                    initAnimator(currentAnimator);

                    if (getFromView() != null) {
                        currentAnimator.enter(getFromView(), enterWithAnimation);
                    } else if (getFromPos() != null) {
                        currentAnimator.enter(getFromPos(), enterWithAnimation);
                    }
                } else {
                    swipeAnimator();
                }

                exitIfRequested();
            } else {
                if (getFromView() != null) {
                    currentAnimator.update(getFromView());
                } else if (getFromPos() != null) {
                    currentAnimator.update(getFromPos());
                }
            }
        }

        super.onReady(index);
    }

    /**
     * Replaces old animator with new one preserving state
     */
    private void swipeAnimator() {
        if (currentAnimator == null) return; // Nothing to swipe

        float state = currentAnimator.getPositionState();
        boolean isLeaving = currentAnimator.isLeaving();
        boolean isAnimating = currentAnimator.isAnimating();

        cleanupAnimator(currentAnimator);
        currentAnimator = getToView().getPositionAnimator();
        initAnimator(currentAnimator);

        if (getFromView() != null) {
            currentAnimator.enter(getFromView(), false);
        } else if (getFromPos() != null) {
            currentAnimator.enter(getFromPos(), false);
        }

        currentAnimator.setState(state, isLeaving, isAnimating);
    }

    /**
     * Adds listener to the set of position updates listeners that will be notified during
     * any position changes.
     *
     * @see ViewPositionAnimator#addPositionUpdateListener(ViewPositionAnimator.PositionUpdateListener)
     */
    public void addPositionUpdateListener(ViewPositionAnimator.PositionUpdateListener listener) {
        listeners.add(listener);
        if (currentAnimator != null) currentAnimator.addPositionUpdateListener(listener);
    }

    /**
     * Removes listener added by {@link #addPositionUpdateListener(ViewPositionAnimator.PositionUpdateListener)}.
     *
     * @see ViewPositionAnimator#removePositionUpdateListener(ViewPositionAnimator.PositionUpdateListener)
     */
    public void removePositionUpdateListener(ViewPositionAnimator.PositionUpdateListener listener) {
        listeners.remove(listener);
        if (currentAnimator != null) currentAnimator.removePositionUpdateListener(listener);
    }

    private void initAnimator(ViewPositionAnimator animator) {
        for (ViewPositionAnimator.PositionUpdateListener listener : listeners) {
            animator.addPositionUpdateListener(listener);
        }
    }

    private void cleanupAnimator(ViewPositionAnimator animator) {
        for (ViewPositionAnimator.PositionUpdateListener listener : listeners) {
            animator.removePositionUpdateListener(listener);
        }
        animator.cleanup();
    }

    private void clear() {
        enterIndex = NO_INDEX;
        if (currentAnimator != null) cleanupAnimator(currentAnimator);
        currentAnimator = null;
        cleanupRequest();
        cancelRequests();
    }

}
