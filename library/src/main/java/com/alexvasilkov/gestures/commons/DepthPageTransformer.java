package com.alexvasilkov.gestures.commons;

import android.view.View;

import androidx.viewpager.widget.ViewPager;

/**
 * Page transformer which will scroll previous page as usual and will scale next page with alpha.
 * <p>
 * Usage: {@link ViewPager#setPageTransformer(boolean, ViewPager.PageTransformer)
 * ViewPager.setPageTransformer(true, new DepthPageTransformer())}
 */
public class DepthPageTransformer implements ViewPager.PageTransformer {

    private static final float MIN_SCALE = 0.75f;

    @Override
    public void transformPage(View view, float position) {
        if (0 < position && position < 1f) {
            // Fade the page out
            view.setAlpha(1f - position);

            // Counteract the default slide transition
            view.setTranslationX(-view.getWidth() * position);

            // Scale the page down (between MIN_SCALE and 1)
            float scaleFactor = 1f - (1f - MIN_SCALE) * position;
            view.setScaleX(scaleFactor);
            view.setScaleY(scaleFactor);
        } else {
            view.setAlpha(1f);
            view.setTranslationX(0f);
            view.setScaleX(1f);
            view.setScaleY(1f);
        }
    }

}
