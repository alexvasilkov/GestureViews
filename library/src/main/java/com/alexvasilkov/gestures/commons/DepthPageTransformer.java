package com.alexvasilkov.gestures.commons;

import android.os.Build;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.widget.ViewPager2;

/**
 * Page transformer which will scroll previous page as usual and will scale next page with alpha.
 * <p>
 * Usage: {@link ViewPager#setPageTransformer(boolean, ViewPager.PageTransformer)
 * ViewPager.setPageTransformer(true, new DepthPageTransformer())} or
 * {@link ViewPager2#setPageTransformer(ViewPager2.PageTransformer)
 * ViewPager2.setPageTransformer(new DepthPageTransformer())}.
 */
public class DepthPageTransformer implements ViewPager.PageTransformer, ViewPager2.PageTransformer {

    private static final float MIN_SCALE = 0.75f;

    @Override
    public void transformPage(@NonNull View view, float position) {
        if (0 < position && position < 1f) {
            // Fade the page out
            view.setAlpha(1f - position);

            // Counteract the default slide transition
            view.setTranslationX(-view.getWidth() * position);

            // Scale the page down (between MIN_SCALE and 1)
            float scaleFactor = 1f - (1f - MIN_SCALE) * position;
            view.setScaleX(scaleFactor);
            view.setScaleY(scaleFactor);
            if (Build.VERSION.SDK_INT >= 21) {
                view.setTranslationZ(scaleFactor - 1f);
            }
        } else {
            view.setAlpha(1f);
            view.setTranslationX(0f);
            view.setScaleX(1f);
            view.setScaleY(1f);
            if (Build.VERSION.SDK_INT >= 21) {
                view.setTranslationZ(0f);
            }
        }
    }

}
