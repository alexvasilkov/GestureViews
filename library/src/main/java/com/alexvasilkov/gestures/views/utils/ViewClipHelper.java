package com.alexvasilkov.gestures.views.utils;

import android.graphics.Canvas;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import com.alexvasilkov.gestures.views.interfaces.ClipView;

public class ViewClipHelper implements ClipView {

    private final View mView;
    private final RectF mClipRect = new RectF(), mOldClipRect = new RectF();
    private boolean mIsClipping;

    public ViewClipHelper(@NonNull View view) {
        mView = view;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clipView(@Nullable RectF rect) {
        if (rect == null) {
            if (mIsClipping) {
                mIsClipping = false;
                mView.invalidate();
            }
        } else {
            // Setting previous clip rect
            if (mIsClipping) {
                mOldClipRect.set(mClipRect);
            } else {
                mOldClipRect.set(0, 0, mView.getWidth(), mView.getHeight());
            }

            mIsClipping = true;

            mClipRect.set(rect);

            // Invalidating only updated part
            int left = (int) Math.min(mClipRect.left, mOldClipRect.left);
            int top = (int) Math.min(mClipRect.top, mOldClipRect.top);
            int right = (int) Math.max(mClipRect.right, mOldClipRect.right) + 1;
            int bottom = (int) Math.max(mClipRect.bottom, mOldClipRect.bottom) + 1;
            mView.invalidate(left, top, right, bottom);
        }
    }

    public void onPreDraw(@NonNull Canvas canvas) {
        if (mIsClipping) {
            canvas.save();
            canvas.clipRect(mClipRect);
        }
    }

    public void onPostDraw(@NonNull Canvas canvas) {
        if (mIsClipping) {
            canvas.restore();
        }
    }

}
