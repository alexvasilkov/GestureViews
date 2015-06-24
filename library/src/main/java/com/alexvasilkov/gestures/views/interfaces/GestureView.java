package com.alexvasilkov.gestures.views.interfaces;

import com.alexvasilkov.gestures.GesturesController;

/**
 * Common interface for all Gesture* views.
 * <p/>
 * All classes implementing this interface should be descendants of {@link android.view.View}.
 */
public interface GestureView {

    /**
     * Returns {@link GesturesController} which is a main engine for all gestures interactions.
     * <p/>
     * Use it to apply settings, access and modify image state and so on.
     */
    GesturesController getController();

}
