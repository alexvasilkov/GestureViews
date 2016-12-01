package com.alexvasilkov.gestures.internal;

import android.content.Context;
import android.util.TypedValue;

public class UnitsUtils {

    private UnitsUtils() {}

    public static float toPixels(Context context, float value) {
        return toPixels(context, TypedValue.COMPLEX_UNIT_DIP, value);
    }

    public static float toPixels(Context context, int type, float value) {
        return TypedValue.applyDimension(type, value,
                context.getResources().getDisplayMetrics());
    }

}
