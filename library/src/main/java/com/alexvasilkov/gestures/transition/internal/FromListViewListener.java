package com.alexvasilkov.gestures.transition.internal;

import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;

import com.alexvasilkov.gestures.transition.tracker.FromTracker;

public class FromListViewListener<ID> extends FromBaseListener<ListView, ID> {

    public FromListViewListener(ListView list, final FromTracker<ID> tracker, boolean autoScroll) {
        super(list, tracker, autoScroll);

        if (!autoScroll) {
            // No need to track list view scrolling if auto scroll is disabled
            return;
        }

        // Tracking list view scrolling to pick up newly visible views
        list.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScroll(AbsListView view, int firstVisible, int visibleCount, int total) {
                final ID id = getAnimator() == null ? null : getAnimator().getRequestedId();

                // If view was requested and list is scrolled we should try to find the view again
                if (id != null) {
                    int position = tracker.getPositionById(id);
                    if (position >= firstVisible && position < firstVisible + visibleCount) {
                        View from = tracker.getViewById(id);
                        if (from != null) {
                            // View is found, we can set up 'from' view position now
                            getAnimator().setFromView(id, from);
                        }
                    }
                }
            }

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                // No-op
            }
        });
    }

    @Override
    boolean isShownInList(ListView list, int pos) {
        return pos >= list.getFirstVisiblePosition() && pos <= list.getLastVisiblePosition();
    }

    @Override
    void scrollToPosition(ListView list, int pos) {
        list.setSelection(pos);
    }

}
