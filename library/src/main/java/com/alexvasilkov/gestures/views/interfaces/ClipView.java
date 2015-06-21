package com.alexvasilkov.gestures.views.interfaces;

import android.graphics.RectF;
import android.support.annotation.Nullable;

public interface ClipView {

    /**
     * Clips view so only part specified in {@code rect} will be drawn.
     * <p/>
     * Pass {@code null} to turn clipping off.
     */
    void clipView(@Nullable RectF rect);

}
