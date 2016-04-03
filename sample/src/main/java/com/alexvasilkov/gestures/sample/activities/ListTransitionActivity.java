package com.alexvasilkov.gestures.sample.activities;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import com.alexvasilkov.android.commons.utils.Views;
import com.alexvasilkov.gestures.animation.ViewPositionAnimator;
import com.alexvasilkov.gestures.commons.RecyclePagerAdapter;
import com.alexvasilkov.gestures.sample.R;
import com.alexvasilkov.gestures.sample.adapters.PaintingsListAdapter;
import com.alexvasilkov.gestures.sample.adapters.PaintingsPagerAdapter;
import com.alexvasilkov.gestures.sample.logic.Painting;
import com.alexvasilkov.gestures.sample.logic.PaintingsHelper;
import com.alexvasilkov.gestures.sample.utils.GestureSettingsMenu;
import com.alexvasilkov.gestures.transition.SimpleViewsTracker;
import com.alexvasilkov.gestures.transition.ViewsTransitionAnimator;
import com.alexvasilkov.gestures.transition.ViewsTransitionBuilder;

public class ListTransitionActivity extends BaseActivity implements
        PaintingsListAdapter.OnPaintingListener,
        ViewPositionAnimator.PositionUpdateListener {

    private ViewHolder views;
    private GestureSettingsMenu settingsMenu;
    private PaintingsPagerAdapter pagerAdapter;
    private ViewsTransitionAnimator<Integer> animator;


    private final SimpleViewsTracker pagerTracker = new SimpleViewsTracker() {
        @Override
        public View getViewForPosition(int position) {
            RecyclePagerAdapter.ViewHolder holder = pagerAdapter.getViewHolder(position);
            return holder == null ? null : PaintingsPagerAdapter.getImage(holder);
        }
    };

    private final SimpleViewsTracker listTracker = new SimpleViewsTracker() {
        @Override
        public View getViewForPosition(int position) {
            int first = views.list.getFirstVisiblePosition();
            int last = views.list.getLastVisiblePosition();
            if (position < first || position > last) {
                return null;
            } else {
                View itemView = views.list.getChildAt(position - first);
                return PaintingsListAdapter.getImage(itemView);
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_list_transition);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        views = new ViewHolder(this);
        settingsMenu = new GestureSettingsMenu();
        settingsMenu.onRestoreInstanceState(savedInstanceState);

        Painting[] paintings = PaintingsHelper.list(getResources());
        views.list.setAdapter(new PaintingsListAdapter(paintings, this));
        pagerAdapter = new PaintingsPagerAdapter(views.pager, paintings, settingsMenu);
        views.pager.setAdapter(pagerAdapter);
        views.pager.setPageMargin(getResources().getDimensionPixelSize(R.dimen.view_pager_margin));

        animator = new ViewsTransitionBuilder<Integer>()
                .fromListView(views.list, listTracker)
                .intoViewPager(views.pager, pagerTracker)
                .build();
        animator.addPositionUpdateListener(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        settingsMenu.onSaveInstanceState(outState);
        super.onSaveInstanceState(outState);
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
    public boolean onCreateOptionsMenu(Menu menu) {
        return settingsMenu.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (settingsMenu.onOptionsItemSelected(item)) {
            invalidateOptionsMenu();
            views.pager.getAdapter().notifyDataSetChanged();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onPaintingClick(int position) {
        animator.enter(position, true);
    }

    @Override
    public void onPositionUpdate(float state, boolean isLeaving) {
        views.background.setVisibility(state == 0f ? View.INVISIBLE : View.VISIBLE);
        views.background.getBackground().setAlpha((int) (255 * state));
    }


    private class ViewHolder {
        final ListView list;
        final ViewPager pager;
        final View background;

        ViewHolder(Activity activity) {
            list = Views.find(activity, R.id.transition_list);
            pager = Views.find(activity, R.id.transition_pager);
            background = Views.find(activity, R.id.transition_full_background);
        }
    }

}
