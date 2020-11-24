package com.alexvasilkov.gestures.sample.ex.animations;

import com.alexvasilkov.gestures.commons.circle.CircleGestureImageView;
import com.alexvasilkov.gestures.commons.circle.CircleImageView;
import com.alexvasilkov.gestures.sample.R;

/**
 * Same as {@link ImageAnimationActivity} example but shows how to animate rounded image
 * using {@link CircleImageView} and {@link CircleGestureImageView}.
 */
public class RoundImageAnimationActivity extends ImageAnimationActivity {

    @Override
    protected void initContentView() {
        setContentView(R.layout.image_animation_round_screen);
        setTitle(R.string.example_image_animation_circular);
    }

}
