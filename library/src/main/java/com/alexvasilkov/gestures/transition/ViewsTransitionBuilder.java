package com.alexvasilkov.gestures.transition;

import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;

import com.alexvasilkov.gestures.transition.internal.FromRecyclerViewListener;
import com.alexvasilkov.gestures.transition.internal.IntoViewPagerListener;

public class ViewsTransitionBuilder<ID> {

    private final ViewsTransitionAnimator<ID> animator = new ViewsTransitionAnimator<>();

    public ViewsTransitionBuilder<ID> fromRecyclerView(@NonNull RecyclerView recyclerView,
                                                       @NonNull ViewsTracker<RecyclerView, ID> tracker) {
        animator.setFromListener(new FromRecyclerViewListener<>(recyclerView, tracker, animator));
        return this;
    }

    public ViewsTransitionBuilder<ID> intoViewPager(@NonNull ViewPager viewPager,
                                                    @NonNull ViewsTracker<ViewPager, ID> helper) {
        animator.setToListener(new IntoViewPagerListener<>(viewPager, helper, animator));
        return this;
    }

    public ViewsTransitionAnimator<ID> build() {
        return animator;
    }

}
