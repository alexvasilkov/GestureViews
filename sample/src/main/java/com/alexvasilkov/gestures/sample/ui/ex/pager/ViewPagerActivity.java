package com.alexvasilkov.gestures.sample.ui.ex.pager;

import android.os.Bundle;
import android.support.v4.view.ViewPager;

import com.alexvasilkov.gestures.commons.RecyclePagerAdapter;
import com.alexvasilkov.gestures.sample.R;
import com.alexvasilkov.gestures.sample.ui.base.BaseExampleActivity;
import com.alexvasilkov.gestures.views.GestureImageView;

/**
 * Simple example demonstrates usage of {@link GestureImageView} within ViewPager.<br/>
 * Two things worth noting here:
 * <ul>
 * <li/> For each GestureImageView inside ViewPager we should enable smooth scrolling by calling
 * {@code gestureImage.getController().enableScrollInViewPager(viewPager)}
 * <li/> It is advised to use {@link RecyclePagerAdapter} as ViewPager adapter for better
 * performance when dealing with heavy data like images.
 * </ul>
 */
public class ViewPagerActivity extends BaseExampleActivity {

    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.pager_screen);
        setTitle(null);

        // Initializing ViewPager
        viewPager = findViewById(R.id.pager);
        viewPager.setAdapter(new ViewPagerAdapter(viewPager, getSettingsListener()));
        viewPager.setPageMargin(getResources().getDimensionPixelSize(R.dimen.view_pager_margin));
    }

    @Override
    protected void onSettingsChanged() {
        // Applying settings from toolbar menu, see BaseExampleActivity
        viewPager.getAdapter().notifyDataSetChanged();
    }

}
