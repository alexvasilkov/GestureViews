package com.alexvasilkov.gestures.sample.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.alexvasilkov.android.commons.state.InstanceState;
import com.alexvasilkov.android.commons.texts.SpannableBuilder;
import com.alexvasilkov.android.commons.utils.Views;
import com.alexvasilkov.events.Events;
import com.alexvasilkov.events.Events.Failure;
import com.alexvasilkov.events.Events.Result;
import com.alexvasilkov.gestures.animation.ViewPositionAnimator;
import com.alexvasilkov.gestures.sample.R;
import com.alexvasilkov.gestures.sample.adapters.EndlessRecyclerAdapter;
import com.alexvasilkov.gestures.sample.adapters.FlickrPhotoListAdapter;
import com.alexvasilkov.gestures.sample.adapters.FlickrPhotoPagerAdapter;
import com.alexvasilkov.gestures.sample.logic.FlickrApi;
import com.alexvasilkov.gestures.sample.utils.DecorUtils;
import com.alexvasilkov.gestures.sample.utils.GestureSettingsMenu;
import com.alexvasilkov.gestures.sample.views.DepthPageTransformer;
import com.alexvasilkov.gestures.transition.SimpleViewsTracker;
import com.alexvasilkov.gestures.transition.ViewsCoordinator;
import com.alexvasilkov.gestures.transition.ViewsTransitionAnimator;
import com.alexvasilkov.gestures.transition.ViewsTransitionBuilder;
import com.alexvasilkov.gestures.views.utils.RecyclePagerAdapter;
import com.googlecode.flickrjandroid.photos.Photo;

import java.util.List;

