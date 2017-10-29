package com.alexvasilkov.gestures.sample.ui.ex.list.complex;

import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.alexvasilkov.gestures.commons.RecyclePagerAdapter;
import com.alexvasilkov.gestures.transition.GestureTransitions;
import com.alexvasilkov.gestures.transition.ViewsTransitionAnimator;
import com.alexvasilkov.gestures.transition.tracker.FromTracker;
import com.alexvasilkov.gestures.transition.tracker.IntoTracker;
import com.alexvasilkov.gestures.transition.tracker.SimpleTracker;

import java.util.List;

/**
 * This example demonstrates images animation from RecyclerView to ViewPager when each list item can
 * contain from 0 to N images which should be viewed independently from other list items.
 */
public class ComplexListV2Activity extends BaseComplexListActivity {

    private List<ListItem> items;
    private ViewsTransitionAnimator<Integer> animator;

    private int currItemPos;

    @Override
    protected List<ListItem> createItems() {
        return items = ListItem.createItemsV2(this);
    }

    @Override
    protected ViewsTransitionAnimator createAnimator(
            final RecyclerView list, final ViewPager pager) {
        // Initializing images animator. It requires us to provide FromTracker and IntoTracker items
        // that are used to find images views for particular item IDs in the list and in the pager
        // to keep them in sync.
        // In this example we will show several images from single list item in a pager.
        // When image is clicked we will store selected list item position locally and then request
        // particular image within this item (by image position inside the item).

        final FromTracker<Integer> listTracker = new FromTracker<Integer>() {
            @Override
            public View getViewById(@NonNull Integer imagePos) {
                // We should return image view of particular image inside current list item
                RecyclerView.ViewHolder holder = list.findViewHolderForLayoutPosition(currItemPos);
                return holder == null ? null : ListAdapter.getImageView(holder, imagePos);
            }

            @Override
            public int getPositionById(@NonNull Integer imagePos) {
                // We should return position of corresponding list item, but since we are showing
                // images belonging to a single list item we can just return this item's position
                return currItemPos;
            }
        };

        final IntoTracker<Integer> pagerTracker = new SimpleTracker() {
            @Override
            protected View getViewAt(int imagePos) {
                // We should return image view for a given pager position
                PagerAdapter adapter = (PagerAdapter) pager.getAdapter();
                RecyclePagerAdapter.ViewHolder holder = adapter.getViewHolder(imagePos);
                return holder == null ? null : PagerAdapter.getImageView(holder);
            }
        };

        return animator = GestureTransitions.from(list, listTracker).into(pager, pagerTracker);
    }

    @Override
    protected void openImageInPager(PagerAdapter adapter, int itemPos, int imagePos) {
        // Saving current list item position
        currItemPos = itemPos;
        // Showing paintings list for particular list item
        adapter.setPaintings(items.get(itemPos).paintings);
        // Starting image transition
        animator.enter(imagePos, true);
    }

}
