package com.alexvasilkov.gestures.transition;

import android.support.annotation.NonNull;

public abstract class SimpleViewsTracker<V> implements ViewsTracker<V, Integer> {

    @Override
    public int getPositionForId(@NonNull Integer id) {
        return id;
    }

    @NonNull
    @Override
    public Integer getIdForPosition(int position) {
        return position;
    }

}
