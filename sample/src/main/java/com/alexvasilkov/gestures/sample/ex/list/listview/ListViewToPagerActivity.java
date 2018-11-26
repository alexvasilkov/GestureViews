package com.alexvasilkov.gestures.sample.ex.list.listview;

import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import com.alexvasilkov.gestures.commons.RecyclePagerAdapter;
import com.alexvasilkov.gestures.sample.R;
import com.alexvasilkov.gestures.sample.base.BaseSettingsActivity;
import com.alexvasilkov.gestures.sample.demo.DemoActivity;
import com.alexvasilkov.gestures.sample.ex.utils.Painting;
import com.alexvasilkov.gestures.transition.GestureTransitions;
import com.alexvasilkov.gestures.transition.ViewsTransitionAnimator;
import com.alexvasilkov.gestures.transition.tracker.SimpleTracker;

import androidx.viewpager.widget.ViewPager;

/**
 * This example demonstrates images animation from ListView into ViewPager.<br>
 * Note, that it is advised to use RecyclerView instead of ListView in most cases.
 * RecyclerView usage is almost the same. See also {@link DemoActivity}.
 */
public class ListViewToPagerActivity extends BaseSettingsActivity {

    private ListView list;
    private ViewPager pager;
    private View background;

    private PagerAdapter pagerAdapter;
    private ViewsTransitionAnimator<Integer> animator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.list_screen);

        final Painting[] paintings = Painting.list(getResources());

        // Initializing ListView
        list = findViewById(R.id.list_view);
        list.setAdapter(new ListAdapter(paintings, this::onPaintingClick));

        // Initializing ViewPager
        pager = findViewById(R.id.list_pager);
        pagerAdapter = new PagerAdapter(pager, paintings, getSettingsController());
        pager.setAdapter(pagerAdapter);
        pager.setPageMargin(getResources().getDimensionPixelSize(R.dimen.view_pager_margin));

        // Initializing images animator. It requires us to provide FromTracker and IntoTracker items
        // that are used to find images views for particular item IDs in the list and in the pager
        // to keep them in sync.
        // In this example we will use SimpleTracker which will track images by their positions,
        // if you have a more complicated case see "complex" package for advanced examples.
        final SimpleTracker listTracker = new SimpleTracker() {
            @Override
            public View getViewAt(int position) {
                View itemView = list.getChildAt(position - list.getFirstVisiblePosition());
                return itemView == null ? null : ListAdapter.getImageView(itemView);
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
        background = findViewById(R.id.list_full_background);
        animator.addPositionUpdateListener((pos, isLeaving) -> applyImageAnimationState(pos));
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

    private void onPaintingClick(int position) {
        // Animating image transition from given list position into pager
        animator.enter(position, true);
    }

    private void applyImageAnimationState(float position) {
        background.setVisibility(position == 0f ? View.INVISIBLE : View.VISIBLE);
        background.setAlpha(position);
    }

}
