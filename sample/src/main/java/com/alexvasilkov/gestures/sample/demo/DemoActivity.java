package com.alexvasilkov.gestures.sample.demo;

import android.animation.ObjectAnimator;
import android.animation.StateListAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.alexvasilkov.android.commons.state.InstanceState;
import com.alexvasilkov.android.commons.texts.SpannableBuilder;
import com.alexvasilkov.android.commons.ui.Views;
import com.alexvasilkov.events.Events;
import com.alexvasilkov.events.Events.Failure;
import com.alexvasilkov.events.Events.Result;
import com.alexvasilkov.gestures.commons.DepthPageTransformer;
import com.alexvasilkov.gestures.sample.R;
import com.alexvasilkov.gestures.sample.base.BaseSettingsActivity;
import com.alexvasilkov.gestures.sample.demo.adapter.EndlessRecyclerAdapter;
import com.alexvasilkov.gestures.sample.demo.adapter.PhotoListAdapter;
import com.alexvasilkov.gestures.sample.demo.adapter.PhotoPagerAdapter;
import com.alexvasilkov.gestures.sample.demo.utils.DecorUtils;
import com.alexvasilkov.gestures.sample.demo.utils.FlickrApi;
import com.alexvasilkov.gestures.transition.GestureTransitions;
import com.alexvasilkov.gestures.transition.ViewsTransitionAnimator;
import com.alexvasilkov.gestures.transition.tracker.SimpleTracker;
import com.alexvasilkov.gestures.views.GestureImageView;
import com.googlecode.flickrjandroid.photos.Photo;

import java.util.List;
import java.util.Objects;

/**
 * Advanced usage example that demonstrates images animation from RecyclerView into ViewPager.
 * <p>
 * For particular use cases see standalone examples.
 */
public class DemoActivity extends BaseSettingsActivity implements PhotoListAdapter.OnPhotoListener {

    private static final int PAGE_SIZE = 30;
    private static final int NO_POSITION = -1;

    private ViewHolder views;
    private ViewsTransitionAnimator<?> imageAnimator;
    private ViewsTransitionAnimator<Integer> listAnimator;
    private PhotoListAdapter gridAdapter;
    private PhotoPagerAdapter pagerAdapter;
    private ViewPager2.OnPageChangeCallback pagerListener;

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

        setContentView(R.layout.demo_screen);

        views = new ViewHolder(this);

        setSupportActionBar(views.toolbar);
        getSupportActionBarNotNull().setDisplayHomeAsUpEnabled(true);
        getSupportActionBarNotNull().setDisplayShowTitleEnabled(false);

        setAppBarStateListAnimator(views.appBar);

        initDecorMargins();
        initTopImage();
        initGrid();
        initPager();
        initPagerAnimator();

        if (savedPagerPosition != NO_POSITION) {
            // A photo was shown in the pager, we should switch to pager mode instantly
            applyFullPagerState(1f, false);
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        saveScreenState();
        super.onSaveInstanceState(outState);
        clearScreenState(); // We don't want to restore state if activity instance is not destroyed
    }

