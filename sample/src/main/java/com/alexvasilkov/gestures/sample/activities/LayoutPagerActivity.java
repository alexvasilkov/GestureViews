package com.alexvasilkov.gestures.sample.activities;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import com.alexvasilkov.fluffycommons.utils.Views;
import com.alexvasilkov.gestures.sample.R;
import com.alexvasilkov.gestures.sample.items.Painting;
import com.alexvasilkov.gestures.sample.items.PaintingsLayoutsAdapter;

public class LayoutPagerActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_layout_pager);

        ViewPager viewPager = Views.find(this, R.id.paintings_view_pager);
        viewPager.setAdapter(new PaintingsLayoutsAdapter(viewPager, Painting.getAllPaintings(getResources())));
        viewPager.setPageMargin(getResources().getDimensionPixelSize(R.dimen.view_pager_margin));
    }

}
