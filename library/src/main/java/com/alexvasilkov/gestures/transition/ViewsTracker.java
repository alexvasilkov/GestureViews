package com.alexvasilkov.gestures.transition;

import android.view.View;

import com.alexvasilkov.gestures.transition.tracker.FromTracker;
import com.alexvasilkov.gestures.transition.tracker.IntoTracker;

import androidx.annotation.NonNull;

/**
 * @deprecated Use {@link GestureTransitions} class with {@link FromTracker} and
 * {@link IntoTracker} instead.
 */
@SuppressWarnings({ "WeakerAccess", "unused" }) // Class is left for compatibility
@Deprecated
public interface ViewsTracker<ID> {

    int NO_POSITION = -1;

    /**
     * @param id Item ID
     * @return Position for item with given id, or {@link #NO_POSITION} if item was not found.
     */
    int getPositionForId(@NonNull ID id);

    /**
     * @param position List position
     * @return Item's id at given position, or {@code null} if position is invalid.
     */
    ID getIdForPosition(int position);

    /**
     * @param position List position
     * @return View at given position, or {@code null} if there is no known view for given position
     * or position is invalid.
     */
    View getViewForPosition(int position);

}
