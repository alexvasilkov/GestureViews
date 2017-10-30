package com.alexvasilkov.gestures.sample.ex.list.recycler;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.alexvasilkov.gestures.animation.ViewPositionAnimator;
import com.alexvasilkov.gestures.commons.RecyclePagerAdapter;
import com.alexvasilkov.gestures.sample.R;
import com.alexvasilkov.gestures.sample.base.BaseExampleActivity;
import com.alexvasilkov.gestures.sample.ex.utils.Painting;
import com.alexvasilkov.gestures.transition.GestureTransitions;
import com.alexvasilkov.gestures.transition.ViewsTransitionAnimator;
import com.alexvasilkov.gestures.transition.tracker.SimpleTracker;

/**
 * This example demonstrates images animation from {@link RecyclerView} into {@link ViewPager}.
 */
public class RecyclerToPagerActivity extends BaseExampleActivity
        implements RecyclerAdapter.OnPaintingClickListener {

    private RecyclerView list;
    private ViewPager pager;
    private View background;

    private PagerAdapter pagerAdapter;
    private ViewsTransitionAnimator<Integer> animator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.list_recycler_screen);

        final Painting[] paintings = Painting.list(getResources());

        // Initializing ListView
        list = findViewById(R.id.recycler_list);
        list.setLayoutManager(new LinearLayoutManager(this));
        list.setAdapter(new RecyclerAdapter(paintings, this));

        // Initializing ViewPager
        pager = findViewById(R.id.recycler_pager);
        pagerAdapter = new PagerAdapter(pager, paintings, getSettingsListener());
        pager.setAdapter(pagerAdapter);
        pager.setPageMargin(getResources().getDimensionPixelSize(R.dimen.view_pager_margin));

        // Initializing images animator. It requires us to provide FromTracker and IntoTracker items
        // that are used to find images views for particular item IDs in the list and in the pager
        // to keep them in sync.
        // In this example we will use SimpleTracker which will track images by their positions,
        // if you have a more complicated case see further examples.
        final SimpleTracker listTracker = new SimpleTracker() {
            @Override
            public View getViewAt(int position) {
                RecyclerView.ViewHolder holder = list.findViewHolderForLayoutPosition(position);
                return holder == null ? null : RecyclerAdapter.getImageView(holder);
            }
        };

        final SimpleTracker pagerTracker = new SimpleTracker() {
            @Override
            public View getViewAt(int position) {
                RecyclePagerAdapter.ViewHolder holder = pagerAdapter.getViewHolder(position);
                return holder == null ? null : PagerAdapter.getImageView(holder);
            }
        };

        animator = GestureTransitions.from(list, listTracker).into(pager, pagerTracker);

        // Setting up background animation during image transition
        background = findViewById(R.id.recycler_full_background);
        animator.addPositionUpdateListener(new ViewPositionAnimator.PositionUpdateListener() {
            @Override
            public void onPositionUpdate(float position, boolean isLeaving) {
                background.setVisibility(position == 0f ? View.INVISIBLE : View.VISIBLE);
                background.setAlpha(position);
            }
        });
    }

    @Override
    public void onBackPressed() {
        // We should leave full image mode instead of closing the screen
        if (!animator.isLeaving()) {
            animator.exit(true);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onSettingsChanged() {
        // Applying settings from toolbar menu, see BaseExampleActivity
        pager.getAdapter().notifyDataSetChanged();
    }

    @Override
    public void onPaintingClick(int position) {
        // Animating image transition from given list position into pager
        animator.enter(position, true);
    }

}