    @Override
    public void onBackPressed() {
        if (!listAnimator.isLeaving()) {
            listAnimator.exit(true); // Exiting from full pager
        } else if (!imageAnimator.isLeaving()) {
            imageAnimator.exit(true); // Exiting from full top image
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onSettingsChanged() {
        // Nothing to do, gesture settings can only be changed in RecyclerView mode
    }

    /**
     * Adjusting margins and paddings to fit translucent decor.
     */
    private void initDecorMargins() {
        DecorUtils.size(views.appBar, Gravity.TOP);
        DecorUtils.padding(views.toolbar, Gravity.TOP);
        DecorUtils.padding(views.pagerToolbar, Gravity.TOP);
        DecorUtils.padding(views.fullImageToolbar, Gravity.TOP);
        DecorUtils.padding(views.grid, Gravity.BOTTOM);
        DecorUtils.margin(views.pagerTitle, Gravity.BOTTOM);
    }

    /**
     * Initializing top image expanding animation.
     */
    private void initTopImage() {
        views.fullImageToolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        views.fullImageToolbar.setNavigationOnClickListener(view -> onBackPressed());

        imageAnimator = GestureTransitions.from(views.appBarImage).into(views.fullImage);

        // Setting up and animating image transition
        imageAnimator.addPositionUpdateListener(this::applyFullImageState);

        views.appBarImage.setOnClickListener(view -> {
            getSettingsController().apply(views.fullImage);
            imageAnimator.enterSingle(true);
        });
    }

    /**
     * Applying top image animation state: fading out toolbar and background.
     */
    private void applyFullImageState(float position, boolean isLeaving) {
        views.fullBackground.setVisibility(position == 0f ? View.INVISIBLE : View.VISIBLE);
        views.fullBackground.setAlpha(position);

        views.fullImageToolbar.setVisibility(position == 0f ? View.INVISIBLE : View.VISIBLE);
        views.fullImageToolbar.setAlpha(position);

        views.fullImage.setVisibility(position == 0f && isLeaving
                ? View.INVISIBLE : View.VISIBLE);
    }

    /**
     * Initializing grid view (RecyclerView) and endless loading.
     */
    private void initGrid() {
        // Setting up images grid
        final int cols = getResources().getInteger(R.integer.images_grid_columns);

        views.grid.setLayoutManager(new GridLayoutManager(this, cols));

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

    /**
     * Initializing pager and fullscreen mode.
     */
    private void initPager() {
        // Setting up pager adapter
        pagerAdapter = new PhotoPagerAdapter(getSettingsController());

        pagerListener = new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                onPhotoInPagerSelected(position);
            }
        };

        views.pager.setAdapter(pagerAdapter);
        views.pager.registerOnPageChangeCallback(pagerListener);
        views.pager.setPageTransformer(new DepthPageTransformer());

        // Setting up pager toolbar
        views.pagerToolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        views.pagerToolbar.setNavigationOnClickListener(view -> onBackPressed());

        // Enabling immersive mode by clicking on full screen image
        pagerAdapter.setImageClickListener(() -> {
            if (!listAnimator.isLeaving()) {
                // Toggle immersive mode
                showSystemUi(!isSystemUiShown());
            }
        });

        DecorUtils.onInsetsChanged(views.pagerToolbar, () -> {
            float alpha = isSystemUiShown() ? 1f : 0f;
            views.pagerToolbar.animate().alpha(alpha);
            views.pagerTitle.animate().alpha(alpha);
        });
    }

    /**
     * Setting up photo title for current pager position.
     */
    private void onPhotoInPagerSelected(int position) {
        Photo photo = pagerAdapter.getPhoto(position);
        if (photo == null) {
            views.pagerTitle.setText(null);
        } else {
            SpannableBuilder title = new SpannableBuilder(DemoActivity.this);

            title.append(photo.getTitle()).append("\n")
                    .createStyle().setColorResId(R.color.demo_photo_subtitle).apply()
                    .append(R.string.demo_photo_by).append(" ")
                    .append(photo.getOwner().getUsername());
            views.pagerTitle.setText(title.build());
        }
    }

    /**
     * Initializing grid-to-pager animation.
     */
    private void initPagerAnimator() {
        final SimpleTracker gridTracker = new SimpleTracker() {
            @Override
            public View getViewAt(int pos) {
                return gridAdapter.getImage(pos);
            }
        };

        final SimpleTracker pagerTracker = new SimpleTracker() {
            @Override
            public View getViewAt(int pos) {
                return pagerAdapter.getImage(pos);
            }
        };

        listAnimator = GestureTransitions
                .from(views.grid, gridTracker)
                .into(views.pager, pagerTracker);

        // Setting up and animating image transition
        listAnimator.addPositionUpdateListener(this::applyFullPagerState);
    }

    /**
     * Applying pager image animation state: fading out toolbar, title and background.
     */
    private void applyFullPagerState(float position, boolean isLeaving) {
        views.fullBackground.setVisibility(position == 0f ? View.INVISIBLE : View.VISIBLE);
        views.fullBackground.setAlpha(position);

        views.pagerToolbar.setVisibility(position == 0f ? View.INVISIBLE : View.VISIBLE);
        views.pagerToolbar.setAlpha(isSystemUiShown() ? position : 0f);

        views.pagerTitle.setVisibility(position == 1f ? View.VISIBLE : View.INVISIBLE);

        if (isLeaving && position == 0f) {
            pagerAdapter.setActivated(false);
            showSystemUi(true);
        }
    }

    /**
     * Checks if system UI (status bar and navigation bar) is shown or we are in fullscreen mode.
     */
    @SuppressWarnings("deprecation") // New insets controller API is not back-ported yet
    private boolean isSystemUiShown() {
        return (getWindow().getDecorView().getSystemUiVisibility()
                & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0;
    }

    /**
     * Shows or hides system UI (status bar and navigation bar).
     */
    @SuppressWarnings("deprecation") // New insets controller API is not back-ported yet
    private void showSystemUi(boolean show) {
        int flags = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE;

        getWindow().getDecorView().setSystemUiVisibility(show ? 0 : flags);
    }


    @Override
    public void onPhotoClick(int position) {
        pagerAdapter.setActivated(true);
        listAnimator.enter(position, true);
    }

    /**
     * Saves current screen state: whether we are in a full image mode or not, as well as current
     * grid position. So that we can restore UI on configuration change.
     */
    private void saveScreenState() {
        clearScreenState();

        savedPhotoCount = gridAdapter.getCount();

        savedPagerPosition = listAnimator.isLeaving() || pagerAdapter.getItemCount() == 0
                ? NO_POSITION : views.pager.getCurrentItem();

        if (views.grid.getChildCount() > 0) {
            View child = views.grid.getChildAt(0);
            savedGridPosition = views.grid.getChildAdapterPosition(child);
            savedGridPositionFromTop = child.getTop()
                    - Views.getMarginParams(child).topMargin
                    - views.grid.getPaddingTop();
        }
    }

    /**
     * Cleans up saved screen state.
     */
    private void clearScreenState() {
        savedPhotoCount = 0;
        savedPagerPosition = NO_POSITION;
        savedGridPosition = NO_POSITION;
        savedGridPositionFromTop = 0;
    }


    /**
     * Photos loading results callback.
     */
    @Result(FlickrApi.LOAD_IMAGES_EVENT)
    private void onPhotosLoaded(List<Photo> photos, boolean hasMore) {
        // RecyclerView will continue scrolling when new items are added, we need to stop it.
        // Seems like this buggy behavior was introduced in support library v26.0.x
        final boolean onBottom =
                views.grid.findViewHolderForAdapterPosition(gridAdapter.getCount() - 1) != null;
        if (onBottom) {
            views.grid.stopScroll();
        }

        // Setting new photos list
        gridAdapter.setPhotos(photos, hasMore);
        pagerAdapter.setPhotos(photos);
        gridAdapter.onNextItemsLoaded();

        // Ensure listener called for 0 position
        pagerListener.onPageSelected(views.pager.getCurrentItem());

        // Restoring saved state
        if (savedPagerPosition != NO_POSITION && savedPagerPosition < photos.size()) {
            pagerAdapter.setActivated(true);
            listAnimator.enter(savedPagerPosition, false);
        }

        if (savedGridPosition != NO_POSITION && savedGridPosition < photos.size()) {
            GridLayoutManager manager =
                    (GridLayoutManager) Objects.requireNonNull(views.grid.getLayoutManager());
            manager.scrollToPositionWithOffset(savedGridPosition, savedGridPositionFromTop);
        }

        clearScreenState();
    }

    /**
     * Photos loading failure callback.
     */
    @Failure(FlickrApi.LOAD_IMAGES_EVENT)
    private void onPhotosLoadFail() {
        gridAdapter.onNextItemsError();

        // Skipping state restoration
        if (savedPagerPosition != NO_POSITION) {
            // We can't show image right now, so we should return back to list
            applyFullPagerState(0f, true);
        }

        clearScreenState();
    }


    @SuppressLint("Recycle")
    private static void setAppBarStateListAnimator(@NonNull View view) {
        // App bar elevation animation does not work unless we set state animator ourselves
        final StateListAnimator sla = new StateListAnimator();
        final int[] notLifted = new int[] { -R.attr.state_lifted };
        final int[] lifted = new int[0];
        final long dur = 150L;
        final float elevation = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 8f, view.getResources().getDisplayMetrics());

        sla.addState(notLifted, ObjectAnimator.ofFloat(view, "elevation", 0f).setDuration(dur));
        sla.addState(lifted, ObjectAnimator.ofFloat(view, "elevation", elevation).setDuration(dur));

        view.setStateListAnimator(sla);
    }


    /**
     * Utility class to hold all views in a single place.
     */
    private static class ViewHolder {
        final Toolbar toolbar;
        final View appBar;
        final ImageView appBarImage;
        final RecyclerView grid;

        final View fullBackground;
        final ViewPager2 pager;
        final TextView pagerTitle;
        final Toolbar pagerToolbar;
        final GestureImageView fullImage;
        final Toolbar fullImageToolbar;

        ViewHolder(Activity activity) {
            toolbar = activity.findViewById(R.id.toolbar);
            appBar = activity.findViewById(R.id.demo_app_bar);
            appBarImage = activity.findViewById(R.id.demo_app_bar_image);
            grid = activity.findViewById(R.id.demo_grid);

            fullBackground = activity.findViewById(R.id.demo_full_background);
            pager = activity.findViewById(R.id.demo_pager);
            pagerTitle = activity.findViewById(R.id.demo_pager_title);
            pagerToolbar = activity.findViewById(R.id.demo_pager_toolbar);
            fullImage = activity.findViewById(R.id.demo_full_image);
            fullImageToolbar = activity.findViewById(R.id.demo_full_image_toolbar);
        }
    }

}
