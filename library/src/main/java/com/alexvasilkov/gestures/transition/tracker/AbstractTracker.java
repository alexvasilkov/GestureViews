package com.alexvasilkov.gestures.transition.tracker;

import android.view.View;

import androidx.annotation.NonNull;

interface AbstractTracker<ID> {

    int NO_POSITION = -1;

    /**
     * @param id Item ID
     * @return Position of list item which contains element with given ID,
     * or {@link #NO_POSITION} if element with given ID is not part of the list.<br>
     * Note, that there can be several elements inside single list item, but we only need to know
     * list item position, so we can scroll to it if required.
     */
    int getPositionById(@NonNull ID id);

    /**
     * @param id Item ID
     * @return View for given element ID, or {@code null} if view is not found.<br>
     * Note, that it is safe to return {@code null} if view is not found on the screen, list view
     * will be automatically scrolled to needed position (as returned by
     * {@link #getPositionById(Object)}) and this method will be called again.
     */
    View getViewById(@NonNull ID id);

}
