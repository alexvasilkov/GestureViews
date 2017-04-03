package com.alexvasilkov.gestures.transition;

import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ListView;

import com.alexvasilkov.gestures.transition.ViewsTransitionAnimator.RequestListener;
import com.alexvasilkov.gestures.transition.internal.FromListViewListener;
import com.alexvasilkov.gestures.transition.internal.FromRecyclerViewListener;
import com.alexvasilkov.gestures.transition.internal.IntoViewPagerListener;
import com.alexvasilkov.gestures.transition.tracker.FromTracker;
import com.alexvasilkov.gestures.transition.tracker.IntoTracker;
import com.alexvasilkov.gestures.views.interfaces.AnimatorView;

/**
 * Transition animation builder.
 */
public class GestureTransitions<ID> {

    // ViewsTransitionAnimator' public constructor is temporary deprecated
    @SuppressWarnings("deprecation")
    private final ViewsTransitionAnimator<ID> animator = new ViewsTransitionAnimator<>();

    private GestureTransitions() {}

    public static <ID> GestureTransitions<ID> from(@NonNull RequestListener<ID> listener) {
        final GestureTransitions<ID> builder = new GestureTransitions<>();
        builder.animator.setFromListener(listener);
        return builder;
    }

    public static <ID> GestureTransitions<ID> from(@NonNull final View view) {
        return from(new RequestListener<ID>() {
            @Override
            public void onRequestView(@NonNull ID id) {
                getAnimator().setFromView(id, view);
            }
        });
    }

    public static <ID> GestureTransitions<ID> from(
            @NonNull RecyclerView recyclerView,
            @NonNull FromTracker<ID> tracker) {
        return from(new FromRecyclerViewListener<>(recyclerView, tracker));
    }

    public static <ID> GestureTransitions<ID> from(
            @NonNull ListView listView,
            @NonNull FromTracker<ID> tracker) {
        return from(new FromListViewListener<>(listView, tracker));
    }

    @SuppressWarnings("unused") // Public API
    public static <ID> GestureTransitions<ID> fromNone() {
        return from(new RequestListener<ID>() {
            @Override
            public void onRequestView(@NonNull ID id) {
                getAnimator().setFromNone(id);
            }
        });
    }


    public ViewsTransitionAnimator<ID> into(@NonNull RequestListener<ID> listener) {
        animator.setToListener(listener);
        return animator;
    }

    public ViewsTransitionAnimator<ID> into(@NonNull final AnimatorView view) {
        return into(new RequestListener<ID>() {
            @Override
            public void onRequestView(@NonNull ID id) {
                getAnimator().setToView(id, view);
            }
        });
    }

    public ViewsTransitionAnimator<ID> into(
            @NonNull ViewPager viewPager,
            @NonNull IntoTracker<ID> tracker) {
        return into(new IntoViewPagerListener<>(viewPager, tracker));
    }

}
