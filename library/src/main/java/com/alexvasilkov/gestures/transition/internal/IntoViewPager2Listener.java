package com.alexvasilkov.gestures.transition.internal;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.viewpager2.widget.ViewPager2;

import com.alexvasilkov.gestures.animation.ViewPositionAnimator;
import com.alexvasilkov.gestures.animation.ViewPositionAnimator.PositionUpdateListener;
import com.alexvasilkov.gestures.transition.ViewsTransitionAnimator;
import com.alexvasilkov.gestures.transition.tracker.IntoTracker;
import com.alexvasilkov.gestures.views.interfaces.AnimatorView;

/**
 * Helper class to animate transitions into ViewPager2 with GestureView-driven pages.
 */
public class IntoViewPager2Listener<ID> extends ViewsTransitionAnimator.RequestListener<ID> {

    private final ViewPager2 viewPager;
    private final IntoTracker<ID> tracker;

    private boolean preventExit;

    public IntoViewPager2Listener(ViewPager2 viewPager, IntoTracker<ID> tracker) {
        this.viewPager = viewPager;
        this.tracker = tracker;

        viewPager.setVisibility(View.GONE); // We do not need to initialize ViewPager on startup
        viewPager.registerOnPageChangeCallback(new PagerListener());
        viewPager.addOnLayoutChangeListener(new LayoutStateListener());
    }

    @Override
    protected void initAnimator(ViewsTransitionAnimator<ID> animator) {
        super.initAnimator(animator);

        animator.addPositionUpdateListener(new PositionUpdateListener() {
            @Override
            public void onPositionUpdate(float pos, boolean isLeaving) {
                if (pos == 1f && isLeaving && getAnimator().getRequestedId() != null) {
                    if (preventExit) {
                        skipExit();
                    }
                    switchToCurrentPage();
                }

                viewPager.setVisibility(pos == 0f && isLeaving ? View.INVISIBLE : View.VISIBLE);
            }
        });
    }

    @Override
    public void onRequestView(@NonNull ID id) {
        // Requesting ViewPager layout if it was in 'gone' state
        if (viewPager.getVisibility() == View.GONE) {
            viewPager.setVisibility(View.INVISIBLE);
        }

        // Trying to find view for currently shown page.
        // If it is not a selected page then we should scroll to it at first.
        int position = tracker.getPositionById(id);

        if (position == IntoTracker.NO_POSITION) {
            return; // Nothing we can do
        }

        if (viewPager.getCurrentItem() == position) {
            applyCurrentPage();
        } else {
            viewPager.setCurrentItem(position, false);
        }
    }

    private void applyCurrentPage() {
        final ID id = getAnimator().getRequestedId();
        if (id == null) {
            return;
        }
        if (viewPager.getAdapter() == null || viewPager.getAdapter().getItemCount() == 0) {
            return;
        }

        final int position = tracker.getPositionById(id);

        if (position == IntoTracker.NO_POSITION) {
            switchToCurrentPage();
            return;
        }

        if (position != viewPager.getCurrentItem()) {
            return;
        }

        final View view = tracker.getViewById(id); // View may be null
        if (view instanceof AnimatorView) {
            getAnimator().setToView(id, (AnimatorView) view);
        } else if (view != null) {
            throw new IllegalArgumentException("View for " + id + " should be AnimatorView");
        }
    }

    private void switchToCurrentPage() {
        if (viewPager.getAdapter() == null || viewPager.getAdapter().getItemCount() == 0) {
            return;
        }

        final ID id = getAnimator().getRequestedId();
        final ID currentId = tracker.getIdByPosition(viewPager.getCurrentItem());

        // If user scrolled to new page we should silently switch views
        if (id != null && currentId != null && !id.equals(currentId)) {
            // Saving current state
            AnimatorView toView = getAnimator().getToView();
            ViewPositionAnimator toAnimator = toView == null ? null : toView.getPositionAnimator();
            boolean isLeaving = toAnimator != null && toAnimator.isLeaving();
            float position = toAnimator == null ? 0f : toAnimator.getPosition();
            boolean isAnimating = toAnimator != null && toAnimator.isAnimating();

            // Switching to new page, preventing exit of previous page
            skipExit();
            getAnimator().enter(currentId, false);

            // If exit animation was in place we should continue it
            if (isLeaving && position > 0f) {
                getAnimator().exit(isAnimating);
            }
        }
    }

    private void skipExit() {
        if (getAnimator().getToView() != null) {
            ViewPositionAnimator toAnimator = getAnimator().getToView().getPositionAnimator();
            if (toAnimator.isLeaving() && toAnimator.getPosition() == 1f) {
                toAnimator.setState(1f, false, false);
            }
        }
    }


    private class PagerListener extends ViewPager2.OnPageChangeCallback {
        @Override
        public void onPageSelected(int position) {
            applyCurrentPage();
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            preventExit = state == ViewPager2.SCROLL_STATE_DRAGGING && !getAnimator().isLeaving();

            if (state == ViewPager2.SCROLL_STATE_IDLE && getAnimator().getRequestedId() != null) {
                switchToCurrentPage();
            }
        }
    }

    private class LayoutStateListener implements View.OnLayoutChangeListener {
        @Override
        public void onLayoutChange(
                View view, int left, int top, int right, int bottom,
                int oldLeft, int oldTop, int oldRight, int oldBottom) {
            applyCurrentPage();
        }
    }

}
