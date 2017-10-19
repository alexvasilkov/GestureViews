package com.alexvasilkov.gestures.sample.ui.ex2;

import android.os.Bundle;
import android.support.v4.view.ViewPager;

import com.alexvasilkov.android.commons.ui.Views;
import com.alexvasilkov.gestures.commons.RecyclePagerAdapter;
import com.alexvasilkov.gestures.sample.R;
import com.alexvasilkov.gestures.sample.ui.base.BaseExampleActivity;
import com.alexvasilkov.gestures.sample.ui.ex.Painting;
import com.alexvasilkov.gestures.views.GestureFrameLayout;

/**
 * Simple example demonstrates usage of {@link GestureFrameLayout} within ViewPager.<br/>
 * Two things worth noting here:
 * <ol>
 * <li/> For each GestureFrameLayout inside ViewPager we need to enable smooth scrolling by calling
 * {@code gestureLayout.getController().enableScrollInViewPager(viewPager)}
 * <li/> It is advised to use {@link RecyclePagerAdapter} as ViewPager adapter for better
 * performance.
 * </ol>
 */
public class LayoutsInPagerActivity extends BaseExampleActivity {

    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.ex2_screen);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final Painting[] paintings = Painting.list(getResources());

        viewPager = Views.find(this, R.id.paintings_view_pager);
        viewPager.setAdapter(new LayoutsPagerAdapter(viewPager, paintings, getSettingsListener()));
        viewPager.setPageMargin(getResources().getDimensionPixelSize(R.dimen.view_pager_margin));
    }

    @Override
    protected void onSettingsChanged() {
        viewPager.getAdapter().notifyDataSetChanged();
    }

}
