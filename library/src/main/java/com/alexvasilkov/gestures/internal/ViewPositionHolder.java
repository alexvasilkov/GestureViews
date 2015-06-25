package com.alexvasilkov.gestures.internal;

import android.os.Build;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewTreeObserver;

import com.alexvasilkov.gestures.animation.ViewPosition;

public class ViewPositionHolder implements ViewTreeObserver.OnGlobalLayoutListener {

    private final OnViewPositionChangedListener mListener;
    private final ViewPosition mPos = ViewPosition.newInstance();

    private View mView;

    public ViewPositionHolder(@NonNull OnViewPositionChangedListener listener) {
        mListener = listener;
    }

    @Override
    public void onGlobalLayout() {
        if (mView != null) {
            boolean changed = ViewPosition.from(mPos, mView);
            if (changed) mListener.onViewPositionChanged(mView, mPos);
        }
    }

    public void init(@NonNull View view) {
        mView = view;
        mView.getViewTreeObserver().addOnGlobalLayoutListener(this);
        if (isLaidOut()) onGlobalLayout();
    }

    @SuppressWarnings("deprecation")
    public void destroy() {
        if (mView != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                mView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            } else {
                mView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
            }
        }

        mPos.view.setEmpty();
        mPos.viewport.setEmpty();
        mPos.image.setEmpty();

        mView = null;
    }

    private boolean isLaidOut() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            return mView.isLaidOut();
        } else {
            return mView.getWidth() > 0 && mView.getHeight() > 0;
        }
    }

    public interface OnViewPositionChangedListener {
        void onViewPositionChanged(View view, ViewPosition position);
    }

}
