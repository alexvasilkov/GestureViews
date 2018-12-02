package com.alexvasilkov.gestures.transition;

import android.view.View;
import android.widget.ListView;

import com.alexvasilkov.gestures.transition.internal.FromListViewListener;
import com.alexvasilkov.gestures.transition.internal.FromRecyclerViewListener;
import com.alexvasilkov.gestures.transition.internal.IntoViewPagerListener;
import com.alexvasilkov.gestures.transition.tracker.FromTracker;
import com.alexvasilkov.gestures.transition.tracker.IntoTracker;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

/**
 * @deprecated Use {@link GestureTransitions} instead.
 */
@SuppressWarnings({ "deprecation", "unused", "WeakerAccess" }) // Class is left for compatibility
@Deprecated
public class ViewsTransitionBuilder<ID> {

    private final ViewsTransitionAnimator<ID> animator = new ViewsTransitionAnimator<>();

    public ViewsTransitionBuilder<ID> fromRecyclerView(
            @NonNull RecyclerView recyclerView,
            @NonNull ViewsTracker<ID> tracker) {

        animator.setFromListener(
                new FromRecyclerViewListener<>(recyclerView, toFromTracker(tracker), true));

        return this;
    }

    public ViewsTransitionBuilder<ID> fromListView(
            @NonNull ListView listView,
            @NonNull ViewsTracker<ID> tracker) {

        animator.setFromListener(
                new FromListViewListener<>(listView, toFromTracker(tracker), true));

        return this;
    }

    public ViewsTransitionBuilder<ID> intoViewPager(
            @NonNull ViewPager viewPager,
            @NonNull ViewsTracker<ID> tracker) {

        animator.setToListener(new IntoViewPagerListener<>(viewPager, toIntoTracker(tracker)));

        return this;
    }

    public ViewsTransitionAnimator<ID> build() {
        return animator;
    }


    private static <ID> FromTracker<ID> toFromTracker(final ViewsTracker<ID> tracker) {
        return new FromTracker<ID>() {
            @Override
            public int getPositionById(@NonNull ID id) {
                return tracker.getPositionForId(id);
            }

            @Override
            public View getViewById(@NonNull ID id) {
                return tracker.getViewForPosition(tracker.getPositionForId(id));
            }
        };
    }

    private static <ID> IntoTracker<ID> toIntoTracker(final ViewsTracker<ID> tracker) {
        return new IntoTracker<ID>() {
            @Override
            public ID getIdByPosition(int position) {
                return tracker.getIdForPosition(position);
            }

            @Override
            public int getPositionById(@NonNull ID id) {
                return tracker.getPositionForId(id);
            }

            @Override
            public View getViewById(@NonNull ID id) {
                return tracker.getViewForPosition(tracker.getPositionForId(id));
            }
        };
    }

}
