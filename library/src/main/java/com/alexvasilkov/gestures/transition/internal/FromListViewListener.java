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

    private static final Rect LOCATION_PARENT = new Rect(), LOCATION = new Rect();

    private final ListView mListView;
    private final ViewsTracker<ID> mTracker;
    private final ViewsTransitionAnimator<ID> mAnimator;

    private ID mId;
    private boolean mScrollHalfVisibleItems;

    public FromListViewListener(@NonNull ListView listView,
            @NonNull ViewsTracker<ID> tracker,
            @NonNull ViewsTransitionAnimator<ID> animator) {
        mListView = listView;
        mTracker = tracker;
        mAnimator = animator;

        mListView.setOnScrollListener(new ScrollListener());
        mAnimator.addPositionUpdateListener(new UpdateListener());
    }

    @Override
    public void onRequestView(@NonNull ID id) {
        // Trying to find requested view on screen. If it is not currently on screen
        // or it is not fully visible than we should scroll to it at first.
        mId = id;
        int position = mTracker.getPositionForId(id);

        if (position == ViewsTracker.NO_POSITION) {
            return; // Nothing we can do
        }

        View view = mTracker.getViewForPosition(position);
        if (view == null) {
            mListView.setSelection(position);
        } else {
            mAnimator.setFromView(id, view);

            if (mScrollHalfVisibleItems) {
                mListView.getGlobalVisibleRect(LOCATION_PARENT);
                LOCATION_PARENT.left += mListView.getPaddingLeft();
                LOCATION_PARENT.right -= mListView.getPaddingRight();
                LOCATION_PARENT.top += mListView.getPaddingTop();
                LOCATION_PARENT.bottom -= mListView.getPaddingBottom();

                view.getGlobalVisibleRect(LOCATION);
                if (!LOCATION_PARENT.contains(LOCATION)
                        || view.getWidth() > LOCATION.width()
                        || view.getHeight() > LOCATION.height()) {
                    mListView.setSelection(position);
                }
            }
        }
    }

    private class ScrollListener implements AbsListView.OnScrollListener {
        @Override
        public void onScroll(AbsListView view, int firstVisible, int visibleCount, int totalCount) {
            if (mId == null) {
                return; // Nothing to do
            }
            for (int position = firstVisible; position < firstVisible + visibleCount; position++) {
                if (mId.equals(mTracker.getIdForPosition(position))) {
                    View from = mTracker.getViewForPosition(position);
                    if (from != null) {
                        mAnimator.setFromView(mId, from);
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
                mId = null;
            }
            mListView.setVisibility(state == 1f && !isLeaving ? View.INVISIBLE : View.VISIBLE);
            mScrollHalfVisibleItems = state == 1f; // Only scroll if we in full mode
        }
    }

}
