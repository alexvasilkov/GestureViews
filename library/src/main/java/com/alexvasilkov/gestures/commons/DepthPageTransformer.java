package com.alexvasilkov.gestures.commons;

import android.os.Build;
import android.support.v4.view.ViewPager;
import android.view.View;

/**
 * Page transformer which will scroll previous page as usual and will scale next page with alpha.
 * <p/>
 * Usage: {@link ViewPager#setPageTransformer(boolean, ViewPager.PageTransformer)
 * ViewPager.setPageTransformer(true, new DepthPageTransformer())}
 */
public class DepthPageTransformer implements ViewPager.PageTransformer {

    private static final float MIN_SCALE = 0.75f;

    @Override
    public void transformPage(View view, float position) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
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

}
