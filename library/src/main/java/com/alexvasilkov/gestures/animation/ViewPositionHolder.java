package com.alexvasilkov.gestures.animation;

import android.os.Build;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewTreeObserver;

class ViewPositionHolder implements ViewTreeObserver.OnPreDrawListener {

    private final ViewPosition mPos = ViewPosition.newInstance();

    private OnViewPositionChangeListener mListener;
    private View mView;
    private boolean mIsPaused;

    @Override
    public boolean onPreDraw() {
        update();
        return true;
    }

    public void init(@NonNull View view, @NonNull OnViewPositionChangeListener listener) {
        mView = view;
        mListener = listener;
        mView.getViewTreeObserver().addOnPreDrawListener(this);
        if (isLaidOut()) update();
    }

    @SuppressWarnings("deprecation")
    public void clear() {
        if (mView != null) mView.getViewTreeObserver().removeOnPreDrawListener(this);

        mPos.view.setEmpty();
        mPos.viewport.setEmpty();
        mPos.image.setEmpty();

        mView = null;
        mListener = null;
        mIsPaused = false;
    }

    public void pause(boolean paused) {
        if (mIsPaused == paused) return;

        mIsPaused = paused;
        if (!paused) update();
    }

    private void update() {
        if (mView != null && mListener != null && !mIsPaused) {
            boolean changed = ViewPosition.apply(mPos, mView);
            if (changed) mListener.onViewPositionChanged(mPos);
        }
    }

    private boolean isLaidOut() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            return mView.isLaidOut();
        } else {
            return mView.getWidth() > 0 && mView.getHeight() > 0;
        }
    }

    public interface OnViewPositionChangeListener {
        void onViewPositionChanged(@NonNull ViewPosition position);
    }

}
