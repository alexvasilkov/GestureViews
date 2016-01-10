package com.alexvasilkov.gestures.transition.internal;

import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;

import com.alexvasilkov.gestures.animation.ViewPositionAnimator;
import com.alexvasilkov.gestures.transition.ViewsCoordinator;
import com.alexvasilkov.gestures.transition.ViewsTracker;
import com.alexvasilkov.gestures.transition.ViewsTransitionAnimator;
import com.alexvasilkov.gestures.views.interfaces.AnimatorView;
import com.alexvasilkov.gestures.commons.RecyclePagerAdapter;

/**
 * Helper class to animate transitions into ViewPager with GestureView-driven pages.
 * <p/>
 * It works best with {@link RecyclePagerAdapter} as ViewPager adapter.
 */
public class IntoViewPagerListener<ID> implements ViewsCoordinator.OnRequestViewListener<ID> {

    private final ViewPager mViewPager;
    private final ViewsTracker<ID> mTracker;
    private final ViewsTransitionAnimator<ID> mAnimator;
    private ID mId;

    private boolean mPreventExit;

    public IntoViewPagerListener(@NonNull ViewPager viewPager,
            @NonNull ViewsTracker<ID> tracker,
            @NonNull ViewsTransitionAnimator<ID> animator) {
        mViewPager = viewPager;
        mTracker = tracker;
        mAnimator = animator;

        mViewPager.setVisibility(View.GONE); // We do not need to initialize ViewPager on startup
        mViewPager.addOnPageChangeListener(new PagerListener());
        mViewPager.setOnHierarchyChangeListener(new ChildStateListener());

        mAnimator.addPositionUpdateListener(new UpdateListener());
    }

    private void applyCurrentPage() {
        if (mId == null) {
            return;
        }
        if (mViewPager.getAdapter() == null || mViewPager.getAdapter().getCount() == 0) {
            return;
        }

        int current = mViewPager.getCurrentItem();
        int position = mTracker.getPositionForId(mId);

        if (position == ViewsTracker.NO_POSITION || current != position) {
            return;
        }

        View view = mTracker.getViewForPosition(current); // View may be null
        if (view instanceof AnimatorView) {
            mAnimator.setToView(mId, (AnimatorView) view);
        } else if (view != null) {
            throw new IllegalArgumentException("View for " + mId + " should be AnimatorView");
        }
    }

    private void switchToCurrentPage() {
        if (mViewPager.getAdapter() == null || mViewPager.getAdapter().getCount() == 0) {
            return;
        }

        // If user scrolled to new page we should silently switch views
        ID currentId = mTracker.getIdForPosition(mViewPager.getCurrentItem());
        if (mId != null && currentId != null && !mId.equals(currentId)) {
            // Saving current state
            AnimatorView toView = mAnimator.getToView();
            ViewPositionAnimator animator = toView == null ? null : toView.getPositionAnimator();
            boolean isLeaving = animator != null && animator.isLeaving();
            boolean isAnimating = animator != null && animator.isAnimating();

            // Switching to new page, preventing exit of previous page
            skipExit();
            mAnimator.enter(currentId, false);

            // Applying saved state
            if (isLeaving) {
                mAnimator.exit(isAnimating);
            }
        }
    }

    private void skipExit() {
        if (mAnimator.getToView() == null) {
            return;
        }
        ViewPositionAnimator animator = mAnimator.getToView().getPositionAnimator();
        if (animator.isLeaving() && animator.getPositionState() == 1f) {
            animator.setState(1f, false, false);
        }
    }

    @Override
    public void onRequestView(@NonNull ID id) {
        // Requesting ViewPager layout if it was in 'gone' state
        if (mViewPager.getVisibility() == View.GONE) {
            mViewPager.setVisibility(View.INVISIBLE);
        }

        // Trying to find view for currently shown page.
        // If it is not a selected page than we should scroll to it at first.
        mId = id;
        int position = mTracker.getPositionForId(id);

        if (position == ViewsTracker.NO_POSITION) {
            return; // Nothing we can do
        }

        if (mViewPager.getCurrentItem() == position) {
            applyCurrentPage();
        } else {
            mViewPager.setCurrentItem(position, false);
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
            mPreventExit = !mAnimator.isLeaving() && state == ViewPager.SCROLL_STATE_DRAGGING;
            if (state == ViewPager.SCROLL_STATE_IDLE && mId != null) {
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
                mId = null;
            }
            if (state == 1f && isLeaving && mId != null) {
                if (mPreventExit) {
                    skipExit();
                }
                switchToCurrentPage();
            }
            mViewPager.setVisibility(state == 0f && isLeaving ? View.INVISIBLE : View.VISIBLE);
        }
    }

}
