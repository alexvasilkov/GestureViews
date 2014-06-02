package com.alexvasilkov.gestures.sample.activities;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.widget.TextView;
import com.alexvasilkov.fluffycommons.texts.SpannableBuilder;
import com.alexvasilkov.fluffycommons.utils.Views;
import com.alexvasilkov.gestures.sample.R;
import com.alexvasilkov.gestures.sample.items.Painting;
import com.alexvasilkov.gestures.sample.items.PaintingsAdapter;

public class PagerActivity extends Activity implements ViewPager.OnPageChangeListener {

    private Painting[] mPaintings;

    private TextView mTitleView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pager);

        mPaintings = Painting.getAllPaintings(getResources());

        mTitleView = Views.find(this, R.id.painting_title);

        ViewPager viewPager = Views.find(this, R.id.paintings_view_pager);
        viewPager.setAdapter(new PaintingsAdapter(viewPager, mPaintings));
        viewPager.setOnPageChangeListener(this);
        viewPager.setPageMargin(getResources().getDimensionPixelSize(R.dimen.view_pager_margin));
        onPageSelected(0); // Manually calling for first item
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
