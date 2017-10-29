package com.alexvasilkov.gestures.sample.ex.list.complex;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.alexvasilkov.gestures.animation.ViewPositionAnimator;
import com.alexvasilkov.gestures.commons.DepthPageTransformer;
import com.alexvasilkov.gestures.sample.R;
import com.alexvasilkov.gestures.sample.base.BaseExampleActivity;
import com.alexvasilkov.gestures.transition.ViewsTransitionAnimator;

import java.util.List;

/**
 * Base implementation of complex list examples. Subclasses should provide there own logic for
 * list items creation, animator initialization and pager items setup.
 */
abstract class BaseComplexListActivity extends BaseExampleActivity
        implements ViewPositionAnimator.PositionUpdateListener, ListAdapter.OnImageClickListener {

    private ViewPager pager;
    private View pagerBackground;
    private PagerAdapter pagerAdapter;

    private ViewsTransitionAnimator animator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.complex_list_screen);

        final RecyclerView list = findViewById(R.id.complex_list);
        pager = findViewById(R.id.complex_pager);
        pagerBackground = findViewById(R.id.complex_pager_background);

        final List<ListItem> items = createItems();

        // Setting up recycler view
        list.setLayoutManager(new LinearLayoutManager(this));
        list.setAdapter(new ListAdapter(items, this));

        // Setting up pager view
        pagerAdapter = new PagerAdapter(pager, getSettingsListener());
        pager.setAdapter(pagerAdapter);
        pager.setPageTransformer(true, new DepthPageTransformer());

        // Setting up animator
        animator = createAnimator(list, pager);
        animator.addPositionUpdateListener(this);
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
        // Applying settings from toolbar menu, see BaseExampleActivity
        pagerAdapter.notifyDataSetChanged();
    }

    @Override
    public void onPositionUpdate(float position, boolean isLeaving) {
        pagerBackground.setVisibility(position == 0f ? View.INVISIBLE : View.VISIBLE);
        pagerBackground.setAlpha(position);

        if (isLeaving && position == 0f) {
            pagerAdapter.setPaintings(null);
        }

        // Fading out images without "from" position
        if (animator.getFromView() == null && isLeaving) {
            float toPosition = animator.getToView() == null
                    ? 1f : animator.getToView().getPositionAnimator().getToPosition();
            pager.setAlpha(position / toPosition);
        } else {
            pager.setAlpha(1f);
        }
    }

    @Override
    public void onImageClick(int itemPos, int imagePos) {
        openImageInPager(pagerAdapter, itemPos, imagePos);
    }


    protected abstract List<ListItem> createItems();

    protected abstract ViewsTransitionAnimator createAnimator(RecyclerView list, ViewPager pager);

    protected abstract void openImageInPager(PagerAdapter adapter, int itemPos, int imagePos);

}
