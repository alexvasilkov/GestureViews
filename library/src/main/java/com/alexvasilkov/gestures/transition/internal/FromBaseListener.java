package com.alexvasilkov.gestures.transition.internal;

import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.view.View;

import com.alexvasilkov.gestures.animation.ViewPositionAnimator.PositionUpdateListener;
import com.alexvasilkov.gestures.transition.ViewsTransitionAnimator;
import com.alexvasilkov.gestures.transition.ViewsTransitionAnimator.RequestListener;
import com.alexvasilkov.gestures.transition.tracker.FromTracker;

abstract class FromBaseListener<P extends View, ID> extends RequestListener<ID> {

    private static final Rect locationParent = new Rect();
    private static final Rect locationChild = new Rect();

    private final P parentView;
    private final FromTracker<ID> tracker;
    private boolean scrollHalfVisibleItems;

    FromBaseListener(P parentView, FromTracker<ID> tracker) {
        this.parentView = parentView;
        this.tracker = tracker;
    }

    abstract void scrollToPosition(P parentView, int pos);


    @Override
    protected void initAnimator(ViewsTransitionAnimator<ID> animator) {
        super.initAnimator(animator);

        animator.addPositionUpdateListener(new PositionUpdateListener() {
            @Override
            public void onPositionUpdate(float pos, boolean isLeaving) {
                parentView.setVisibility(pos == 1f && !isLeaving ? View.INVISIBLE : View.VISIBLE);
                scrollHalfVisibleItems = pos == 1f; // Only scroll if we in full mode
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

        View view = tracker.getViewById(id);
        if (view == null) {
            scrollToPosition(parentView, position);
        } else {
            getAnimator().setFromView(id, view);

            if (scrollHalfVisibleItems && !isFullyVisible(parentView, view)) {
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
