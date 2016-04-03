package com.alexvasilkov.gestures.transition.internal;

import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;

import com.alexvasilkov.gestures.animation.ViewPositionAnimator;
import com.alexvasilkov.gestures.commons.RecyclePagerAdapter;
import com.alexvasilkov.gestures.transition.ViewsCoordinator;
import com.alexvasilkov.gestures.transition.ViewsTracker;
import com.alexvasilkov.gestures.transition.ViewsTransitionAnimator;
import com.alexvasilkov.gestures.views.interfaces.AnimatorView;

/**
 * Helper class to animate transitions into ViewPager with GestureView-driven pages.
 * <p/>
 * It works best with {@link RecyclePagerAdapter} as ViewPager adapter.
 */
public class IntoViewPagerListener<ID> implements ViewsCoordinator.OnRequestViewListener<ID> {

    private final ViewPager viewPager;
    private final ViewsTracker<ID> tracker;
    private final ViewsTransitionAnimator<ID> animator;
    private ID id;

    private boolean preventExit;

    public IntoViewPagerListener(@NonNull ViewPager viewPager,
            @NonNull ViewsTracker<ID> tracker,
            @NonNull ViewsTransitionAnimator<ID> animator) {
        this.viewPager = viewPager;
        this.tracker = tracker;
        this.animator = animator;

        viewPager.setVisibility(View.GONE); // We do not need to initialize ViewPager on startup
        viewPager.addOnPageChangeListener(new PagerListener());
        viewPager.setOnHierarchyChangeListener(new ChildStateListener());

        animator.addPositionUpdateListener(new UpdateListener());
    }

    private void applyCurrentPage() {
        if (id == null) {
            return;
        }
        if (viewPager.getAdapter() == null || viewPager.getAdapter().getCount() == 0) {
            return;
        }

        int current = viewPager.getCurrentItem();
        int position = tracker.getPositionForId(id);

        if (position == ViewsTracker.NO_POSITION || current != position) {
            return;
        }

        View view = tracker.getViewForPosition(current); // View may be null
        if (view instanceof AnimatorView) {
            animator.setToView(id, (AnimatorView) view);
        } else if (view != null) {
            throw new IllegalArgumentException("View for " + id + " should be AnimatorView");
        }
    }

    private void switchToCurrentPage() {
        if (viewPager.getAdapter() == null || viewPager.getAdapter().getCount() == 0) {
            return;
        }

        // If user scrolled to new page we should silently switch views
        ID currentId = tracker.getIdForPosition(viewPager.getCurrentItem());
        if (id != null && currentId != null && !id.equals(currentId)) {
            // Saving current state
            AnimatorView toView = animator.getToView();
            ViewPositionAnimator animator = toView == null ? null : toView.getPositionAnimator();
            boolean isLeaving = animator != null && animator.isLeaving();
            boolean isAnimating = animator != null && animator.isAnimating();

            // Switching to new page, preventing exit of previous page
            skipExit();
            this.animator.enter(currentId, false);

            // Applying saved state
            if (isLeaving) {
                this.animator.exit(isAnimating);
            }
        }
    }

    private void skipExit() {
        if (animator.getToView() == null) {
            return;
        }
        ViewPositionAnimator animator = this.animator.getToView().getPositionAnimator();
        if (animator.isLeaving() && animator.getPositionState() == 1f) {
            animator.setState(1f, false, false);
        }
    }

    @Override
    public void onRequestView(@NonNull ID id) {
        // Requesting ViewPager layout if it was in 'gone' state
        if (viewPager.getVisibility() == View.GONE) {
            viewPager.setVisibility(View.INVISIBLE);
        }

        // Trying to find view for currently shown page.
        // If it is not a selected page than we should scroll to it at first.
        this.id = id;
        int position = tracker.getPositionForId(id);

        if (position == ViewsTracker.NO_POSITION) {
            return; // Nothing we can do
        }

        if (viewPager.getCurrentItem() == position) {
            applyCurrentPage();
        } else {
            viewPager.setCurrentItem(position, false);
        }
    }


    private class PagerListener implements ViewPager.OnPageChangeListener {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            // No-op
        }

        @Override
        public void onPageSelected(int position) {
            applyCurrentPage();
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            preventExit = !animator.isLeaving() && state == ViewPager.SCROLL_STATE_DRAGGING;
            if (state == ViewPager.SCROLL_STATE_IDLE && id != null) {
                switchToCurrentPage();
            }
        }
    }

    private class ChildStateListener implements ViewGroup.OnHierarchyChangeListener {
        @Override
        public void onChildViewAdded(View parent, View child) {
            applyCurrentPage();
        }

        @Override
        public void onChildViewRemoved(View parent, View child) {
            // No-op
        }
    }

    private class UpdateListener implements ViewPositionAnimator.PositionUpdateListener {
        @Override
        public void onPositionUpdate(float state, boolean isLeaving) {
            if (state == 0f && isLeaving) {
                id = null;
            }
            if (state == 1f && isLeaving && id != null) {
                if (preventExit) {
                    skipExit();
                }
                switchToCurrentPage();
            }
            viewPager.setVisibility(state == 0f && isLeaving ? View.INVISIBLE : View.VISIBLE);
        }
    }

}
