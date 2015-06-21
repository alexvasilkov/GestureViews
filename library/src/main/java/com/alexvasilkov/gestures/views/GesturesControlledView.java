package com.alexvasilkov.gestures.views;

import com.alexvasilkov.gestures.GesturesController;

public interface GesturesControlledView {

    /**
     * Returns {@link GesturesController} which is a main engine for all gestures interactions.
     * <p/>
     * Use it to apply settings, access and modify image state and so on.
     */
    GesturesController getController();

}
