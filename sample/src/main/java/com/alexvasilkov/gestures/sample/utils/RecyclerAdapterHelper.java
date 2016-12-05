package com.alexvasilkov.gestures.sample.utils;

import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;

import java.util.List;

public class RecyclerAdapterHelper {

    private RecyclerAdapterHelper() {}

    /**
     * Calls adapter's notify* methods when items are added / removed / moved / updated.
     */
    public static <T> void notifyChanges(RecyclerView.Adapter<?> adapter,
            final List<T> oldList, final List<T> newList) {

        DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() {
                return oldList == null ? 0 : oldList.size();
            }

            @Override
            public int getNewListSize() {
                return newList == null ? 0 : newList.size();
            }

            @Override
            public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                return oldList.get(oldItemPosition).equals(newList.get(newItemPosition));
            }

            @Override
            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                return areItemsTheSame(oldItemPosition, newItemPosition);
            }
        }).dispatchUpdatesTo(adapter);
    }

}
