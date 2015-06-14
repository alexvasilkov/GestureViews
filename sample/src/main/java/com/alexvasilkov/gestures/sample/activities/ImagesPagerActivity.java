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
import com.alexvasilkov.gestures.sample.items.Painting;
import com.alexvasilkov.gestures.sample.items.PaintingsImagesAdapter;

public class ImagesPagerActivity extends BaseActivity implements ViewPager.OnPageChangeListener {

    private Painting[] mPaintings;

    private ViewPager mViewPager;
    private TextView mTitleView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_images_pager);

        Toolbar toolbar = Views.find(this, R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mPaintings = Painting.getAllPaintings(getResources());

        mTitleView = Views.find(this, R.id.painting_title);

        mViewPager = Views.find(this, R.id.paintings_view_pager);
        mViewPager.setAdapter(new PaintingsImagesAdapter(mViewPager, mPaintings));
        mViewPager.addOnPageChangeListener(this);
        mViewPager.setPageMargin(getResources().getDimensionPixelSize(R.dimen.view_pager_margin));
        onPageSelected(0); // Manually calling for first item
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem crop = menu.add(Menu.NONE, R.id.menu_crop, 0, R.string.button_crop);
        crop.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        crop.setIcon(R.drawable.ic_crop_white_24dp);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_crop:
                ImageCropActivity.show(this, mViewPager.getCurrentItem());
                return true;
            default:
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

}
