package com.alexvasilkov.gestures.views.interfaces;

import android.graphics.RectF;

import androidx.annotation.Nullable;

public interface ClipBounds {

    /**
     * Clips view so only {@code rect} part will be drawn.
     *
     * @param rect Rectangle to clip view bounds, or {@code null} to turn clipping off
     */
    void clipBounds(@Nullable RectF rect);

}
