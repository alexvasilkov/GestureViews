package com.alexvasilkov.gestures.transition.internal;

import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.alexvasilkov.gestures.animation.ViewPositionAnimator;
import com.alexvasilkov.gestures.transition.ViewsCoordinator;
import com.alexvasilkov.gestures.transition.ViewsTracker;
import com.alexvasilkov.gestures.transition.ViewsTransitionAnimator;

public class FromRecyclerViewListener<ID> implements ViewsCoordinator.OnRequestViewListener<ID> {

    private static final Rect LOCATION_PARENT = new Rect(), LOCATION = new Rect();

    private final RecyclerView mRecyclerView;
    private final ViewsTracker<ID> mTracker;
    private final ViewsTransitionAnimator<ID> mAnimator;

    private ID mId;
    private boolean mScrollHalfVisibleItems;

    public FromRecyclerViewListener(@NonNull RecyclerView recyclerView,
                                    @NonNull ViewsTracker<ID> tracker,
                                    @NonNull ViewsTransitionAnimator<ID> animator) {
        mRecyclerView = recyclerView;
        mTracker = tracker;
        mAnimator = animator;

        mRecyclerView.addOnChildAttachStateChangeListener(new ChildStateListener());
        mAnimator.addPositionUpdateListener(new UpdateListener());
    }

    @Override
    public void onRequestView(@NonNull ID id) {
        // Trying to find requested view on screen. If it is not currently on screen
        // or it is not fully visible than we should scroll to it at first.
        mId = id;
        int position = mTracker.getPositionForId(id);

        if (position == ViewsTracker.NO_POSITION) return; // Nothing we can do

        View view = mTracker.getViewForPosition(position);
        if (view == null) {
            mRecyclerView.smoothScrollToPosition(position);
        } else {
            mAnimator.setFromView(id, view);

            if (mScrollHalfVisibleItems) {
                mRecyclerView.getGlobalVisibleRect(LOCATION_PARENT);
                LOCATION_PARENT.left += mRecyclerView.getPaddingLeft();
                LOCATION_PARENT.right -= mRecyclerView.getPaddingRight();
                LOCATION_PARENT.top += mRecyclerView.getPaddingTop();
                LOCATION_PARENT.bottom -= mRecyclerView.getPaddingBottom();

                view.getGlobalVisibleRect(LOCATION);
                if (!LOCATION_PARENT.contains(LOCATION)
                        || view.getWidth() > LOCATION.width()
                        || view.getHeight() > LOCATION.height()) {
                    mRecyclerView.smoothScrollToPosition(position);
                }
            }
        }
    }

    private class ChildStateListener implements RecyclerView.OnChildAttachStateChangeListener {
        @Override
        public void onChildViewAttachedToWindow(View view) {
            int position = mRecyclerView.getChildAdapterPosition(view);
            if (mId != null && mId.equals(mTracker.getIdForPosition(position))) {
                View from = mTracker.getViewForPosition(position);
                if (from != null) mAnimator.setFromView(mId, from);
            }
        }

        @Override
        public void onChildViewDetachedFromWindow(View view) {
            // No-op
        }
    }

    private class UpdateListener implements ViewPositionAnimator.PositionUpdateListener {
        @Override
        public void onPositionUpdate(float state, boolean isLeaving) {
            if (state == 0f && isLeaving) mId = null;
            mRecyclerView.setVisibility(state == 1f && !isLeaving ? View.INVISIBLE : View.VISIBLE);
            mScrollHalfVisibleItems = state == 1f; // Only scroll if we in full mode
        }
    }

}
