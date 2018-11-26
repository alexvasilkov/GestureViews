package com.alexvasilkov.gestures.transition.internal;

import android.view.View;

import com.alexvasilkov.gestures.transition.tracker.FromTracker;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.OnChildAttachStateChangeListener;

public class FromRecyclerViewListener<ID> extends FromBaseListener<RecyclerView, ID> {

    public FromRecyclerViewListener(final RecyclerView list, final FromTracker<ID> tracker,
            boolean autoScroll) {

        super(list, tracker, autoScroll);

        if (!autoScroll) {
            // No need to track items views if auto scroll is disabled
            return;
        }

        // Tracking attached list items to pick up newly visible views
        list.addOnChildAttachStateChangeListener(new OnChildAttachStateChangeListener() {
            @Override
            public void onChildViewAttachedToWindow(View view) {
                final ID id = getAnimator() == null ? null : getAnimator().getRequestedId();

                // If view was requested and list is scrolled we should try to find the view again
                if (id != null) {
                    int position = list.getChildAdapterPosition(view);
                    int positionById = tracker.getPositionById(id);
                    if (position == positionById) {
                        View from = tracker.getViewById(id);
                        if (from != null) {
                            // View is found, we can set up 'from' view position now
                            getAnimator().setFromView(id, from);
                        }
                    }
                }
            }

            @Override
            public void onChildViewDetachedFromWindow(View view) {
                // No-op
            }
        });
    }

    @Override
    boolean isShownInList(RecyclerView list, int pos) {
        return list.findViewHolderForLayoutPosition(pos) != null;
    }

    @Override
    void scrollToPosition(RecyclerView list, int pos) {
        if (list.getLayoutManager() instanceof LinearLayoutManager) {
            // Centering item in its parent
            final LinearLayoutManager manager = (LinearLayoutManager) list.getLayoutManager();
            final boolean isHorizontal = manager.getOrientation() == LinearLayoutManager.HORIZONTAL;

            int offset = isHorizontal
                    ? (list.getWidth() - list.getPaddingLeft() - list.getPaddingRight()) / 2
                    : (list.getHeight() - list.getPaddingTop() - list.getPaddingBottom()) / 2;

            final RecyclerView.ViewHolder holder = list.findViewHolderForAdapterPosition(pos);
            if (holder != null) {
                final View view = holder.itemView;
                offset -= isHorizontal ? view.getWidth() / 2 : view.getHeight() / 2;
            }

            manager.scrollToPositionWithOffset(pos, offset);
        } else {
            list.scrollToPosition(pos);
        }
    }

}
