package com.alexvasilkov.gestures.sample.utils;

import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;

public class ViewsCompat {

    @SuppressWarnings("deprecation")
    public static void setBackground(View view, Drawable drawable) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            view.setBackgroundDrawable(drawable);
        } else {
            view.setBackground(drawable);
        }
    }

}
