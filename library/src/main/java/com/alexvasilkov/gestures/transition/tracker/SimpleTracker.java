package com.alexvasilkov.gestures.transition.tracker;

import android.view.View;

import androidx.annotation.NonNull;

/**
 * Class implementing both {@link FromTracker} and {@link IntoTracker} assuming that positions will
 * be used as items ids.
 * <p>
 * Note, that it will only work correctly if both "from" and "to" lists are the same and there will
 * be no changes to them which will change existing items' positions. So you can't remove items, or
 * add items into the middle, but you can add new items to the end of the list, as long as both
 * lists are updated simultaneously.
 * <p>
 * If you need to handle more advanced cases you should manually implement {@link FromTracker} and
 * {@link IntoTracker}, and use items ids instead of their positions.
 */
public abstract class SimpleTracker implements FromTracker<Integer>, IntoTracker<Integer> {

    @Override
    public Integer getIdByPosition(int position) {
        return position;
    }

    @Override
    public int getPositionById(@NonNull Integer id) {
        return id;
    }

    @Override
    public View getViewById(@NonNull Integer id) {
        return getViewAt(id);
    }

    protected abstract View getViewAt(int position);

}
