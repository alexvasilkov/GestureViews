package com.alexvasilkov.gestures.transition;

import android.support.annotation.NonNull;
import android.view.View;

public interface ViewsTracker<ID> {

    int NO_POSITION = -1;

    /**
     * @return position for item with given id, or {@link #NO_POSITION} if item was not found
     */
    int getPositionForId(@NonNull ID id);

    /**
     * @return item's id at given position, or {@code null} if position is invalid
     */
    ID getIdForPosition(int position);

    /**
     * @return view at given position from which to start animation, or {@code null} if there is no
     * known view for given position, or position is invalid
     */
    View getViewForPosition(int position);

}
