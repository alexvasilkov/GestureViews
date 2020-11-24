package com.alexvasilkov.gestures.sample.ex.other.scene;

import com.alexvasilkov.gestures.State;

class Item {

    private final int imageId;
    private final State state = new State();

    Item(int imageId) {
        this.imageId = imageId;
    }

    int getImageId() {
        return imageId;
    }

    State getState() {
        return state;
    }

}
