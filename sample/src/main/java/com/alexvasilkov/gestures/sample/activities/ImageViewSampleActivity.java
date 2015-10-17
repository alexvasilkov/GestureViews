package com.alexvasilkov.gestures.sample.activities;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.alexvasilkov.android.commons.texts.SpannableBuilder;
import com.alexvasilkov.android.commons.utils.Views;
import com.alexvasilkov.gestures.sample.R;
import com.alexvasilkov.gestures.sample.adapters.PaintingsImagesAdapter;
import com.alexvasilkov.gestures.sample.logic.Painting;
import com.alexvasilkov.gestures.sample.utils.GestureSettingsMenu;
import com.alexvasilkov.gestures.views.interfaces.GestureView;

public class ImageViewSampleActivity extends BaseActivity
        implements ViewPager.OnPageChangeListener, PaintingsImagesAdapter.OnSetupGestureViewListener {

    private Painting[] mPaintings;

    private ViewPager mViewPager;
    private TextView mTitleView;

    private GestureSettingsMenu mSettingsMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_image_view_sample);

        Toolbar toolbar = Views.find(this, R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mPaintings = Painting.getAllPaintings(getResources());

        mSettingsMenu = new GestureSettingsMenu();
        mSettingsMenu.onRestoreInstanceState(savedInstanceState);

        mTitleView = Views.find(this, R.id.painting_title);

        mViewPager = Views.find(this, R.id.paintings_view_pager);
        mViewPager.setAdapter(new PaintingsImagesAdapter(mViewPager, mPaintings, this));
        mViewPager.addOnPageChangeListener(this);
        mViewPager.setPageMargin(getResources().getDimensionPixelSize(R.dimen.view_pager_margin));
        onPageSelected(0); // Manually calling for first item
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        mSettingsMenu.onSaveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return mSettingsMenu.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mSettingsMenu.onOptionsItemSelected(item)) {
            invalidateOptionsMenu();
            mViewPager.getAdapter().notifyDataSetChanged();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        // no-op
    }

    @Override
    public void onPageSelected(int position) {
        CharSequence title = new SpannableBuilder(this)
                .createStyle().setFont(Typeface.DEFAULT_BOLD).apply()
                .append(R.string.paintings_author).append("\n")
                .clearStyle()
                .append(mPaintings[position].getTitle())
                .build();
        mTitleView.setText(title);
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        // no-op
    }

    @Override
    public void onSetupGestureView(GestureView view) {
        mSettingsMenu.applySettings(view);
    }

}
