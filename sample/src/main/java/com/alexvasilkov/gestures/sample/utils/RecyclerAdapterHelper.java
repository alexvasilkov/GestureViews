package com.alexvasilkov.gestures.sample.utils;

import android.support.v7.widget.RecyclerView;

import java.util.Collections;
import java.util.List;

public class RecyclerAdapterHelper {

    private RecyclerAdapterHelper() {}

    /**
     * Calls adapter's notify* method when items are added / removed / updated.
     * Only correctly works if items order is unchanged.
     */
    public static <T> void notifyChanges(RecyclerView.Adapter<?> adapter,
            List<T> oldList, List<T> newList,
            boolean notifyCommons) {

        if (oldList == null) {
            oldList = Collections.emptyList();
        }
        if (newList == null) {
            newList = Collections.emptyList();
        }

        // Notifying about changes to animate items
        int lastNew = 0;
        int current = 0;
        boolean hasCommonItems = false;

        for (T old : oldList) {
            int newPos = newList.indexOf(old);
            if (newPos == -1) {
                adapter.notifyItemRemoved(current);
            } else {
                hasCommonItems = true;

                for (int i = lastNew; i < newPos; i++) {
                    adapter.notifyItemInserted(current);
                    current++;
                }

                if (notifyCommons) {
                    adapter.notifyItemChanged(current);
                }
                current++;

                lastNew = newPos + 1;
            }
        }

        for (int i = lastNew, size = newList.size(); i < size; i++) {
            adapter.notifyItemInserted(current);
            current++;
        }

        // If no common items, than no animation is needed
        if (!hasCommonItems) {
            adapter.notifyDataSetChanged();
        }
    }

}
