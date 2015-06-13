package com.alexvasilkov.gestures.sample.animation;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Intent;
import android.graphics.RectF;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import com.alexvasilkov.gestures.GesturesController;
import com.alexvasilkov.gestures.Settings;
import com.alexvasilkov.gestures.State;
import com.alexvasilkov.gestures.StateController;
import com.alexvasilkov.gestures.widgets.GestureImageView;

import java.io.Serializable;

public class Helper {

    private static final String EXTRA_INFO = "com.alexvasilkov.gestures.INFO";
    private static final long DEFAULT_DURATION = 250L;

    private final Activity mActivity;
    private final Info mInfo;
    private final GestureImageView mView;
    private ViewInfo mViewInfo;

    private AnimationUpdateListener mListener;
    private long mDuration = DEFAULT_DURATION;

    private ValueAnimator mAnimator;
    private float mAnimationState;
    private final State mInitialState = new State(), mFinalState = new State();
    private final RectF mInitialRect = new RectF(), mFinalRect = new RectF();
    private final RectF mClippingRect = new RectF();
    private boolean mIsFinishing;

    private boolean mStateRestrictBounds;

    public Helper(Activity activity, GestureImageView imageView) {
        mActivity = activity;

        mInfo = (Info) activity.getIntent().getSerializableExtra(EXTRA_INFO);
        if (mInfo == null) throw new NullPointerException("Image info is not found in intent " +
                "extras, ensure you started activity using Starter.start(..) method");

        mView = imageView;

        mView.getController().addOnStateChangedListener(
                new GesturesController.OnStateChangedListener() {
                    @Override
                    public void onStateChanged(State state) {
                        // No-op
                    }

                    @Override
                    public void onStateReset(State oldState, State newState) {
                        onImageStateReset(newState);
                    }
                });

        mView.getViewTreeObserver().addOnPreDrawListener(
                new ViewTreeObserver.OnPreDrawListener() {
                    @Override
                    public boolean onPreDraw() {
                        mView.getViewTreeObserver().removeOnPreDrawListener(this);
                        mViewInfo = new ViewInfo(mView, null);
                        onImageStateReset(mView.getController().getState());
                        return true;
                    }
                });
    }

    @SuppressWarnings("unchecked")
    public <T extends Serializable> T getItem() {
        return (T) mInfo.item.data;
    }

    public Helper setAnimationUpdateListener(AnimationUpdateListener listener) {
        mListener = listener;
        return this;
    }

    public long getDuration() {
        return mDuration;
    }

    public Helper setDuration(long duration) {
        this.mDuration = duration;
        return this;
    }

    public void enter(Bundle savedState) {
        if (savedState != null) {
            // Animation is not needed
            mAnimationState = 1f;
            // Applying current state, listener will be notified
            onImageStateReset(mView.getController().getState());
            return;
        }

        startAnimation(true);
    }

    public void exit() {
        if (mIsFinishing) return;

        mIsFinishing = true;
        // Ensure we are animating from current state (but only if enter animation is finished)
        if (mAnimationState == 1f) onImageStateReset(mView.getController().getState());
        startAnimation(false);
    }

    private void onImageStateReset(State newState) {
        mFinalState.set(newState);

        if (mViewInfo != null) {
            ViewInfo initialInfo = mInfo.item;
            float x = initialInfo.imageLeft - mViewInfo.viewLeft;
            float y = initialInfo.imageTop - mViewInfo.viewTop;
            float w = mView.getController().getSettings().getViewW();
            float h = mView.getController().getSettings().getViewH();
            float zoom = h == 0f ? 1f : initialInfo.imageHeight / h;
            mInitialState.set(x, y, zoom, 0f);

            float left = initialInfo.viewLeft - mViewInfo.viewLeft + mView.getPaddingLeft();
            float top = initialInfo.viewTop - mViewInfo.viewTop + mView.getPaddingTop();
            mInitialRect.set(left, top,
                    left + initialInfo.viewWidth, top + initialInfo.viewHeight);

            left = newState.getX() + mView.getPaddingLeft();
            top = newState.getY() + mView.getPaddingTop();
            mFinalRect.set(left, top, left + w * newState.getZoom(), top + h * newState.getZoom());
        } else {
            mInitialState.set(mFinalState);

            mInitialRect.set(0f, 0f, 0f, 0f);
            mFinalRect.set(0f, 0f, 0f, 0f);
        }

        applyAnimationState();
    }

    private void startAnimation(final boolean isEnter) {
        if (mAnimator != null) mAnimator.cancel();

        float durationFraction = isEnter ? 1f - mAnimationState : mAnimationState;

        mAnimator = ValueAnimator.ofFloat(mAnimationState, isEnter ? 1f : 0f);
        mAnimator.setDuration((long) (mDuration * durationFraction));

        mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(@NonNull ValueAnimator valueAnimator) {
                mAnimationState = (Float) valueAnimator.getAnimatedValue();
                applyAnimationState();
            }
        });

        mAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(@NonNull Animator animation) {
                Settings settings = mView.getController().getSettings();
                // Saving settings states
                mStateRestrictBounds = settings.isRestrictBounds();
                // Disabling bounds restrictions & gestures
                settings.setRestrictBounds(false).disableGestures();
            }

            @Override
            public void onAnimationEnd(@NonNull Animator animation) {
                if (isEnter) {
                    // Restoring original settings
                    mView.getController().getSettings()
                            .setRestrictBounds(mStateRestrictBounds)
                            .enableGestures();
                    mView.getController().updateState();
                    mView.clipLayout(null); // Do not clip
                } else {
                    mActivity.finish();
                    mActivity.overridePendingTransition(0, 0);
                }
            }
        });

        mAnimator.start();
    }

    private void applyAnimationState() {
        State state = mView.getController().getState();
        StateController.interpolate(state, mInitialState, mFinalState, mAnimationState);
        mView.getController().updateState();

        interpolate(mClippingRect, mInitialRect, mFinalRect, mAnimationState);
        mView.clipLayout(mAnimationState == 1f ? null : mClippingRect);

        if (mListener != null) mListener.onAnimationUpdate(mAnimationState);
    }

    /**
     * Interpolates from start rect to the end rect by given factor (from 0 to 1),
     * storing result into out rect.
     */
    private static void interpolate(RectF out, RectF start, RectF end, float factor) {
        float left = StateController.interpolate(start.left, end.left, factor);
        float top = StateController.interpolate(start.top, end.top, factor);
        float right = StateController.interpolate(start.right, end.right, factor);
        float bottom = StateController.interpolate(start.bottom, end.bottom, factor);
        out.set(left, top, right, bottom);
    }

    private static class Info implements Serializable {
        private static final long serialVersionUID = -9045545135039975677L;

        ViewInfo item;
    }


    public static class Starter {

        private final Info info = new Info();

        public Starter from(ImageView view, Serializable data) {
            info.item = new ViewInfo(view, data);
            return this;
        }

        public void start(Activity activity, Intent intent) {
            if (info.item == null) throw new NullPointerException("You should set primary item " +
                    "with from(..) method before starting next activity");

            intent.putExtra(EXTRA_INFO, info);
            activity.startActivity(intent);
            // Skipping animation
            activity.overridePendingTransition(0, 0);
        }

    }

    public interface AnimationUpdateListener {
        void onAnimationUpdate(float animationState);
    }

}