public class AdvancedDemoActivity extends BaseActivity implements
        ViewPositionAnimator.PositionUpdateListener,
        FlickrPhotoListAdapter.OnPhotoListener {

    private static final int PAGE_SIZE = 30;

    private ViewHolder mViews;
    private ViewsTransitionAnimator<Integer> mAnimator;
    private FlickrPhotoListAdapter mGridAdapter;
    private FlickrPhotoPagerAdapter mPagerAdapter;
    private ViewPager.OnPageChangeListener mPagerListener;
    private GestureSettingsMenu mSettingsMenu;

    @InstanceState
    private int mPagerPhotoPosition = -1;
    @InstanceState
    private int mGridPosition = -1;
    @InstanceState
    private int mGridPositionFromTop;
    @InstanceState
    private int mPhotoCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_advanced_demo);
        mViews = new ViewHolder(this);

        mSettingsMenu = new GestureSettingsMenu();
        mSettingsMenu.onRestoreInstanceState(savedInstanceState);

        setSupportActionBar(mViews.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        initDecorMargins();
        initGrid();
        initPager();
        initAnimator();

        if (mPagerPhotoPosition != -1) {
            // Photo was show in pager, we should switch to pager mode instantly
            onPositionUpdate(1f, false);
        }
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
    protected void onSaveInstanceState(Bundle outState) {
        mPagerPhotoPosition = mAnimator.isLeaving() || mPagerAdapter.getCount() == 0
                ? -1 : mViews.pager.getCurrentItem();

        if (mViews.grid.getChildCount() > 0) {
            View child = mViews.grid.getChildAt(0);
            mGridPosition = mViews.grid.getChildAdapterPosition(child);
            mGridPositionFromTop = child.getTop()
                    - Views.getMarginParams(child).topMargin
                    - mViews.grid.getPaddingTop();
        } else {
            mGridPosition = -1;
            mGridPositionFromTop = 0;
        }

        mSettingsMenu.onSaveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return mSettingsMenu.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mSettingsMenu.onOptionsItemSelected(item)) {
            invalidateOptionsMenu();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void onCreateOptionsMenuFullMode(Menu menu) {
        MenuItem crop = menu.add(Menu.NONE, R.id.menu_crop, 0, R.string.button_crop);
        crop.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        crop.setIcon(R.drawable.ic_crop_white_24dp);
    }

    private boolean onOptionsItemSelectedFullMode(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_crop:
                Photo photo = mPagerAdapter.getPhoto(mViews.pager.getCurrentItem());
                if (photo == null) return false;
                PhotoCropActivity.show(AdvancedDemoActivity.this, photo);
                return true;
            default:
                return false;
        }
    }

    private void initDecorMargins() {
        // Adjusting margins and paddings to fit translucent decor
        DecorUtils.paddingForStatusBar(mViews.toolbar, true);
        DecorUtils.paddingForStatusBar(mViews.toolbarBack, true);
        DecorUtils.paddingForStatusBar(mViews.pagerToolbar, true);
        DecorUtils.marginForStatusBar(mViews.grid);
        DecorUtils.paddingForNavBar(mViews.grid);
        DecorUtils.marginForNavBar(mViews.pagerTitle);
    }

    private void initGrid() {
        // Setting up images grid
        final int cols = getResources().getInteger(R.integer.images_grid_columns);

        mViews.grid.setLayoutManager(new GridLayoutManager(this, cols));
        mViews.grid.setItemAnimator(new DefaultItemAnimator());

        mGridAdapter = new FlickrPhotoListAdapter(this);
        mGridAdapter.setLoadingOffset(PAGE_SIZE / 2);
        mGridAdapter.setCallbacks(new EndlessRecyclerAdapter.LoaderCallbacks() {
            @Override
            public boolean canLoadNextItems() {
                return mGridAdapter.canLoadNext();
            }

            @Override
            public void loadNextItems() {
                // We should either load all items that were loaded before state save / restore,
                // or next page if we already loaded all previously shown items
                int count = Math.max(mPhotoCount, mGridAdapter.getCount() + PAGE_SIZE);
                Events.create(FlickrApi.LOAD_IMAGES_EVENT).param(count).post();
            }
        });
        mViews.grid.setAdapter(mGridAdapter);
    }

    @SuppressLint("PrivateResource")
    private void initPager() {
        // Setting up pager views
        mPagerAdapter = new FlickrPhotoPagerAdapter(mViews.pager);
        mPagerAdapter.setSetupListener(mSettingsMenu);

        mPagerListener = new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                onPhotoInPagerSelected(position);
            }
        };

        mViews.pager.setAdapter(mPagerAdapter);
        mViews.pager.addOnPageChangeListener(mPagerListener);
        mViews.pager.setPageTransformer(true, new DepthPageTransformer());

        mViews.pagerToolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
        mViews.pagerToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(@NonNull View v) {
                onBackPressed();
            }
        });

        onCreateOptionsMenuFullMode(mViews.pagerToolbar.getMenu());

        mViews.pagerToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                return onOptionsItemSelectedFullMode(item);
            }
        });
    }

    private void initAnimator() {
        mAnimator = new ViewsTransitionBuilder<Integer>()
                .fromRecyclerView(mViews.grid, new SimpleViewsTracker() {
                    @Override
                    public View getViewForPosition(int position) {
                        RecyclerView.ViewHolder holder =
                                mViews.grid.findViewHolderForLayoutPosition(position);
                        return holder == null ? null : FlickrPhotoListAdapter.getImage(holder);
                    }
                })
                .intoViewPager(mViews.pager, new SimpleViewsTracker() {
                    @Override
                    public View getViewForPosition(int position) {
                        RecyclePagerAdapter.ViewHolder holder = mPagerAdapter.getViewHolder(position);
                        return holder == null ? null : FlickrPhotoPagerAdapter.getImage(holder);
                    }
                })
                .build();
        mAnimator.addPositionUpdateListener(this);
        mAnimator.setReadyListener(new ViewsCoordinator.OnViewsReadyListener<Integer>() {
            @Override
            public void onViewsReady(@NonNull Integer id) {
                // Setting image drawable from 'from' view to 'to' to prevent flickering
                ImageView from = (ImageView) mAnimator.getFromView();
                ImageView to = (ImageView) mAnimator.getToView();
                if (to.getDrawable() == null) to.setImageDrawable(from.getDrawable());
            }
        });
    }

    private void onPhotoInPagerSelected(int position) {
        Photo photo = mPagerAdapter.getPhoto(position);
        if (photo == null) {
            mViews.pagerTitle.setText(null);
        } else {
            SpannableBuilder title = new SpannableBuilder(AdvancedDemoActivity.this);
            title.append(photo.getTitle()).append("\n")
                    .createStyle().setColorResId(R.color.text_secondary_light).apply()
                    .append(R.string.photo_by).append(" ")
                    .append(photo.getOwner().getUsername());
            mViews.pagerTitle.setText(title.build());
        }
    }

    @Override
    public void onPhotoClick(Photo photo, int position, ImageView image) {
        mPagerAdapter.setActivated(true);
        mAnimator.enter(position, true);
    }

    @Override
    public void onPositionUpdate(float state, boolean isLeaving) {
        mViews.pagerBackground.setVisibility(state == 0f ? View.INVISIBLE : View.VISIBLE);
        mViews.pagerBackground.getBackground().setAlpha((int) (255 * state));

        mViews.toolbar.setVisibility(state == 1f ? View.INVISIBLE : View.VISIBLE);
        mViews.toolbar.setAlpha((float) Math.sqrt(1d - state)); // Slow down toolbar animation

        mViews.pagerToolbar.setVisibility(state == 0f ? View.INVISIBLE : View.VISIBLE);
        mViews.pagerToolbar.setAlpha(state);

        mViews.pagerTitle.setVisibility(state == 1f ? View.VISIBLE : View.INVISIBLE);

        if (isLeaving && state == 0f) mPagerAdapter.setActivated(false);
    }


    @Result(FlickrApi.LOAD_IMAGES_EVENT)
    private void onPhotosLoaded(List<Photo> photos, boolean hasMore) {
        mPhotoCount = photos.size();
        mGridAdapter.setPhotos(photos, hasMore);
        mPagerAdapter.setPhotos(photos);
        mGridAdapter.onNextItemsLoaded();

        // Ensure listener called for 0 position
        mPagerListener.onPageSelected(mViews.pager.getCurrentItem());

        // Restoring saved state
        if (mPagerPhotoPosition != -1) {
            if (mPagerPhotoPosition < mPhotoCount) {
                mPagerAdapter.setActivated(true);
                mAnimator.enter(mPagerPhotoPosition, false);
            }
            mPagerPhotoPosition = -1;
        }

        if (mGridPosition != -1) {
            if (mGridPosition < mPhotoCount) {
                ((GridLayoutManager) mViews.grid.getLayoutManager())
                        .scrollToPositionWithOffset(mGridPosition, mGridPositionFromTop);
            }
            mGridPosition = -1;
            mGridPositionFromTop = 0;
        }
    }

    @Failure(FlickrApi.LOAD_IMAGES_EVENT)
    private void onPhotosLoadFail() {
        mGridAdapter.onNextItemsError();

        // Skipping state restoration
        if (mPagerPhotoPosition != -1) {
            // We can't show image right now, so we should return back to list
            onPositionUpdate(0f, true);
        }

        mPagerPhotoPosition = -1;
        mGridPosition = -1;
        mGridPositionFromTop = 0;
    }


    private class ViewHolder {
        public final Toolbar toolbar;
        public final View toolbarBack;
        public final RecyclerView grid;

        public final ViewPager pager;
        public final Toolbar pagerToolbar;
        public final TextView pagerTitle;
        public final View pagerBackground;

        public ViewHolder(Activity activity) {
            toolbar = Views.find(activity, R.id.toolbar);
            toolbarBack = Views.find(activity, R.id.flickr_toolbar_back);
            grid = Views.find(activity, R.id.flickr_list);

            pager = Views.find(activity, R.id.flickr_pager);
            pagerToolbar = Views.find(activity, R.id.flickr_full_toolbar);
            pagerTitle = Views.find(activity, R.id.flickr_full_title);
            pagerBackground = Views.find(activity, R.id.flickr_full_background);
        }
    }

}
