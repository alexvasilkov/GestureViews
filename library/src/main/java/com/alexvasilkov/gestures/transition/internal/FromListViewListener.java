package com.alexvasilkov.gestures.transition.internal;

import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;

import com.alexvasilkov.gestures.animation.ViewPositionAnimator;
import com.alexvasilkov.gestures.transition.ViewsCoordinator;
import com.alexvasilkov.gestures.transition.ViewsTracker;
import com.alexvasilkov.gestures.transition.ViewsTransitionAnimator;

public class FromListViewListener<ID> implements ViewsCoordinator.OnRequestViewListener<ID> {

    private static final Rect locationParent = new Rect();
    private static final Rect locationChild = new Rect();

    private final ListView listView;
    private final ViewsTracker<ID> tracker;
    private final ViewsTransitionAnimator<ID> animator;

    private ID id;
    private boolean scrollHalfVisibleItems;

    public FromListViewListener(@NonNull ListView listView,
            @NonNull ViewsTracker<ID> tracker,
            @NonNull ViewsTransitionAnimator<ID> animator) {
        this.listView = listView;
        this.tracker = tracker;
        this.animator = animator;

        this.listView.setOnScrollListener(new ScrollListener());
        this.animator.addPositionUpdateListener(new UpdateListener());
    }

    @Override
    public void onRequestView(@NonNull ID id) {
        // Trying to find requested view on screen. If it is not currently on screen
        // or it is not fully visible than we should scroll to it at first.
        this.id = id;
        int position = tracker.getPositionForId(id);

        if (position == ViewsTracker.NO_POSITION) {
            return; // Nothing we can do
        }

        View view = tracker.getViewForPosition(position);
        if (view == null) {
            listView.setSelection(position);
        } else {
            animator.setFromView(id, view);

            if (scrollHalfVisibleItems) {
                listView.getGlobalVisibleRect(locationParent);
                locationParent.left += listView.getPaddingLeft();
                locationParent.right -= listView.getPaddingRight();
                locationParent.top += listView.getPaddingTop();
                locationParent.bottom -= listView.getPaddingBottom();

                view.getGlobalVisibleRect(locationChild);
                if (!locationParent.contains(locationChild)
                        || view.getWidth() > locationChild.width()
                        || view.getHeight() > locationChild.height()) {
                    listView.setSelection(position);
                }
            }
        }
    }

    private class ScrollListener implements AbsListView.OnScrollListener {
        @Override
        public void onScroll(AbsListView view, int firstVisible, int visibleCount, int totalCount) {
            if (id == null) {
                return; // Nothing to do
            }
            for (int position = firstVisible; position < firstVisible + visibleCount; position++) {
                if (id.equals(tracker.getIdForPosition(position))) {
                    View from = tracker.getViewForPosition(position);
                    if (from != null) {
                        animator.setFromView(id, from);
                    }
                }
            }
        }

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            // No-op
        }
    }

    private class UpdateListener implements ViewPositionAnimator.PositionUpdateListener {
        @Override
        public void onPositionUpdate(float state, boolean isLeaving) {
            if (state == 0f && isLeaving) {
                id = null;
            }
            listView.setVisibility(state == 1f && !isLeaving ? View.INVISIBLE : View.VISIBLE);
            scrollHalfVisibleItems = state == 1f; // Only scroll if we in full mode
        }
    }

}
