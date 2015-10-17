package com.alexvasilkov.gestures.transition;

import android.support.annotation.NonNull;
import android.view.View;

public interface ViewsTracker<V, ID> {

    int getPositionForId(@NonNull ID id);

    @NonNull
    ID getIdForPosition(int position);

    View getViewForPosition(int position);

}
