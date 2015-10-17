package com.alexvasilkov.gestures.transition;

import android.support.annotation.NonNull;
import android.view.View;

public abstract class ViewsTracker<V, ID> {

    public final V parent;

    public ViewsTracker(V parent) {
        this.parent = parent;
    }

    public abstract int getPositionForId(@NonNull ID id);

    @NonNull
    public abstract ID getIdForPosition(int position);

    public abstract View getViewForPosition(int position);

}
