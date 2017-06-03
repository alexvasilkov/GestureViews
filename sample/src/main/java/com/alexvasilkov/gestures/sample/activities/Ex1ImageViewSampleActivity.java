package com.alexvasilkov.gestures.sample.activities;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.alexvasilkov.android.commons.texts.SpannableBuilder;
import com.alexvasilkov.android.commons.ui.Views;
import com.alexvasilkov.gestures.sample.R;
import com.alexvasilkov.gestures.sample.adapters.PaintingsPagerAdapter;
import com.alexvasilkov.gestures.sample.logic.Painting;
import com.alexvasilkov.gestures.sample.logic.PaintingsHelper;
import com.alexvasilkov.gestures.sample.utils.GestureSettingsMenu;

public class Ex1ImageViewSampleActivity extends BaseActivity
        implements ViewPager.OnPageChangeListener {

    private Painting[] paintings;

    private ViewPager viewPager;
    private TextView titleView;

    private GestureSettingsMenu settingsMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_ex1_image_view);
        setTitle(null);

        Toolbar toolbar = Views.find(this, R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        paintings = PaintingsHelper.list(getResources());

        settingsMenu = new GestureSettingsMenu();
        settingsMenu.onRestoreInstanceState(savedInstanceState);

        titleView = Views.find(this, R.id.painting_title);

        viewPager = Views.find(this, R.id.paintings_view_pager);
        viewPager.setAdapter(new PaintingsPagerAdapter(viewPager, paintings, settingsMenu));
        viewPager.addOnPageChangeListener(this);
        viewPager.setPageMargin(getResources().getDimensionPixelSize(R.dimen.view_pager_margin));
        onPageSelected(0); // Manually calling for first item
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        settingsMenu.onSaveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return settingsMenu.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (settingsMenu.onOptionsItemSelected(item)) {
            supportInvalidateOptionsMenu();
            viewPager.getAdapter().notifyDataSetChanged();
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
                .append(paintings[position].getAuthor()).append("\n")
                .clearStyle()
                .append(paintings[position].getTitle())
                .build();
        titleView.setText(title);
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        // no-op
    }

}
