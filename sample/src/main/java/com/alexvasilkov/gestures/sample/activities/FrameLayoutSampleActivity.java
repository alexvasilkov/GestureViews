package com.alexvasilkov.gestures.sample.activities;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;

import com.alexvasilkov.android.commons.utils.Views;
import com.alexvasilkov.gestures.sample.R;
import com.alexvasilkov.gestures.sample.adapters.PaintingsLayoutsAdapter;
import com.alexvasilkov.gestures.sample.logic.Painting;
import com.alexvasilkov.gestures.sample.utils.GestureSettingsMenu;
import com.alexvasilkov.gestures.views.interfaces.GestureView;

public class FrameLayoutSampleActivity extends BaseActivity
        implements PaintingsLayoutsAdapter.OnSetupGestureViewListener {

    private ViewPager mViewPager;
    private GestureSettingsMenu mSettingsMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_layout_pager);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mSettingsMenu = new GestureSettingsMenu();
        mSettingsMenu.onRestoreInstanceState(savedInstanceState);

        Painting[] paintings = Painting.getAllPaintings(getResources());

        mViewPager = Views.find(this, R.id.paintings_view_pager);
        mViewPager.setAdapter(new PaintingsLayoutsAdapter(mViewPager, paintings, this));
        mViewPager.setPageMargin(getResources().getDimensionPixelSize(R.dimen.view_pager_margin));
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
    public void onSetupGestureView(GestureView view) {
        mSettingsMenu.applySettings(view);
    }

}
