package com.alexvasilkov.gestures.transition.internal;

import android.graphics.Rect;
import android.view.View;

import com.alexvasilkov.gestures.animation.ViewPositionAnimator.PositionUpdateListener;
import com.alexvasilkov.gestures.transition.ViewsTransitionAnimator;
import com.alexvasilkov.gestures.transition.ViewsTransitionAnimator.RequestListener;
import com.alexvasilkov.gestures.transition.tracker.FromTracker;

import androidx.annotation.NonNull;

abstract class FromBaseListener<P extends View, ID> extends RequestListener<ID> {

    private static final Rect locationParent = new Rect();
    private static final Rect locationChild = new Rect();

    private final P parentView;
    private final FromTracker<ID> tracker;
    private final boolean autoScroll;

    private boolean isFullyOpened;

    FromBaseListener(P parentView, FromTracker<ID> tracker, boolean autoScroll) {
        this.parentView = parentView;
        this.tracker = tracker;
        this.autoScroll = autoScroll;
    }

    abstract boolean isShownInList(P parentView, int pos);

    abstract void scrollToPosition(P parentView, int pos);


    @Override
    protected void initAnimator(ViewsTransitionAnimator<ID> animator) {
        super.initAnimator(animator);

        animator.addPositionUpdateListener(new PositionUpdateListener() {
            @Override
            public void onPositionUpdate(float pos, boolean isLeaving) {
                parentView.setVisibility(pos == 1f && !isLeaving ? View.INVISIBLE : View.VISIBLE);
                isFullyOpened = pos == 1f;
            }
        });
    }

    @Override
    public void onRequestView(@NonNull ID id) {
        // Trying to find requested view on screen. If it is not currently on screen
        // or it is not fully visible then we should scroll to it at first.
        int position = tracker.getPositionById(id);

        if (position == FromTracker.NO_POSITION) {
            getAnimator().setFromNone(id);
            return;
        }

        if (isShownInList(parentView, position)) {
            final View view = tracker.getViewById(id);

            if (view == null) {
                // There is no view for visible item, we have to set 'from' to no specific position
                getAnimator().setFromNone(id);
            } else {
                // View is found, we can set up 'from' view position now
                getAnimator().setFromView(id, view);

                // Scrolling list to reveal half-visible view
                if (autoScroll && isFullyOpened && !isFullyVisible(parentView, view)) {
                    scrollToPosition(parentView, position);
                }
            }
        } else {
            // There is no view, so we'll set 'from' to no specific position by default
            getAnimator().setFromNone(id);

            // Item isn't shown so let's scroll to it and see if we'll be able to find the view
            if (autoScroll) {
                scrollToPosition(parentView, position);
            }
        }
    }


    private static boolean isFullyVisible(View parent, View child) {
        parent.getGlobalVisibleRect(locationParent);
        locationParent.left += parent.getPaddingLeft();
        locationParent.right -= parent.getPaddingRight();
        locationParent.top += parent.getPaddingTop();
        locationParent.bottom -= parent.getPaddingBottom();

        child.getGlobalVisibleRect(locationChild);

        return locationParent.contains(locationChild)
                && child.getWidth() == locationChild.width()
                && child.getHeight() == locationChild.height();
    }

}
