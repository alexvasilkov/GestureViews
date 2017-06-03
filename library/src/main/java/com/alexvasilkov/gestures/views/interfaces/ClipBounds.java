package com.alexvasilkov.gestures.views.interfaces;

import android.graphics.RectF;
import android.support.annotation.Nullable;

public interface ClipBounds {

    /**
     * Clips view so only {@code rect} part will be drawn.
     * <p/>
     * Pass {@code null} to turn clipping off.
     */
    void clipBounds(@Nullable RectF rect);

}
