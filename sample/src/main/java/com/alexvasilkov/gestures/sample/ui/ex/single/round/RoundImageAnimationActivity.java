package com.alexvasilkov.gestures.sample.ui.ex.single.round;

import com.alexvasilkov.gestures.commons.circle.CircleGestureImageView;
import com.alexvasilkov.gestures.commons.circle.CircleImageView;
import com.alexvasilkov.gestures.sample.R;
import com.alexvasilkov.gestures.sample.ui.ex.single.SingleImageAnimationActivity;

/**
 * Same as {@link SingleImageAnimationActivity} example but shows how to animate rounded image
 * using {@link CircleImageView} and {@link CircleGestureImageView}.
 */
public class RoundImageAnimationActivity extends SingleImageAnimationActivity {

    @Override
    protected void initContentView() {
        setContentView(R.layout.single_image_round_screen);
    }

}
