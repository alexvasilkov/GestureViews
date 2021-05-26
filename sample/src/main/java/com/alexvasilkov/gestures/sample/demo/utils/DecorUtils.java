package com.alexvasilkov.gestures.sample.demo.utils;

import android.graphics.Rect;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;

import androidx.core.graphics.Insets;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.alexvasilkov.gestures.sample.R;

import java.util.ArrayList;
import java.util.List;

public class DecorUtils {

    private DecorUtils() {}

    private static void addListener(View view, InsetsListener listener) {
        View root = view.getRootView().findViewById(android.R.id.content);
        root = ((ViewGroup) root).getChildAt(0);
        ApplyInsetsListener rootListener = (ApplyInsetsListener) root.getTag(R.id.tag_insets);

        if (rootListener == null) {
            rootListener = new ApplyInsetsListener();
            root.setTag(R.id.tag_insets, rootListener);
            ViewCompat.setOnApplyWindowInsetsListener(root, rootListener);
        }

        rootListener.addListener(listener);
    }

    public static void padding(View view, int direction) {
        addListener(view, insets -> {
            final Rect extra = getExtraInsets(view, insets, direction, R.id.tag_extra_padding);

            view.setPadding(
                    view.getPaddingLeft() + extra.left,
                    view.getPaddingTop() + extra.top,
                    view.getPaddingRight() + extra.right,
                    view.getPaddingBottom() + extra.bottom
            );

            ViewGroup.LayoutParams params = view.getLayoutParams();
            if (params.height > 0) {
                params.height += extra.top + extra.bottom;
            }
            if (params.width > 0) {
                params.width += extra.left + extra.right;
            }
            view.setLayoutParams(params);
        });
    }

    public static void margin(View view, int direction) {
        addListener(view, insets -> {
            final Rect extra = getExtraInsets(view, insets, direction, R.id.tag_extra_margin);
            MarginLayoutParams params = (MarginLayoutParams) view.getLayoutParams();
            params.leftMargin += extra.left;
            params.topMargin += extra.top;
            params.rightMargin += extra.right;
            params.bottomMargin += extra.bottom;
            view.setLayoutParams(params);
        });
    }

    public static void size(View view, int direction) {
        addListener(view, insets -> {
            final Rect extra = getExtraInsets(view, insets, direction, R.id.tag_extra_size);
            ViewGroup.LayoutParams params = view.getLayoutParams();
            if (params.height > 0) {
                params.height += extra.top + extra.bottom;
            }
            if (params.width > 0) {
                params.width += extra.left + extra.right;
            }
            view.setLayoutParams(params);
        });
    }

    public static void onInsetsChanged(View view, Runnable action) {
        addListener(view, insets -> action.run());
    }


    private static Rect getExtraInsets(View view, Insets insets, int direction, int tagId) {
        Rect oldInsets = (Rect) view.getTag(tagId);
        oldInsets = oldInsets == null ? new Rect() : oldInsets;

        Rect newInsets = new Rect();
        newInsets.set(
                (direction & Gravity.LEFT) == Gravity.LEFT ? insets.left : 0,
                (direction & Gravity.TOP) == Gravity.TOP ? insets.top : 0,
                (direction & Gravity.RIGHT) == Gravity.RIGHT ? insets.right : 0,
                (direction & Gravity.BOTTOM) == Gravity.BOTTOM ? insets.bottom : 0
        );
        view.setTag(tagId, newInsets);

        return new Rect(
                newInsets.left - oldInsets.left,
                newInsets.top - oldInsets.top,
                newInsets.right - oldInsets.right,
                newInsets.bottom - oldInsets.bottom
        );
    }


    private static class ApplyInsetsListener implements OnApplyWindowInsetsListener {
        private final List<InsetsListener> listeners = new ArrayList<>();
        private Insets lastInsets;

        @Override
        public WindowInsetsCompat onApplyWindowInsets(View view, WindowInsetsCompat insets) {
            lastInsets = insets.getSystemWindowInsets();
            for (InsetsListener listener : listeners) {
                listener.applyInsets(lastInsets);
            }
            return insets.consumeStableInsets();
        }

        void addListener(InsetsListener listener) {
            listeners.add(listener);
            if (lastInsets != null) {
                listener.applyInsets(lastInsets);
            }
        }
    }

    private interface InsetsListener {
        void applyInsets(Insets insets);
    }

}
