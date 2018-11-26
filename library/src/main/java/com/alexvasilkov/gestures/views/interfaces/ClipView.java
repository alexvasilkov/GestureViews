package com.alexvasilkov.gestures.views.interfaces;

import android.graphics.RectF;

import androidx.annotation.Nullable;

public interface ClipView {

    /**
     * Clips view so only {@code rect} part (modified by view's state) will be drawn.
     *
     * @param rect Clip rectangle or {@code null} to turn clipping off
     * @param rotation Clip rectangle rotation
     */
    void clipView(@Nullable RectF rect, float rotation);

}
