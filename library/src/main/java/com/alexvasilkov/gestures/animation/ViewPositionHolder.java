package com.alexvasilkov.gestures.animation;

import android.os.Build;
import android.view.View;
import android.view.ViewTreeObserver;

import androidx.annotation.NonNull;

/**
 * Helper class that monitors {@link View} position on screen and notifies
 * {@link OnViewPositionChangeListener} if any changes were detected.
 */
class ViewPositionHolder implements ViewTreeObserver.OnPreDrawListener {

    private final ViewPosition pos = ViewPosition.newInstance();

    private OnViewPositionChangeListener listener;
    private View view;
    private boolean isPaused;

    @Override
    public boolean onPreDraw() {
        update();
        return true;
    }

    void init(@NonNull View view, @NonNull OnViewPositionChangeListener listener) {
        this.view = view;
        this.listener = listener;

        view.getViewTreeObserver().addOnPreDrawListener(this);
        if (isLaidOut()) {
            update();
        }
    }

    void clear() {
        if (view != null) {
            view.getViewTreeObserver().removeOnPreDrawListener(this);
        }

        pos.view.setEmpty();
        pos.viewport.setEmpty();
        pos.image.setEmpty();

        view = null;
        listener = null;
        isPaused = false;
    }

    void pause(boolean paused) {
        if (isPaused == paused) {
            return;
        }

        isPaused = paused;
        update();
    }

    private void update() {
        if (view != null && listener != null && !isPaused) {
            boolean changed = ViewPosition.apply(pos, view);
            if (changed) {
                listener.onViewPositionChanged(pos);
            }
        }
    }

    private boolean isLaidOut() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            return view.isLaidOut();
        } else {
            return view.getWidth() > 0 && view.getHeight() > 0;
        }
    }

    interface OnViewPositionChangeListener {
        void onViewPositionChanged(@NonNull ViewPosition position);
    }

}
