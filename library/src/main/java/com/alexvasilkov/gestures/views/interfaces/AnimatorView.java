package com.alexvasilkov.gestures.views.interfaces;

import com.alexvasilkov.gestures.animation.ViewPositionAnimator;

/**
 * Common interface for views supporting position animation.
 */
public interface AnimatorView {

    /**
     * @return {@link ViewPositionAnimator} instance to control animation from other view position.
     */
    ViewPositionAnimator getPositionAnimator();

}
