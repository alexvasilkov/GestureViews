package com.alexvasilkov.gestures.sample.ui.ex.single;

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
        setContentView(R.layout.single_image_round_screen);
    }

}
