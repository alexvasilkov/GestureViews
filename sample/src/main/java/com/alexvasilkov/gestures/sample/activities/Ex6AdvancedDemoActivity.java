package com.alexvasilkov.gestures.sample.activities;

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
import android.widget.TextView;

import com.alexvasilkov.android.commons.state.InstanceState;
import com.alexvasilkov.android.commons.texts.SpannableBuilder;
import com.alexvasilkov.android.commons.ui.Views;
import com.alexvasilkov.events.Events;
import com.alexvasilkov.events.Events.Failure;
import com.alexvasilkov.events.Events.Result;
import com.alexvasilkov.gestures.animation.ViewPositionAnimator;
import com.alexvasilkov.gestures.commons.DepthPageTransformer;
import com.alexvasilkov.gestures.commons.RecyclePagerAdapter;
import com.alexvasilkov.gestures.sample.R;
import com.alexvasilkov.gestures.sample.adapters.EndlessRecyclerAdapter;
import com.alexvasilkov.gestures.sample.adapters.PhotoListAdapter;
import com.alexvasilkov.gestures.sample.adapters.PhotoPagerAdapter;
import com.alexvasilkov.gestures.sample.logic.FlickrApi;
import com.alexvasilkov.gestures.sample.utils.DecorUtils;
import com.alexvasilkov.gestures.sample.utils.GestureSettingsMenu;
import com.alexvasilkov.gestures.transition.GestureTransitions;
import com.alexvasilkov.gestures.transition.ViewsTransitionAnimator;
import com.alexvasilkov.gestures.transition.tracker.SimpleTracker;
import com.googlecode.flickrjandroid.photos.Photo;

import java.util.List;

