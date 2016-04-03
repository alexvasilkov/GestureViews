package com.alexvasilkov.gestures.views.utils;

import android.graphics.Canvas;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import com.alexvasilkov.gestures.views.interfaces.ClipView;

public class ViewClipHelper implements ClipView {

    private final View view;
    private final RectF clipRect = new RectF();
    private final RectF clipRectOld = new RectF();
    private boolean isClipping;

    public ViewClipHelper(@NonNull View view) {
        this.view = view;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clipView(@Nullable RectF rect) {
        if (rect == null) {
            if (isClipping) {
                isClipping = false;
                view.invalidate();
            }
        } else {
            // Setting previous clip rect
            if (isClipping) {
                clipRectOld.set(clipRect);
            } else {
                clipRectOld.set(0, 0, view.getWidth(), view.getHeight());
            }

            isClipping = true;

            clipRect.set(rect);

            // Invalidating only updated part
            int left = (int) Math.min(clipRect.left, clipRectOld.left);
            int top = (int) Math.min(clipRect.top, clipRectOld.top);
            int right = (int) Math.max(clipRect.right, clipRectOld.right) + 1;
            int bottom = (int) Math.max(clipRect.bottom, clipRectOld.bottom) + 1;
            view.invalidate(left, top, right, bottom);
        }
    }

    public void onPreDraw(@NonNull Canvas canvas) {
        if (isClipping) {
            canvas.save();
            canvas.clipRect(clipRect);
        }
    }

    public void onPostDraw(@NonNull Canvas canvas) {
        if (isClipping) {
            canvas.restore();
        }
    }

}
