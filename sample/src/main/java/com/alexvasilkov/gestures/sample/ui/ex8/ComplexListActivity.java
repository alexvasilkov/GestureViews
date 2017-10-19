package com.alexvasilkov.gestures.sample.ui.ex8;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.alexvasilkov.android.commons.ui.Views;
import com.alexvasilkov.gestures.animation.ViewPositionAnimator;
import com.alexvasilkov.gestures.commons.DepthPageTransformer;
import com.alexvasilkov.gestures.commons.RecyclePagerAdapter;
import com.alexvasilkov.gestures.sample.R;
import com.alexvasilkov.gestures.sample.ui.base.BaseExampleActivity;
import com.alexvasilkov.gestures.sample.utils.DecorUtils;
import com.alexvasilkov.gestures.transition.GestureTransitions;
import com.alexvasilkov.gestures.transition.ViewsTransitionAnimator;
import com.alexvasilkov.gestures.transition.tracker.FromTracker;
import com.alexvasilkov.gestures.transition.tracker.SimpleTracker;

/**
 * This example demonstrates images animation from RecyclerView to ViewPager for cases where each
 * list item can contain from 0 to 3 images.
 */
public class ComplexListActivity extends BaseExampleActivity
        implements ViewPositionAnimator.PositionUpdateListener, ListAdapter.OnImageClickListener {

    private ViewHolder views;
    private PagerAdapter pagerAdapter;

    private ViewsTransitionAnimator<Integer> animator;

    private int selectedPosition = FromTracker.NO_POSITION;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.ex8_screen);
        views = new ViewHolder(this);

        setSupportActionBar(views.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        initDecorMargins();
        initList();
        initPager();
        initAnimator();
    }

    @Override
    public void onBackPressed() {
        if (!animator.isLeaving()) {
            animator.exit(true);
        } else {
            super.onBackPressed();
        }
    }

    private void initDecorMargins() {
        // Adjusting margins and paddings to fit translucent decor
        DecorUtils.paddingForStatusBar(views.toolbar, true);
        DecorUtils.paddingForStatusBar(views.pagerToolbar, true);
        DecorUtils.marginForStatusBar(views.list);
        DecorUtils.paddingForNavBar(views.list);
    }

    private void initList() {
        // Setting up images grid
        views.list.setLayoutManager(new LinearLayoutManager(this));

        final ListAdapter listAdapter = new ListAdapter(Item.createItemsList(this), this);
        views.list.setAdapter(listAdapter);
    }

    private void initPager() {
        // Setting up pager views
        pagerAdapter = new PagerAdapter(views.pager, getSettingsListener());

        views.pager.setAdapter(pagerAdapter);
        views.pager.setPageTransformer(true, new DepthPageTransformer());

        views.pagerToolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        views.pagerToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(@NonNull View view) {
                onBackPressed();
            }
        });
    }

    private void initAnimator() {
        final FromTracker<Integer> listTracker = new FromTracker<Integer>() {
            @Override
            public View getViewById(@NonNull Integer imagePos) {
                final RecyclerView.ViewHolder holder =
                        views.list.findViewHolderForLayoutPosition(selectedPosition);
                return holder == null ? null : ListAdapter.getImage(holder, imagePos);
            }

            @Override
            public int getPositionById(@NonNull Integer imagePos) {
                final boolean hasHolder =
                        views.list.findViewHolderForLayoutPosition(selectedPosition) != null;
                return !hasHolder || getViewById(imagePos) != null
                        ? selectedPosition : FromTracker.NO_POSITION;
            }
        };

        final SimpleTracker pagerTracker = new SimpleTracker() {
            @Override
            public View getViewAt(int pos) {
                final RecyclePagerAdapter.ViewHolder holder = pagerAdapter.getViewHolder(pos);
                return holder == null ? null : PagerAdapter.getImage(holder);
            }
        };

        animator = GestureTransitions.from(views.list, listTracker).into(views.pager, pagerTracker);
        animator.addPositionUpdateListener(this);
    }


    @Override
    protected void onSettingsChanged() {
        // No-op
    }

    @Override
    public void onPositionUpdate(float position, boolean isLeaving) {
        views.pagerBackground.setVisibility(position == 0f ? View.INVISIBLE : View.VISIBLE);
        views.pagerBackground.getBackground().setAlpha((int) (255 * position));

        views.pagerToolbar.setVisibility(position == 0f ? View.INVISIBLE : View.VISIBLE);
        views.pagerToolbar.setAlpha(position);

        if (isLeaving && position == 0f) {
            pagerAdapter.setActivated(false);
        }

        // Fading out images without "from" position
        if (animator.getFromView() == null && isLeaving) {
            float toPosition = animator.getToView() == null
                    ? 1f : animator.getToView().getPositionAnimator().getToPosition();
            views.pager.setAlpha(position / toPosition);
        } else {
            views.pager.setAlpha(1f);
        }
    }

    @Override
    public void onImageClick(Item item, int itemPos, int imagePos) {
        selectedPosition = itemPos;

        pagerAdapter.setPaintings(item.paintings);
        pagerAdapter.setActivated(true);

        animator.enter(imagePos, true);
    }


    private class ViewHolder {
        final Toolbar toolbar;
        final RecyclerView list;
        final ViewPager pager;
        final Toolbar pagerToolbar;
        final View pagerBackground;

        ViewHolder(Activity activity) {
            toolbar = Views.find(activity, R.id.toolbar);
            list = Views.find(activity, R.id.complex_list);
            pager = Views.find(activity, R.id.complex_pager);
            pagerToolbar = Views.find(activity, R.id.complex_full_toolbar);
            pagerBackground = Views.find(activity, R.id.complex_full_background);
        }
    }

}
