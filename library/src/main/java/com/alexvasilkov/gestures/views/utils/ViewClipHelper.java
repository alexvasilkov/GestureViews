package com.alexvasilkov.gestures.views.utils;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import com.alexvasilkov.gestures.views.interfaces.ClipView;

public class ViewClipHelper implements ClipView {

    private static final Matrix tmpMatrix = new Matrix();

    private final View view;

    private boolean isClipping;

    private final RectF clipRect = new RectF();
    private float clipRotation;

    private final RectF clipBounds = new RectF();
    private final RectF clipBoundsOld = new RectF();

    public ViewClipHelper(@NonNull View view) {
        this.view = view;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clipView(@Nullable RectF rect, float rotation) {
        if (rect == null) {
            if (isClipping) {
                isClipping = false;
                view.invalidate();
            }
        } else {
            // Setting previous clip rect
            if (isClipping) {
                clipBoundsOld.set(clipBounds);
            } else {
                clipBoundsOld.set(0f, 0f, view.getWidth(), view.getHeight());
            }

            isClipping = true;

            clipRect.set(rect);
            clipRotation = rotation;

            // Computing upper bounds of clipping rect after rotation
            clipBounds.set(clipRect);
            tmpMatrix.setRotate(rotation, clipRect.centerX(), clipRect.centerY());
            tmpMatrix.mapRect(clipBounds);

            // Invalidating only updated part
            int left = (int) Math.min(clipBounds.left, clipBoundsOld.left);
            int top = (int) Math.min(clipBounds.top, clipBoundsOld.top);
            int right = (int) Math.max(clipBounds.right, clipBoundsOld.right) + 1;
            int bottom = (int) Math.max(clipBounds.bottom, clipBoundsOld.bottom) + 1;
            view.invalidate(left, top, right, bottom);
        }
    }

    public void onPreDraw(@NonNull Canvas canvas) {
        if (isClipping) {
            canvas.save();

            // Note, that prior Android 4.3 (18) canvas matrix is not correctly applied to
            // clip rect, clip rect will be set to its upper bound, which is good enough for us.
            canvas.rotate(clipRotation, clipRect.centerX(), clipRect.centerY());
            canvas.clipRect(clipRect);
            canvas.rotate(-clipRotation, clipRect.centerX(), clipRect.centerY());
        }
    }

    public void onPostDraw(@NonNull Canvas canvas) {
        if (isClipping) {
            canvas.restore();
        }
    }

}
