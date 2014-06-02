package com.alexvasilkov.gestures.sample.items;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.alexvasilkov.fluffycommons.texts.SpannableBuilder;
import com.alexvasilkov.fluffycommons.utils.UsefulIntents;
import com.alexvasilkov.fluffycommons.utils.Views;
import com.alexvasilkov.gestures.sample.R;
import com.alexvasilkov.gestures.widgets.GestureLayout;

public class PaintingsLayoutsAdapter extends PagerAdapter implements View.OnClickListener {

    private final ViewPager mViewPager;
    private final Painting[] mPaintings;

    public PaintingsLayoutsAdapter(ViewPager pager, Painting[] paintings) {
        mViewPager = pager;
        mPaintings = paintings;
    }

    @Override
    public int getCount() {
        return mPaintings.length;
    }

    @Override
    public View instantiateItem(final ViewGroup container, int position) {
        Context context = container.getContext();
        View layout = LayoutInflater.from(context).inflate(R.layout.activity_layout_item, container, false);
        container.addView(layout, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        GestureLayout gLayout = Views.find(layout, R.id.painting_g_layout);
        gLayout.fixViewPagerScroll(mViewPager);

        ImageView image = Views.find(layout, R.id.painting_image);
        image.setImageResource(mPaintings[position].getImageId());

        TextView title = Views.find(layout, R.id.painting_title);
        CharSequence titleText = new SpannableBuilder(context)
                .createStyle().setFont(Typeface.DEFAULT_BOLD).apply()
                .append(R.string.paintings_author).append("\n")
                .clearStyle()
                .append(mPaintings[position].getTitle())
                .build();
        title.setText(titleText);

        View button = Views.find(layout, R.id.painting_button);
        button.setTag(mPaintings[position].getLink());
        button.setOnClickListener(this);

        return layout;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public void onClick(View view) {
        UsefulIntents.get(view.getContext()).openWebBrowser((String) view.getTag());
    }

}
