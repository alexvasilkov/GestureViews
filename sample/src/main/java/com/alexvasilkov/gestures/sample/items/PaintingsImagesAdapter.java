package com.alexvasilkov.gestures.sample.items;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;

import com.alexvasilkov.gestures.widgets.GestureImageView;
import com.bumptech.glide.Glide;

public class PaintingsImagesAdapter extends PagerAdapter {

    private final ViewPager mViewPager;
    private final Painting[] mPaintings;

    public PaintingsImagesAdapter(ViewPager pager, Painting[] paintings) {
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

        GestureImageView gImageView = new GestureImageView(context);
        final int match = ViewGroup.LayoutParams.MATCH_PARENT;
        container.addView(gImageView, match, match);
        gImageView.fixViewPagerScroll(mViewPager);
        gImageView.getController().getSettings().setOverscrollDistance(context, 32, 0);

        Glide.with(context).load(mPaintings[position].getImageId()).into(gImageView);

        return gImageView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

}
