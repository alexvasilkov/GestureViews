package com.alexvasilkov.gestures.sample.activities;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;

import com.alexvasilkov.android.commons.state.InstanceState;
import com.alexvasilkov.android.commons.utils.Views;
import com.alexvasilkov.events.Events;
import com.alexvasilkov.events.Events.Failure;
import com.alexvasilkov.events.Events.Result;
import com.alexvasilkov.gestures.animation.ViewPositionAnimator;
import com.alexvasilkov.gestures.sample.R;
import com.alexvasilkov.gestures.sample.adapters.FlickrPhotoListAdapter;
import com.alexvasilkov.gestures.sample.adapters.FlickrPhotoPagerAdapter;
import com.alexvasilkov.gestures.sample.logic.FlickrApi;
import com.alexvasilkov.gestures.sample.utils.DecorUtils;
import com.alexvasilkov.gestures.sample.views.PaginatedRecyclerView;
import com.googlecode.flickrjandroid.photos.Photo;

import java.util.List;

public class FlickrListActivity extends BaseActivity
        implements ViewPositionAnimator.PositionUpdateListener, FlickrPhotoListAdapter.OnPhotoListener {

    private static final int PAGE_SIZE = 30;

    private ViewHolder mViews;
    private FlickrPhotoListAdapter mGridAdapter;
    private FlickrPhotoPagerAdapter mPagerAdapter;

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

        setContentView(R.layout.activity_flickr_list);
        mViews = new ViewHolder(this);

        setSupportActionBar(mViews.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        initDecorMargins();
        initGrid();
        initPager();

        if (mPagerPhotoPosition != -1) {
            // Photo was show in pager, we should switch to pager mode instantly
            onPositionUpdate(1f, false);
        }
    }

    @Override
    public void onBackPressed() {
        if (!mPagerAdapter.getListAnimator().isLeaving()) {
            mPagerAdapter.getListAnimator().exit(true);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        mPagerPhotoPosition = mPagerAdapter.getCount() == 0 ? -1 : mViews.pager.getCurrentItem();

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

        super.onSaveInstanceState(outState);
    }

    private void initDecorMargins() {
        // Adjusting margins and paddings to fit translucent decor
        DecorUtils.paddingForStatusBar(mViews.toolbar, true);
        DecorUtils.paddingForStatusBar(mViews.toolbarBack, true);
        DecorUtils.paddingForStatusBar(mViews.pagerToolbar, true);
        DecorUtils.marginForStatusBar(mViews.grid);
        DecorUtils.paddingForNavBar(mViews.grid);
    }

    private void initGrid() {
        // Setting up images grid
        final int cols = getResources().getInteger(R.integer.images_grid_columns);

        mViews.grid.setLayoutManager(new GridLayoutManager(this, cols));
        mViews.grid.setItemAnimator(new DefaultItemAnimator());
        mViews.grid.setLoadingText(getString(R.string.loading_images));
        mViews.grid.setErrorText(getString(R.string.reload_images));
        mViews.grid.setLoadingOffset(PAGE_SIZE / 2);
        mViews.grid.setEndlessListener(new PaginatedRecyclerView.EndlessListener() {
            @Override
            public boolean canLoadNextPage() {
                return mGridAdapter.canLoadNext();
            }

            @Override
            public void onLoadNextPage() {
                // We should either load all items that were loaded before state save / restore,
                // or next page if we already loaded all previously shown items
                int count = Math.max(mPhotoCount, mGridAdapter.getItemCount() + PAGE_SIZE);
                Events.create(FlickrApi.LOAD_IMAGES_EVENT).param(count).post();
            }
        });

        mGridAdapter = new FlickrPhotoListAdapter(this);
        mViews.grid.setAdapter(mGridAdapter);
    }

    private void initPager() {
        // Setting up pager views
        mViews.pager.setPageMargin(getResources().getDimensionPixelSize(R.dimen.view_pager_margin));
        mPagerAdapter = new FlickrPhotoPagerAdapter(mViews.pager, mViews.grid);
        mPagerAdapter.getListAnimator().addPositionUpdateListener(this);

        mViews.pagerToolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
        mViews.pagerToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(@NonNull View v) {
                onBackPressed();
            }
        });
    }

    @Override
    public void onPhotoClick(Photo photo, int position, ImageView image) {
        mPagerAdapter.getListAnimator().enter(position, true);
    }

    @Override
    public void onPositionUpdate(float state, boolean isLeaving) {
        mViews.pagerBackground.setVisibility(state == 0f ? View.INVISIBLE : View.VISIBLE);
        mViews.pagerBackground.getBackground().setAlpha((int) (255 * state));

        mViews.toolbar.setVisibility(state == 1f ? View.INVISIBLE : View.VISIBLE);
        mViews.toolbar.setAlpha((float) Math.sqrt(1d - state)); // Slow down toolbar animation

        mViews.pager.setVisibility(state == 0f && isLeaving ? View.INVISIBLE : View.VISIBLE);

        mViews.pagerToolbar.setVisibility(state == 0f ? View.INVISIBLE : View.VISIBLE);
        mViews.pagerToolbar.setAlpha(state);
    }


    @Result(FlickrApi.LOAD_IMAGES_EVENT)
    private void onPhotosLoaded(List<Photo> photos, boolean hasMore) {
        mPhotoCount = photos.size();

        mGridAdapter.setPhotos(photos, hasMore);
        mPagerAdapter.setPhotos(photos);
        mViews.grid.onNextPageLoaded();

        if (mPagerPhotoPosition != -1) {
            if (mPagerPhotoPosition < mPhotoCount) {
                mPagerAdapter.getListAnimator().enter(mPagerPhotoPosition, false);
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
        mViews.grid.onNextPageFail();

        mPagerPhotoPosition = -1;
        mGridPosition = -1;
        mGridPositionFromTop = 0;
    }


    private class ViewHolder {
        public final Toolbar toolbar;
        public final View toolbarBack;
        public final PaginatedRecyclerView grid;

        public final ViewPager pager;
        public final Toolbar pagerToolbar;
        public final View pagerBackground;

        public ViewHolder(Activity activity) {
            toolbar = Views.find(activity, R.id.toolbar);
            toolbarBack = Views.find(activity, R.id.flickr_toolbar_back);
            grid = Views.find(activity, R.id.flickr_list);

            pager = Views.find(activity, R.id.full_images_pager);
            pagerToolbar = Views.find(activity, R.id.full_image_toolbar);
            pagerBackground = Views.find(activity, R.id.full_image_background);
        }
    }

}
