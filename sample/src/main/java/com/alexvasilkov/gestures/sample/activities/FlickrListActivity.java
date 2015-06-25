package com.alexvasilkov.gestures.sample.activities;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;

import com.alexvasilkov.android.commons.utils.Views;
import com.alexvasilkov.events.Events;
import com.alexvasilkov.events.Events.Failure;
import com.alexvasilkov.events.Events.Result;
import com.alexvasilkov.gestures.sample.R;
import com.alexvasilkov.gestures.sample.items.FlickrListAdapter;
import com.alexvasilkov.gestures.sample.logic.FlickrApi;
import com.alexvasilkov.gestures.sample.utils.DecorUtils;
import com.alexvasilkov.gestures.sample.utils.glide.GlideHelper;
import com.alexvasilkov.gestures.sample.views.PaginatedRecyclerView;
import com.alexvasilkov.gestures.views.GestureImageViewFull;
import com.googlecode.flickrjandroid.photos.Photo;
import com.googlecode.flickrjandroid.photos.PhotoList;

public class FlickrListActivity extends BaseActivity implements GestureImageViewFull.OnImageStateChangeListener,
        FlickrListAdapter.OnPhotoListener {

    private ViewHolder mViews;
    private FlickrListAdapter mAdapter;
    private boolean mFullGesturesDisabled;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_flickr_list);
        mViews = new ViewHolder(this);

        setSupportActionBar(mViews.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Adjusting margins and paddings to fit translucent decor
        DecorUtils.paddingForStatusBar(mViews.toolbar, true);
        DecorUtils.paddingForStatusBar(mViews.toolbarBack, true);
        DecorUtils.paddingForStatusBar(mViews.fullToolbar, true);
        DecorUtils.marginForStatusBar(mViews.images);
        DecorUtils.paddingForNavBar(mViews.images);

        // Setting up images list
        int cols = getResources().getInteger(R.integer.images_grid_columns);

        mViews.images.setLayoutManager(new GridLayoutManager(this, cols));
        mViews.images.setItemAnimator(new DefaultItemAnimator());
        mViews.images.setLoadingText(getString(R.string.loading_images));
        mViews.images.setErrorText(getString(R.string.reload_images));

        mViews.images.setEndlessListener(new PaginatedRecyclerView.EndlessListener() {
            @Override
            public boolean canLoadNextPage() {
                return mAdapter.canLoadNext();
            }

            @Override
            public void onLoadNextPage() {
                Events.create(FlickrApi.LOAD_IMAGES_EVENT)
                        .param(mAdapter.getNextPageIndex(), PaginatedRecyclerView.PAGE_SIZE)
                        .post();
            }
        });

        mAdapter = new FlickrListAdapter(this);
        mViews.images.setAdapter(mAdapter);

        // Settings up full image view
        mViews.fullImage.getController().getSettings().setFillViewport(true).setMaxZoom(3f);
        mViews.fullImage.setOnImageStateChangeListener(this);

        mViews.fullToolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
        mViews.fullToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(@NonNull View v) {
                onBackPressed();
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (mViews.fullImage.isOpen()) {
            mViews.fullImage.exit(true);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onPhotoClick(Photo photo, ImageView image) {
        // Temporary disabling touch controls
        if (!mFullGesturesDisabled) {
            mViews.fullImage.getController().getSettings().disableGestures();
            mFullGesturesDisabled = true;
        }

        mViews.fullImage.enter(image, true);
        mViews.fullImage.setImageDrawable(image.getDrawable()); // For smoother animation
        mAdapter.listenForViewUpdates(photo);

        // Loading image (note)
        GlideHelper.loadFlickrFull(photo, mViews.fullImage, mViews.fullProgress,
                new GlideHelper.OnImageLoadedListener() {
                    @Override
                    public void onImageLoaded() {
                        // Re-enabling touch controls
                        mViews.fullImage.getController().getSettings().enableGestures();
                        mFullGesturesDisabled = false;
                    }
                });
    }

    @Override
    public void onPhotoViewChanged(Photo photo, ImageView image) {
        mViews.fullImage.update(image);
    }

    @Override
    public void onImageStateChanged(float state, boolean isFinishing) {
        mViews.fullBackground.getBackground().setAlpha((int) (255 * state));
        mViews.fullBackground.setVisibility(state == 0f ? View.INVISIBLE : View.VISIBLE);

        mViews.toolbar.setAlpha((float) Math.sqrt(1d - state)); // Slow down toolbar animation
        mViews.toolbar.setVisibility(state == 1f ? View.INVISIBLE : View.VISIBLE);

        mViews.fullToolbar.setAlpha(state);
        mViews.fullToolbar.setVisibility(state == 0f ? View.INVISIBLE : View.VISIBLE);

        mViews.fullProgress.setVisibility(state == 1f ? View.VISIBLE : View.INVISIBLE);

        if (state == 0f && isFinishing) mAdapter.listenForViewUpdates(null);
    }


    @Result(FlickrApi.LOAD_IMAGES_EVENT)
    private void onPhotosLoaded(PhotoList page) {
        mAdapter.setNextPage(page);
        mViews.images.onNextPageLoaded();
    }

    @Failure(FlickrApi.LOAD_IMAGES_EVENT)
    private void onPhotosLoadFail() {
        mViews.images.onNextPageFail();
    }


    private class ViewHolder {
        final Toolbar toolbar;
        final View toolbarBack;
        final PaginatedRecyclerView images;

        final Toolbar fullToolbar;
        final View fullBackground;
        final GestureImageViewFull fullImage;
        final View fullProgress;

        public ViewHolder(Activity activity) {
            toolbar = Views.find(activity, R.id.toolbar);
            toolbarBack = Views.find(activity, R.id.flickr_toolbar_back);
            images = Views.find(activity, R.id.flickr_list);

            fullToolbar = Views.find(activity, R.id.full_image_toolbar);
            fullBackground = Views.find(activity, R.id.full_image_background);
            fullImage = Views.find(activity, R.id.full_image);
            fullProgress = Views.find(activity, R.id.full_image_progress);
        }
    }

}
