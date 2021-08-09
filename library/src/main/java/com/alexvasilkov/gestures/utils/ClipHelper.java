package com.alexvasilkov.gestures.utils;

import android.graphics.Canvas;
import android.graphics.RectF;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.alexvasilkov.gestures.State;
import com.alexvasilkov.gestures.views.interfaces.ClipView;

/**
 * Helper class to implement view clipping (with {@link ClipView} interface).
 *
 * Usage: call {@link #clipView(RectF, float)} method when needed and override
 * {@link View#draw(Canvas)} method:
 * <pre>{@code
 *   public void draw(Canvas canvas) {
 *       clipHelper.onPreDraw(canvas);
 *       super.draw(canvas);
 *       clipHelper.onPostDraw(canvas);
 *   }
 * }</pre>
 */
public class ClipHelper implements ClipView {

    private final View view;

    private boolean isClipping;

    private final RectF clipRect = new RectF();
    private float clipRotation;

    public ClipHelper(@NonNull View view) {
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
            isClipping = true;

            clipRect.set(rect);
            clipRotation = rotation;
            view.invalidate();
        }
    }

    public void onPreDraw(@NonNull Canvas canvas) {
        if (isClipping) {
            canvas.save();

            if (State.equals(clipRotation, 0f)) {
                canvas.clipRect(clipRect);
            } else {
                // Note, that prior Android 4.3 (18) canvas matrix is not correctly applied to
                // clip rect, clip rect will be set to its upper bound, which is good enough for us.
                canvas.rotate(clipRotation, clipRect.centerX(), clipRect.centerY());
                canvas.clipRect(clipRect);
                canvas.rotate(-clipRotation, clipRect.centerX(), clipRect.centerY());
            }
        }
    }

    public void onPostDraw(@NonNull Canvas canvas) {
        if (isClipping) {
            canvas.restore();
        }
    }

}
