package com.alexvasilkov.gestures.transition.internal;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.OnChildAttachStateChangeListener;
import android.view.View;

import com.alexvasilkov.gestures.transition.tracker.FromTracker;

public class FromRecyclerViewListener<ID> extends FromBaseListener<RecyclerView, ID> {

    public FromRecyclerViewListener(final RecyclerView recycler, final FromTracker<ID> tracker) {
        super(recycler, tracker);

        recycler.addOnChildAttachStateChangeListener(new OnChildAttachStateChangeListener() {
            @Override
            public void onChildViewAttachedToWindow(View view) {
                final ID id = getAnimator() == null ? null : getAnimator().getRequestedId();

                if (id != null) {
                    int position = recycler.getChildAdapterPosition(view);
                    int positionById = tracker.getPositionById(id);
                    if (position == positionById) {
                        View from = tracker.getViewById(id);
                        if (from != null) {
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
    void scrollToPosition(RecyclerView parent, int pos) {
        if (parent.getLayoutManager() instanceof LinearLayoutManager) {
            // Centering item in its parent
            final LinearLayoutManager manager = (LinearLayoutManager) parent.getLayoutManager();
            final boolean isHorizontal = manager.getOrientation() == LinearLayoutManager.HORIZONTAL;

            int offset = isHorizontal
                    ? (parent.getWidth() - parent.getPaddingLeft() - parent.getPaddingRight()) / 2
                    : (parent.getHeight() - parent.getPaddingTop() - parent.getPaddingBottom()) / 2;

            final RecyclerView.ViewHolder holder = parent.findViewHolderForAdapterPosition(pos);
            if (holder != null) {
                final View view = holder.itemView;
                offset -= isHorizontal ? view.getWidth() / 2 : view.getHeight() / 2;
            }

            manager.scrollToPositionWithOffset(pos, offset);
        } else {
            parent.scrollToPosition(pos);
        }
    }

}
