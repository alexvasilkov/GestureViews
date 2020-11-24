package com.alexvasilkov.gestures.sample.ex.image.pager;

import android.os.Bundle;

import androidx.viewpager.widget.ViewPager;

import com.alexvasilkov.gestures.commons.RecyclePagerAdapter;
import com.alexvasilkov.gestures.sample.R;
import com.alexvasilkov.gestures.sample.base.BaseSettingsActivity;
import com.alexvasilkov.gestures.views.GestureImageView;

import java.util.Objects;

/**
 * This example demonstrates usage of {@link GestureImageView} within ViewPager.<br>
 * Two things worth noting here:
 * <ul>
 * <li/> For each GestureImageView inside ViewPager we should enable smooth scrolling by calling
 * {@code gestureImage.getController().enableScrollInViewPager(viewPager)}
 * <li/> It is advised to use {@link RecyclePagerAdapter} as ViewPager adapter for better
 * performance when dealing with heavy data like images.
 * </ul>
 */
public class ViewPagerActivity extends BaseSettingsActivity {

    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.image_pager_screen);
        setInfoText(R.string.info_image_pager);

        // Initializing ViewPager
        viewPager = findViewById(R.id.pager);
        viewPager.setAdapter(new ViewPagerAdapter(viewPager, getSettingsController()));
        viewPager.setPageMargin(getResources().getDimensionPixelSize(R.dimen.view_pager_margin));
    }

    @Override
    protected void onSettingsChanged() {
        // Applying settings from toolbar menu, see BaseExampleActivity
        Objects.requireNonNull(viewPager.getAdapter()).notifyDataSetChanged();
    }

}
