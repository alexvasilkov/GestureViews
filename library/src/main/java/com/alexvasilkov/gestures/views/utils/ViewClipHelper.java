package com.alexvasilkov.gestures.views.utils;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import com.alexvasilkov.gestures.State;
import com.alexvasilkov.gestures.views.interfaces.ClipView;
import com.alexvasilkov.gestures.views.interfaces.GestureView;

public class ViewClipHelper implements ClipView {

    private static final Matrix tmpMatrix = new Matrix();
    private static final Matrix tmpMatrixInverse = new Matrix();

    private final View view;
    private final GestureView gestureView;
    private final RectF clipRect = new RectF();
    private final State clipState = new State();
    private final RectF clipBounds = new RectF();
    private final RectF clipBoundsOld = new RectF();
    private boolean isClipping;

    public <T extends View & GestureView> ViewClipHelper(@NonNull T view) {
        this.view = view;
        this.gestureView = view;
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
                clipBoundsOld.set(clipBounds);
            } else {
                clipBoundsOld.set(0f, 0f, view.getWidth(), view.getHeight());
            }

            isClipping = true;

            clipRect.set(rect);
            clipState.set(gestureView.getController().getState());

            clipBounds.set(clipRect);
            clipState.get(tmpMatrix);
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

            // Clip state transforms to viewport coordinates (excluding paddings),
            // so we need to add padding to move to view (canvas) coordinates.
            clipState.get(tmpMatrix);
            tmpMatrix.postTranslate(view.getPaddingLeft(), view.getPaddingTop());
            tmpMatrix.invert(tmpMatrixInverse);

            // Note, that prior Android 4.3 (18) canvas matrix is not correctly applied to
            // clip rect, clip rect will be set to its upper bound, which is good enough as well.
            canvas.concat(tmpMatrix);
            canvas.clipRect(clipRect);
            canvas.concat(tmpMatrixInverse);
        }
    }

    public void onPostDraw(@NonNull Canvas canvas) {
        if (isClipping) {
            canvas.restore();
        }
    }

}
