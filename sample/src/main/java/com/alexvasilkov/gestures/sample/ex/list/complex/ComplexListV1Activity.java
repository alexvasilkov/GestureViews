package com.alexvasilkov.gestures.sample.ex.list.complex;

import android.view.View;

import com.alexvasilkov.gestures.commons.RecyclePagerAdapter;
import com.alexvasilkov.gestures.sample.ex.utils.Painting;
import com.alexvasilkov.gestures.transition.GestureTransitions;
import com.alexvasilkov.gestures.transition.ViewsTransitionAnimator;
import com.alexvasilkov.gestures.transition.tracker.FromTracker;
import com.alexvasilkov.gestures.transition.tracker.IntoTracker;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

/**
 * This example demonstrates images animation from RecyclerView to ViewPager when each list item can
 * contain from 0 to N images which should be viewed together (combined from across all list items).
 */
public class ComplexListV1Activity extends BaseComplexListActivity {

    private List<Painting> paintings;
    private List<Position> positions;

    private ViewsTransitionAnimator<Position> animator;

    @Override
    protected List<ListItem> createItems() {
        final List<ListItem> items = ListItem.createItemsV1(this);

        // We are going to show all paintings from across all items in a view pager.
        // To do so we need to prepare list of all paintings and their respective positions.
        // Images positions are needed to properly track list-to-pager transitions later.
        paintings = new ArrayList<>();
        positions = new ArrayList<>();

        for (int itemPos = 0; itemPos < items.size(); itemPos++) {
            List<Painting> itemPaintings = items.get(itemPos).paintings;
            if (itemPaintings != null) {
                for (int imagePos = 0; imagePos < itemPaintings.size(); imagePos++) {
                    paintings.add(itemPaintings.get(imagePos));
                    positions.add(new Position(itemPos, imagePos));
                }
            }
        }

        return items;
    }

    @Override
    protected ViewsTransitionAnimator createAnimator(
            final RecyclerView list, final ViewPager pager) {
        // Initializing images animator. It requires us to provide FromTracker and IntoTracker items
        // that are used to find images views for particular item IDs in the list and in the pager
        // to keep them in sync.
        // In this example we will show all images from across all list items in a pager.
        // We will use combination {list item position, image position} to precisely identify each
        // image and its position in the list. We will also need to map images positions inside
        // the pager to there list positions and vice versa, see data preparations above.

        final FromTracker<Position> listTracker = new FromTracker<Position>() {
            @Override
            public View getViewById(@NonNull Position pos) {
                // We should return image view of particular image inside corresponding list item
                RecyclerView.ViewHolder holder = list.findViewHolderForLayoutPosition(pos.itemPos);
                return holder == null ? null : ListAdapter.getImageView(holder, pos.imagePos);
            }

            @Override
            public int getPositionById(@NonNull Position pos) {
                // We should return position of corresponding list item
                return pos.itemPos;
            }
        };

        final IntoTracker<Position> pagerTracker = new IntoTracker<Position>() {
            @Override
            public Position getIdByPosition(int position) {
                // We should return list position for corresponding pager position
                return positions.get(position);
            }

            @Override
            public int getPositionById(@NonNull Position pos) {
                // We should return pager position for corresponding list position
                return positions.indexOf(pos);
            }

            @Override
            public View getViewById(@NonNull Position pos) {
                // We should return image view for a given pager position
                int pagerPos = getPositionById(pos);
                PagerAdapter adapter = (PagerAdapter) pager.getAdapter();
                RecyclePagerAdapter.ViewHolder holder = adapter.getViewHolder(pagerPos);
                return holder == null ? null : PagerAdapter.getImageView(holder);
            }
        };

        return animator = GestureTransitions.from(list, listTracker).into(pager, pagerTracker);
    }

    @Override
    protected void openImageInPager(PagerAdapter adapter, int itemPos, int imagePos) {
        // Showing paintings list across all list item
        adapter.setPaintings(paintings);
        // Starting image transition
        animator.enter(new Position(itemPos, imagePos), true);
    }


    /**
     * Represents image position both in the list of items and inside particular item.
     */
    private class Position {
        final int itemPos;
        final int imagePos;

        Position(int itemPos, int imagePos) {
            this.itemPos = itemPos;
            this.imagePos = imagePos;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }

            Position pos = (Position) obj;
            return itemPos == pos.itemPos && imagePos == pos.imagePos;
        }

        @Override
        public int hashCode() {
            return 31 * itemPos + imagePos;
        }
    }

}