public class Ex6AdvancedDemoActivity extends BaseActivity implements
        ViewPositionAnimator.PositionUpdateListener,
        PhotoListAdapter.OnPhotoListener {

    private static final int PAGE_SIZE = 30;
    private static final int NO_POSITION = -1;

    private ViewHolder views;
    private ViewsTransitionAnimator<Integer> animator;
    private PhotoListAdapter gridAdapter;
    private PhotoPagerAdapter pagerAdapter;
    private ViewPager.OnPageChangeListener pagerListener;
    private GestureSettingsMenu settingsMenu;

    @InstanceState
    private int savedPagerPosition = NO_POSITION;
    @InstanceState
    private int savedGridPosition = NO_POSITION;
    @InstanceState
    private int savedGridPositionFromTop;
    @InstanceState
    private int savedPhotoCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_ex6_advanced_demo);
        views = new ViewHolder(this);

        settingsMenu = new GestureSettingsMenu();
        settingsMenu.onRestoreInstanceState(savedInstanceState);

        setSupportActionBar(views.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        initDecorMargins();
        initGrid();
        initPager();
        initAnimator();

        if (savedPagerPosition != NO_POSITION) {
            // Photo was shown in pager, we should switch to pager mode instantly
            onPositionUpdate(1f, false);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        settingsMenu.onSaveInstanceState(outState);
        saveScreenState();
        super.onSaveInstanceState(outState);
        clearScreenState(); // We don't want to restore state if activity instance is not destroyed
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
            supportInvalidateOptionsMenu();
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
                Photo photo = pagerAdapter.getPhoto(views.pager.getCurrentItem());
                if (photo == null) {
                    return false;
                }
                PhotoCropActivity.show(Ex6AdvancedDemoActivity.this, photo);
                return true;
            default:
                return false;
        }
    }

    private void initDecorMargins() {
        // Adjusting margins and paddings to fit translucent decor
        DecorUtils.paddingForStatusBar(views.toolbar, true);
        DecorUtils.paddingForStatusBar(views.pagerToolbar, true);
        DecorUtils.marginForStatusBar(views.grid);
        DecorUtils.paddingForNavBar(views.grid);
        DecorUtils.marginForNavBar(views.pagerTitle);
    }

    private void initGrid() {
        // Setting up images grid
        final int cols = getResources().getInteger(R.integer.images_grid_columns);

        views.grid.setLayoutManager(new GridLayoutManager(this, cols));
        views.grid.setItemAnimator(new DefaultItemAnimator());

        gridAdapter = new PhotoListAdapter(this);
        gridAdapter.setLoadingOffset(PAGE_SIZE / 2);
        gridAdapter.setCallbacks(new EndlessRecyclerAdapter.LoaderCallbacks() {
            @Override
            public boolean canLoadNextItems() {
                return gridAdapter.canLoadNext();
            }

            @Override
            public void loadNextItems() {
                // We should either load all items that were loaded before state save / restore,
                // or next page if we already loaded all previously shown items
                int count = Math.max(savedPhotoCount, gridAdapter.getCount() + PAGE_SIZE);
                Events.create(FlickrApi.LOAD_IMAGES_EVENT).param(count).post();
            }
        });
        views.grid.setAdapter(gridAdapter);
    }

    private void initPager() {
        // Setting up pager views
        pagerAdapter = new PhotoPagerAdapter(views.pager);
        pagerAdapter.setSetupListener(settingsMenu);

        pagerListener = new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                onPhotoInPagerSelected(position);
            }
        };

        views.pager.setAdapter(pagerAdapter);
        views.pager.addOnPageChangeListener(pagerListener);
        views.pager.setPageTransformer(true, new DepthPageTransformer());

        views.pagerToolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        views.pagerToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(@NonNull View view) {
                onBackPressed();
            }
        });

        onCreateOptionsMenuFullMode(views.pagerToolbar.getMenu());

        views.pagerToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                return onOptionsItemSelectedFullMode(item);
            }
        });
    }

    private void initAnimator() {
        final SimpleTracker gridTracker = new SimpleTracker() {
            @Override
            public View getViewAt(int pos) {
                RecyclerView.ViewHolder holder = views.grid.findViewHolderForLayoutPosition(pos);
                return holder == null ? null : PhotoListAdapter.getImage(holder);
            }
        };

        final SimpleTracker pagerTracker = new SimpleTracker() {
            @Override
            public View getViewAt(int pos) {
                RecyclePagerAdapter.ViewHolder holder = pagerAdapter.getViewHolder(pos);
                return holder == null ? null : PhotoPagerAdapter.getImage(holder);
            }
        };

        animator = GestureTransitions.from(views.grid, gridTracker).into(views.pager, pagerTracker);
        animator.addPositionUpdateListener(this);
    }

    private void onPhotoInPagerSelected(int position) {
        Photo photo = pagerAdapter.getPhoto(position);
        if (photo == null) {
            views.pagerTitle.setText(null);
        } else {
            SpannableBuilder title = new SpannableBuilder(Ex6AdvancedDemoActivity.this);
            title.append(photo.getTitle()).append("\n")
                    .createStyle().setColorResId(R.color.text_secondary_light).apply()
                    .append(R.string.photo_by).append(" ")
                    .append(photo.getOwner().getUsername());
            views.pagerTitle.setText(title.build());
        }
    }

    @Override
    public void onPhotoClick(int position) {
        pagerAdapter.setActivated(true);
        animator.enter(position, true);
    }

    @Override
    public void onPositionUpdate(float position, boolean isLeaving) {
        views.pagerBackground.setVisibility(position == 0f ? View.INVISIBLE : View.VISIBLE);
        views.pagerBackground.getBackground().setAlpha((int) (255 * position));

        views.pagerToolbar.setVisibility(position == 0f ? View.INVISIBLE : View.VISIBLE);
        views.pagerToolbar.setAlpha(position);

        views.pagerTitle.setVisibility(position == 1f ? View.VISIBLE : View.INVISIBLE);

        if (isLeaving && position == 0f) {
            pagerAdapter.setActivated(false);
        }
    }

    private void saveScreenState() {
        clearScreenState();

        savedPhotoCount = gridAdapter.getCount();

        savedPagerPosition = animator.isLeaving() || pagerAdapter.getCount() == 0
                ? NO_POSITION : views.pager.getCurrentItem();

        if (views.grid.getChildCount() > 0) {
            View child = views.grid.getChildAt(0);
            savedGridPosition = views.grid.getChildAdapterPosition(child);
            savedGridPositionFromTop = child.getTop()
                    - Views.getMarginParams(child).topMargin
                    - views.grid.getPaddingTop();
        }
    }

    private void clearScreenState() {
        savedPhotoCount = 0;
        savedPagerPosition = NO_POSITION;
        savedGridPosition = NO_POSITION;
        savedGridPositionFromTop = 0;
    }


    @Result(FlickrApi.LOAD_IMAGES_EVENT)
    private void onPhotosLoaded(List<Photo> photos, boolean hasMore) {
        gridAdapter.setPhotos(photos, hasMore);
        pagerAdapter.setPhotos(photos);
        gridAdapter.onNextItemsLoaded();

        // Ensure listener called for 0 position
        pagerListener.onPageSelected(views.pager.getCurrentItem());

        // Restoring saved state
        if (savedPagerPosition != NO_POSITION && savedPagerPosition < photos.size()) {
            pagerAdapter.setActivated(true);
            animator.enter(savedPagerPosition, false);
        }

        if (savedGridPosition != NO_POSITION && savedGridPosition < photos.size()) {
            ((GridLayoutManager) views.grid.getLayoutManager())
                    .scrollToPositionWithOffset(savedGridPosition, savedGridPositionFromTop);
        }

        clearScreenState();
    }

    @Failure(FlickrApi.LOAD_IMAGES_EVENT)
    private void onPhotosLoadFail() {
        gridAdapter.onNextItemsError();

        // Skipping state restoration
        if (savedPagerPosition != NO_POSITION) {
            // We can't show image right now, so we should return back to list
            onPositionUpdate(0f, true);
        }

        clearScreenState();
    }


    private class ViewHolder {
        final Toolbar toolbar;
        final RecyclerView grid;

        final ViewPager pager;
        final Toolbar pagerToolbar;
        final TextView pagerTitle;
        final View pagerBackground;

        ViewHolder(Activity activity) {
            toolbar = Views.find(activity, R.id.toolbar);
            grid = Views.find(activity, R.id.advanced_grid);

            pager = Views.find(activity, R.id.advanced_pager);
            pagerToolbar = Views.find(activity, R.id.advanced_full_toolbar);
            pagerTitle = Views.find(activity, R.id.advanced_full_title);
            pagerBackground = Views.find(activity, R.id.advanced_full_background);
        }
    }

}
