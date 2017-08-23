package com.alexvasilkov.gestures.sample.ui.ex1;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import com.alexvasilkov.android.commons.texts.SpannableBuilder;
import com.alexvasilkov.android.commons.ui.Views;
import com.alexvasilkov.gestures.commons.RecyclePagerAdapter;
import com.alexvasilkov.gestures.sample.R;
import com.alexvasilkov.gestures.sample.logic.Painting;
import com.alexvasilkov.gestures.sample.logic.Paintings;
import com.alexvasilkov.gestures.sample.ui.base.BaseExampleActivity;
import com.alexvasilkov.gestures.views.GestureImageView;

/**
 * Simple example demonstrates usage of {@link GestureImageView} within ViewPager.<br/>
 * Two things worth noting here:
 * <ol>
 * <li/> For each GestureImageView inside ViewPager we need to enable smooth scrolling by calling
 * {@code gestureImage.getController().enableScrollInViewPager(viewPager)}
 * <li/> It is advised to use {@link RecyclePagerAdapter} as ViewPager adapter for better
 * performance.
 * </ol>
 */
public class ImagesInPagerActivity extends BaseExampleActivity {

    private Painting[] paintings;

    private ViewPager viewPager;
    private TextView titleView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.ex1_screen);
        setTitle(null);

        final Toolbar toolbar = Views.find(this, R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        paintings = Paintings.list(getResources());

        titleView = Views.find(this, R.id.painting_title);

        viewPager = Views.find(this, R.id.paintings_view_pager);
        viewPager.setAdapter(new ImagesPagerAdapter(viewPager, paintings, getSettingsListener()));
        viewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                showPaintingInfo(position);
            }
        });
        viewPager.setPageMargin(getResources().getDimensionPixelSize(R.dimen.view_pager_margin));

        showPaintingInfo(0); // Manually calling for the first item
    }

    @Override
    protected void onSettingsChanged() {
        viewPager.getAdapter().notifyDataSetChanged();
    }

    private void showPaintingInfo(int position) {
        final CharSequence title = new SpannableBuilder(this)
                .createStyle().setFont(Typeface.DEFAULT_BOLD).apply()
                .append(paintings[position].author).append("\n")
                .clearStyle()
                .append(paintings[position].title)
                .build();
        titleView.setText(title);
    }

}
