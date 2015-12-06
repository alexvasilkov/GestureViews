package com.alexvasilkov.gestures.sample.activities;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;

import com.alexvasilkov.android.commons.utils.Views;
import com.alexvasilkov.gestures.animation.ViewPositionAnimator;
import com.alexvasilkov.gestures.sample.R;
import com.alexvasilkov.gestures.sample.adapters.PaintingsListAdapter;
import com.alexvasilkov.gestures.sample.adapters.PaintingsPagerAdapter;
import com.alexvasilkov.gestures.sample.logic.Painting;
import com.alexvasilkov.gestures.sample.utils.GestureSettingsMenu;
import com.alexvasilkov.gestures.transition.SimpleViewsTracker;
import com.alexvasilkov.gestures.transition.ViewsTransitionAnimator;
import com.alexvasilkov.gestures.transition.ViewsTransitionBuilder;
import com.alexvasilkov.gestures.views.utils.RecyclePagerAdapter;

public class ListTransitionActivity extends BaseActivity implements
        PaintingsListAdapter.OnPaintingListener,
        ViewPositionAnimator.PositionUpdateListener {

    private ViewHolder mViews;
    private GestureSettingsMenu mSettingsMenu;
    private PaintingsPagerAdapter mPagerAdapter;
    private ViewsTransitionAnimator<Integer> mAnimator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_list_transition);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mViews = new ViewHolder(this);
        mSettingsMenu = new GestureSettingsMenu();
        mSettingsMenu.onRestoreInstanceState(savedInstanceState);

        Painting[] paintings = Painting.getAllPaintings(getResources());
        mViews.list.setAdapter(new PaintingsListAdapter(paintings, this));
        mPagerAdapter = new PaintingsPagerAdapter(mViews.pager, paintings, mSettingsMenu);
        mViews.pager.setAdapter(mPagerAdapter);
        mViews.pager.setPageMargin(getResources().getDimensionPixelSize(R.dimen.view_pager_margin));

        mAnimator = new ViewsTransitionBuilder<Integer>()
                .fromListView(mViews.list, mListTracker)
                .intoViewPager(mViews.pager, mPagerTracker)
                .build();
        mAnimator.addPositionUpdateListener(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        mSettingsMenu.onSaveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed() {
        if (!mAnimator.isLeaving()) {
            mAnimator.exit(true);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return mSettingsMenu.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mSettingsMenu.onOptionsItemSelected(item)) {
            invalidateOptionsMenu();
            mViews.pager.getAdapter().notifyDataSetChanged();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onPaintingClick(Painting painting, int position, ImageView image) {
        mAnimator.enter(position, true);
    }

    @Override
    public void onPositionUpdate(float state, boolean isLeaving) {
        mViews.background.setVisibility(state == 0f ? View.INVISIBLE : View.VISIBLE);
        mViews.background.getBackground().setAlpha((int) (255 * state));
    }


    private final SimpleViewsTracker mPagerTracker = new SimpleViewsTracker() {
        @Override
        public View getViewForPosition(int position) {
            RecyclePagerAdapter.ViewHolder holder = mPagerAdapter.getViewHolder(position);
            return holder == null ? null : PaintingsPagerAdapter.getImage(holder);
        }
    };

    private final SimpleViewsTracker mListTracker = new SimpleViewsTracker() {
        @Override
        public View getViewForPosition(int position) {
            int first = mViews.list.getFirstVisiblePosition();
            int last = mViews.list.getLastVisiblePosition();
            if (position < first || position > last) {
                return null;
            } else {
                View itemView = mViews.list.getChildAt(position - first);
                return PaintingsListAdapter.getImage(itemView);
            }
        }
    };


    private class ViewHolder {
        public final ListView list;
        public final ViewPager pager;
        public final View background;

        public ViewHolder(Activity activity) {
            list = Views.find(activity, R.id.transition_list);
            pager = Views.find(activity, R.id.transition_pager);
            background = Views.find(activity, R.id.transition_full_background);
        }
    }

}
