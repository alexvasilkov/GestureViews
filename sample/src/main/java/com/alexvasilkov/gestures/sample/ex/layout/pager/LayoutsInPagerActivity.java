package com.alexvasilkov.gestures.sample.ex.layout.pager;

import android.os.Bundle;

import com.alexvasilkov.gestures.commons.RecyclePagerAdapter;
import com.alexvasilkov.gestures.sample.R;
import com.alexvasilkov.gestures.sample.base.BaseSettingsActivity;
import com.alexvasilkov.gestures.sample.ex.utils.Painting;
import com.alexvasilkov.gestures.views.GestureFrameLayout;

import androidx.viewpager.widget.ViewPager;

import java.util.Objects;

/**
 * Simple example demonstrates usage of {@link GestureFrameLayout} within ViewPager.<br>
 * Two things worth noting here:
 * <ol>
 * <li/> For each GestureFrameLayout inside ViewPager we need to enable smooth scrolling by calling
 * {@code gestureLayout.getController().enableScrollInViewPager(viewPager)}
 * <li/> It is advised to use {@link RecyclePagerAdapter} as ViewPager adapter for better
 * performance.
 * </ol>
 */
public class LayoutsInPagerActivity extends BaseSettingsActivity {

    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.layout_pager_screen);

        final Painting[] paintings = Painting.list(getResources());

        viewPager = findViewById(R.id.frame_pager);
        viewPager.setAdapter(
                new LayoutsPagerAdapter(viewPager, paintings, getSettingsController()));
        viewPager.setPageMargin(getResources().getDimensionPixelSize(R.dimen.view_pager_margin));
    }

    @Override
    protected void onSettingsChanged() {
        // Applying settings from toolbar menu, see BaseExampleActivity
        Objects.requireNonNull(viewPager.getAdapter()).notifyDataSetChanged();
    }

}
