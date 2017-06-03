package com.alexvasilkov.gestures.sample.activities;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;

import com.alexvasilkov.android.commons.ui.Views;
import com.alexvasilkov.gestures.sample.R;
import com.alexvasilkov.gestures.sample.adapters.PaintingsLayoutsPagerAdapter;
import com.alexvasilkov.gestures.sample.logic.Painting;
import com.alexvasilkov.gestures.sample.logic.PaintingsHelper;
import com.alexvasilkov.gestures.sample.utils.GestureSettingsMenu;

public class Ex2FrameLayoutSampleActivity extends BaseActivity {

    private ViewPager viewPager;
    private GestureSettingsMenu settingsMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_ex2_frame_layout);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        settingsMenu = new GestureSettingsMenu();
        settingsMenu.onRestoreInstanceState(savedInstanceState);

        Painting[] paintings = PaintingsHelper.list(getResources());

        viewPager = Views.find(this, R.id.paintings_view_pager);
        viewPager.setAdapter(
                new PaintingsLayoutsPagerAdapter(viewPager, paintings, settingsMenu));
        viewPager.setPageMargin(getResources().getDimensionPixelSize(R.dimen.view_pager_margin));
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

}
