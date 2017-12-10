package com.alexvasilkov.gestures.transition.tracker;

public interface IntoTracker<ID> extends AbstractTracker<ID> {

    /**
     * @param position List position
     * @return Item's id at given position, or {@code null} if position is invalid.
     * Note, that only one id per position should be possible for "To" view.
     */
    ID getIdByPosition(int position);

}
