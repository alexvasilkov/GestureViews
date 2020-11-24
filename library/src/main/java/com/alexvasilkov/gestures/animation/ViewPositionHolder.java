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
    private View.OnAttachStateChangeListener attachListener;
    private boolean isPaused;

    @Override
    public boolean onPreDraw() {
        update();
        return true;
    }

    void init(@NonNull View view, @NonNull OnViewPositionChangeListener listener) {
        clear(); // Cleaning up old listeners, just in case

        this.view = view;
        this.listener = listener;

        attachListener = new View.OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(View view) {
                onViewAttached(view, true);
            }

            @Override
            public void onViewDetachedFromWindow(View view) {
                onViewAttached(view, false);
            }
        };
        view.addOnAttachStateChangeListener(attachListener);

        onViewAttached(view, isAttached(view));

        if (isLaidOut(view)) {
            update();
        }
    }

    private void onViewAttached(View view, boolean attached) {
        view.getViewTreeObserver().removeOnPreDrawListener(this);
        if (attached) {
            view.getViewTreeObserver().addOnPreDrawListener(this);
        }
    }

    void clear() {
        if (view != null) {
            view.removeOnAttachStateChangeListener(attachListener);
            onViewAttached(view, false);
        }

        pos.view.setEmpty();
        pos.viewport.setEmpty();
        pos.image.setEmpty();

        view = null;
        attachListener = null;
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

    private static boolean isLaidOut(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            return view.isLaidOut();
        } else {
            return view.getWidth() > 0 && view.getHeight() > 0;
        }
    }

    private static boolean isAttached(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            return view.isAttachedToWindow();
        } else {
            return view.getWindowToken() != null;
        }
    }

    interface OnViewPositionChangeListener {
        void onViewPositionChanged(@NonNull ViewPosition position);
    }

}
