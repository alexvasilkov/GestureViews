package com.alexvasilkov.gestures.sample.activities;

import android.app.Activity;
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
import com.alexvasilkov.gestures.sample.utils.GestureSettingsMenu;
import com.alexvasilkov.gestures.sample.utils.glide.GlideHelper;
import com.alexvasilkov.gestures.transition.GestureTransitions;
import com.alexvasilkov.gestures.transition.ViewsTransitionAnimator;

public class Ex4SingleImageAnimationActivity extends BaseActivity {

    private ViewHolder views;
    private ViewsTransitionAnimator animator;
    private GestureSettingsMenu settingsMenu;

    @InstanceState
    private boolean isCircleImage = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_ex4_single_image);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        views = new ViewHolder(this);

        settingsMenu = new GestureSettingsMenu();
        settingsMenu.onRestoreInstanceState(savedInstanceState);

        initViews();

        setCircleImage(isCircleImage);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        settingsMenu.onSaveInstanceState(outState);
        super.onSaveInstanceState(outState);
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

        return settingsMenu.onCreateOptionsMenu(menu);
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
        } else if (settingsMenu.onOptionsItemSelected(item)) {
            supportInvalidateOptionsMenu();
            settingsMenu.onSetupGestureView(views.fullImage); // Updating gesture image settings
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }


    private void initViews() {
        GlideHelper.loadResource(R.drawable.painting_01, views.image);

        views.image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(@NonNull View view) {
                openFullImage();
            }
        });

        animator = GestureTransitions.from(views.image).into(views.fullImage);

        animator.addPositionUpdateListener(new PositionUpdateListener() {
            @Override
            public void onPositionUpdate(float position, boolean isLeaving) {
                views.fullImage.setVisibility(position == 0f && isLeaving
                        ? View.INVISIBLE : View.VISIBLE);
            }
        });
    }

    private void openFullImage() {
        // Setting image drawable from 'from' view to 'to' to prevent flickering
        if (views.fullImage.getDrawable() == null) {
            views.fullImage.setImageDrawable(views.image.getDrawable());
        }

        settingsMenu.onSetupGestureView(views.fullImage); // Updating gesture image settings
        views.fullImage.getController().resetState();

        animator.enterSingle(true);
    }

    private void setCircleImage(boolean isCircle) {
        isCircleImage = isCircle;
        views.image.setCircle(isCircle);
        views.fullImage.setCircle(isCircle);
    }


    private class ViewHolder {
        final CircleImageView image;
        final CircleGestureImageView fullImage;

        ViewHolder(Activity activity) {
            image = Views.find(activity, R.id.single_image);
            fullImage = Views.find(activity, R.id.single_image_full);
        }
    }

}
