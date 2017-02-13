package com.alexvasilkov.gestures.transition.internal;

import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;

import com.alexvasilkov.gestures.transition.ViewsTracker;

public class FromListViewListener<ID> extends FromBaseListener<ListView, ID> {

    public FromListViewListener(ListView listView, final ViewsTracker<ID> tracker) {
        super(listView, tracker);

        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScroll(AbsListView view, int firstVisible, int visibleCount, int total) {
                final ID id = getAnimator() == null ? null : getAnimator().getRequestedId();

                if (id != null) {
                    int position = tracker.getPositionForId(id);
                    if (position >= firstVisible && position < firstVisible + visibleCount) {
                        View from = tracker.getViewForPosition(position);
                        if (from != null) {
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
    void scrollToPosition(ListView parentView, int pos) {
        parentView.setSelection(pos);
    }

}
