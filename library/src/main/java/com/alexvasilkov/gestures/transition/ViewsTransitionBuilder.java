package com.alexvasilkov.gestures.transition;

import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;

import com.alexvasilkov.gestures.transition.internal.FromRecyclerViewListener;
import com.alexvasilkov.gestures.transition.internal.IntoViewPagerListener;

public class ViewsTransitionBuilder<ID> {

    private final ViewsTransitionAnimator<ID> animator = new ViewsTransitionAnimator<>();

    public ViewsTransitionBuilder<ID> fromRecycler(@NonNull ViewsTracker<RecyclerView, ID> helper) {
        animator.setFromListener(new FromRecyclerViewListener<>(helper, animator));
        return this;
    }

    public ViewsTransitionBuilder<ID> intoViewPager(@NonNull ViewsTracker<ViewPager, ID> helper) {
        animator.setToListener(new IntoViewPagerListener<>(helper, animator));
        return this;
    }

    public ViewsTransitionAnimator<ID> build() {
        return animator;
    }

}
