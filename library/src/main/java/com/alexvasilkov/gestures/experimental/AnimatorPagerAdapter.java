package com.alexvasilkov.gestures.experimental;

import android.graphics.Rect;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.alexvasilkov.gestures.animation.ViewsListPositionAnimator;
import com.alexvasilkov.gestures.animation.ViewsSyncHelper.RequestsListener;
import com.alexvasilkov.gestures.views.interfaces.AnimatorView;

public abstract class AnimatorPagerAdapter<VH extends RecyclePagerAdapter.ViewHolder>
        extends RecyclePagerAdapter<VH> {

    private static final int NO_INDEX = -1;
    private static final Rect LOCATION_PARENT = new Rect(), LOCATION = new Rect();

    private final ViewsListPositionAnimator mListAnimator;
    private final ViewPager mViewPager;
    private final RecyclerView mRecyclerView;
    private int mFromIndex = NO_INDEX, mToIndex = NO_INDEX;
    private boolean mIsPagerEnabled;
    private boolean mScrollHalfVisibleItems;

    public AnimatorPagerAdapter(ViewPager viewPager, RecyclerView recyclerView) {
        mViewPager = viewPager;
        mRecyclerView = recyclerView;

        setPagerEnabled(false);

        mListAnimator = new ViewsListPositionAnimator(new ViewsRequestsListener()) {
            @Override
            public void exit(boolean withAnimation) {
                // If animation state is not yet applied for 'to' view but exit is requested,
                // than it's time to apply correct animation state
                applyItemFromPager();

                super.exit(withAnimation);
            }

            @Override
            public void enter(int index, boolean withAnimation) {
                // We should scroll recycler view if we entering without animation,
                // i.e. when swiping viewpager
                mScrollHalfVisibleItems = !withAnimation;
                super.enter(index, withAnimation);
            }
        };

        viewPager.setAdapter(this);
        viewPager.addOnPageChangeListener(new PagerListener());

        recyclerView.addOnChildAttachStateChangeListener(new RecyclerChildStateListener());
    }

    public ViewsListPositionAnimator getListAnimator() {
        return mListAnimator;
    }

    @Override
    public final int getCount() {
        return mIsPagerEnabled ? getItemsCount() : 0; // No pages if not in full screen mode
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Object obj = super.instantiateItem(container, position);
        notifyPagerItemChanged(position); // Updating 'to' view when data set is invalidated
        return obj;
    }

    protected abstract int getItemsCount();

    protected abstract AnimatorView getAnimatorView(VH holder);

    /**
     * Will be called when both 'from' and 'to' views are ready to be animated.
     *
     * @see RequestsListener#onViewsReady(int)
     */
    protected void onViewsReady(View from, AnimatorView to, int index) {
    }

    private void notifyPagerItemChanged(int position) {
        if (mToIndex != NO_INDEX && mToIndex == position) {
            VH holder = getViewHolder(position); // Holder sometimes maybe null
            if (holder != null) mListAnimator.setToView(position, getAnimatorView(holder));
        }
    }

    private void applyItemFromPager() {
        // If user scrolled to new page we should silently apply animation logic
        int position = AnimatorPagerAdapter.this.mViewPager.getCurrentItem();
        if (mToIndex != NO_INDEX && mToIndex != position) mListAnimator.enter(position, false);
    }

    protected void setPagerEnabled(boolean isEnabled) {
        if (mIsPagerEnabled != isEnabled) {
            mIsPagerEnabled = isEnabled;
            notifyDataSetChanged();
        }
    }

    private class ViewsRequestsListener implements RequestsListener {
        @Override
        public void requestFromView(int index) {
            // Trying to find requested view on screen. If it is not currently on screen
            // or it is not fully visible than we should scroll to it at first.
            mFromIndex = index;

            RecyclerView.ViewHolder holder = mRecyclerView.findViewHolderForAdapterPosition(index);
            if (holder == null) {
                mRecyclerView.smoothScrollToPosition(index);
            } else {
                mListAnimator.setFromView(index, holder.itemView);

                if (mScrollHalfVisibleItems) {
                    mRecyclerView.getGlobalVisibleRect(LOCATION_PARENT);
                    View v = holder.itemView;
                    v.getGlobalVisibleRect(LOCATION);
                    if (!LOCATION_PARENT.contains(LOCATION)
                            || v.getWidth() > LOCATION.width()
                            || v.getHeight() > LOCATION.height()) {
                        mRecyclerView.smoothScrollToPosition(index);
                    }
                }
            }
        }

        @Override
        public void requestToView(int index) {
            // Trying to find view for currently shown page.
            // If it is not a selected page than we should scroll to it at first.
            setPagerEnabled(true);
            mToIndex = index;

            if (mViewPager.getCurrentItem() == index) {
                notifyPagerItemChanged(index);
            } else {
                mViewPager.setCurrentItem(index, false);
            }
        }

        @Override
        public void cancelRequests() {
            mFromIndex = mToIndex = NO_INDEX;
            setPagerEnabled(false);
        }

        @Override
        public void onViewsReady(int index) {
            AnimatorPagerAdapter.this.onViewsReady(
                    mListAnimator.getFromView(), mListAnimator.getToView(), index);
        }
    }

    private class RecyclerChildStateListener implements RecyclerView.OnChildAttachStateChangeListener {
        @Override
        public void onChildViewAttachedToWindow(View view) {
            int index = mRecyclerView.getChildAdapterPosition(view);
            if (mFromIndex != NO_INDEX && index == mFromIndex) {
                mListAnimator.setFromView(index, view);
            }
        }

        @Override
        public void onChildViewDetachedFromWindow(View view) {
            // No-op
        }
    }

    private class PagerListener implements ViewPager.OnPageChangeListener {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            // No-op
        }

        @Override
        public void onPageSelected(int position) {
            notifyPagerItemChanged(position);
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            if (state == ViewPager.SCROLL_STATE_IDLE) applyItemFromPager();
        }
    }

}
