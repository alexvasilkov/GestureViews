package com.alexvasilkov.gestures.sample.ui.ex4;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.alexvasilkov.android.commons.state.InstanceState;
import com.alexvasilkov.android.commons.ui.Views;
import com.alexvasilkov.gestures.animation.ViewPositionAnimator.PositionUpdateListener;
import com.alexvasilkov.gestures.commons.circle.CircleGestureImageView;
import com.alexvasilkov.gestures.commons.circle.CircleImageView;
import com.alexvasilkov.gestures.sample.R;
import com.alexvasilkov.gestures.sample.ui.base.BaseExampleActivity;
import com.alexvasilkov.gestures.sample.utils.glide.GlideHelper;
import com.alexvasilkov.gestures.transition.GestureTransitions;
import com.alexvasilkov.gestures.transition.ViewsTransitionAnimator;

/**
 * This example demonstrates animation from small to full image mode.<br/>
 * It also demonstrates usage of {@link CircleImageView} and {@link CircleGestureImageView} to
 * perform animation from small rounded image into a full rectangular image.
 */
public class SingleImageAnimationActivity extends BaseExampleActivity {

    private CircleImageView image;
    private CircleGestureImageView fullImage;
    private ViewsTransitionAnimator animator;

    @InstanceState
    private boolean isCircleImage = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.ex4_screen);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        image = Views.find(this, R.id.single_image);
        fullImage = Views.find(this, R.id.single_image_full);

        initViews();

        setCircleImage(isCircleImage);
    }

    @Override
    public void onBackPressed() {
        if (!animator.isLeaving()) {
            animator.exit(true);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem shape;
        if (isCircleImage) {
            shape = menu.add(Menu.NONE, R.id.menu_crop_square, 0, R.string.menu_crop_square);
            shape.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            shape.setIcon(R.drawable.ic_crop_square_white_24dp);
        } else {
            shape = menu.add(Menu.NONE, R.id.menu_crop_circle, 0, R.string.menu_crop_circle);
            shape.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            shape.setIcon(R.drawable.ic_radio_button_unchecked_white_24dp);
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_crop_square) {
            setCircleImage(false);
            supportInvalidateOptionsMenu();
            return true;
        } else if (item.getItemId() == R.id.menu_crop_circle) {
            setCircleImage(true);
            supportInvalidateOptionsMenu();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onSettingsChanged() {
        // Updating gesture image settings
        getSettingsListener().onSetupGestureView(fullImage);
    }

    private void initViews() {
        GlideHelper.loadResource(R.drawable.painting_01, image);

        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(@NonNull View view) {
                openFullImage();
            }
        });

        animator = GestureTransitions.from(image).into(fullImage);

        animator.addPositionUpdateListener(new PositionUpdateListener() {
            @Override
            public void onPositionUpdate(float position, boolean isLeaving) {
                fullImage.setVisibility(position == 0f && isLeaving
                        ? View.INVISIBLE : View.VISIBLE);
            }
        });
    }

    private void openFullImage() {
        // Setting image drawable from 'from' view to 'to' to prevent flickering
        if (fullImage.getDrawable() == null) {
            fullImage.setImageDrawable(image.getDrawable());
        }

        // Updating gesture image settings
        getSettingsListener().onSetupGestureView(fullImage);
        fullImage.getController().resetState();

        animator.enterSingle(true);
    }

    private void setCircleImage(boolean isCircle) {
        isCircleImage = isCircle;
        image.setCircle(isCircle);
        fullImage.setCircle(isCircle);
    }

}
