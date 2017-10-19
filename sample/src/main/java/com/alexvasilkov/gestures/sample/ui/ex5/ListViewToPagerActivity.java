package com.alexvasilkov.gestures.sample.ui.ex5;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.ListView;

import com.alexvasilkov.android.commons.ui.Views;
import com.alexvasilkov.gestures.animation.ViewPositionAnimator;
import com.alexvasilkov.gestures.commons.RecyclePagerAdapter;
import com.alexvasilkov.gestures.sample.R;
import com.alexvasilkov.gestures.sample.ui.base.BaseExampleActivity;
import com.alexvasilkov.gestures.sample.ui.demo.DemoActivity;
import com.alexvasilkov.gestures.sample.ui.ex.Painting;
import com.alexvasilkov.gestures.transition.GestureTransitions;
import com.alexvasilkov.gestures.transition.ViewsTransitionAnimator;
import com.alexvasilkov.gestures.transition.tracker.SimpleTracker;

/**
 * This example demonstrates images animation from ListView into ViewPager.<br/>
 * Note, that it is advised to use RecyclerView instead of ListView in most cases.
 * RecyclerView usage is almost the same. See also {@link DemoActivity}.
 */
public class ListViewToPagerActivity extends BaseExampleActivity
        implements PaintingsListAdapter.OnPaintingListener {

    private ListView list;
    private ViewPager pager;
    private View background;

    private PaintingsPagerAdapter pagerAdapter;
    private ViewsTransitionAnimator<Integer> animator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.ex5_screen);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final Painting[] paintings = Painting.list(getResources());

        // Initializing ListView
        list = Views.find(this, R.id.transition_list);
        list.setAdapter(new PaintingsListAdapter(paintings, this));

        // Initializing ViewPager
        pager = Views.find(this, R.id.transition_pager);
        pagerAdapter = new PaintingsPagerAdapter(pager, paintings, getSettingsListener());
        pager.setAdapter(pagerAdapter);
        pager.setPageMargin(getResources().getDimensionPixelSize(R.dimen.view_pager_margin));

        // Initializing images animator
        final SimpleTracker listTracker = new SimpleTracker() {
            @Override
            public View getViewAt(int position) {
                int first = list.getFirstVisiblePosition();
                int last = list.getLastVisiblePosition();
                if (position < first || position > last) {
                    return null;
                } else {
                    View itemView = list.getChildAt(position - first);
                    return PaintingsListAdapter.getImage(itemView);
                }
            }
        };

        final SimpleTracker pagerTracker = new SimpleTracker() {
            @Override
            public View getViewAt(int position) {
                RecyclePagerAdapter.ViewHolder holder = pagerAdapter.getViewHolder(position);
                return holder == null ? null : PaintingsPagerAdapter.getImage(holder);
            }
        };

        animator = GestureTransitions.from(list, listTracker).into(pager, pagerTracker);

        // Setting up background animation during image animation
        background = Views.find(this, R.id.transition_full_background);
        animator.addPositionUpdateListener(new ViewPositionAnimator.PositionUpdateListener() {
            @Override
            public void onPositionUpdate(float position, boolean isLeaving) {
                background.setVisibility(position == 0f ? View.INVISIBLE : View.VISIBLE);
                background.getBackground().setAlpha((int) (255 * position));
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (!animator.isLeaving()) {
            animator.exit(true);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onSettingsChanged() {
        pager.getAdapter().notifyDataSetChanged();
    }

    @Override
    public void onPaintingClick(int position) {
        animator.enter(position, true);
    }

}
