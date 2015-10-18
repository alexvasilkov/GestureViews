package com.alexvasilkov.gestures.sample.views;

import android.support.v4.view.ViewPager;
import android.view.View;

public class DepthPageTransformer implements ViewPager.PageTransformer {

    private static final float MIN_SCALE = 0.75f;

    @Override
    public void transformPage(View view, float position) {
        if (0 < position && position < 1f) {
            // Fade the page out
            view.setAlpha(Math.max(1f - position, 0f));

            // Counteract the default slide transition
            view.setTranslationX(-view.getWidth() * position);

            // Scale the page down (between MIN_SCALE and 1)
            float scaleFactor = 1f - (1f - MIN_SCALE) * Math.abs(position);
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
