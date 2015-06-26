package com.alexvasilkov.gestures.views;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.alexvasilkov.gestures.animation.ViewPositionAnimator;

/**
 * This class extends {@link GestureImageView} and adds smooth position change animation using
 * {@link ViewPositionAnimator} helper class.
 * <p/>
 * To animate from any ImageView to this view and back you may call
 * {@link #enter(ImageView, boolean)} and {@link #exit(boolean)} methods.
 * <p/>
 * You can also listen for current animation state using
 * {@link #setOnImageStateChangeListener(OnImageStateChangeListener)}.
 */
public class GestureImageViewFull extends GestureImageView {

    private final ViewPositionAnimator mAnimator = new ViewPositionAnimator();
    private boolean mIsOpen;

    private OnImageStateChangeListener mListener;

    private ImageView mImageFrom;


    public GestureImageViewFull(Context context) {
        this(context, null);
    }

    public GestureImageViewFull(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GestureImageViewFull(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setVisibility(INVISIBLE);

        mAnimator.setOnPositionChangeListener(new ViewPositionAnimator.OnPositionChangeListener() {
            @Override
            public void onPositionChanged(float state, boolean isFinishing) {
                GestureImageViewFull.this.onPositionChanged(state, isFinishing);
            }
        });
    }

    public void setOnImageStateChangeListener(@Nullable OnImageStateChangeListener listener) {
        mListener = listener;
    }

    /**
     * Animates 'enter' transition.
     */
    public void enter(@NonNull ImageView from, boolean withAnimation) {
        mImageFrom = from;
        mIsOpen = true;
        mAnimator.init(from, this);
        mAnimator.enter(withAnimation);
    }

    /**
     * Animates 'exit' transition.
     */
    public void exit(boolean withAnimation) {
        mAnimator.exit(withAnimation);
    }

    /**
     * Should be called whenever initial view is changed but still represents the same image.
     */
    public void update(ImageView from) {
        mAnimator.update(from);
        if (mImageFrom != null) mImageFrom.setVisibility(VISIBLE); // Restoring visibility

        mImageFrom = from;
        if (mImageFrom != null) mImageFrom.setVisibility(INVISIBLE);
    }

    /**
     * Whether {@link #exit(boolean)} method was already called.
     */
    public boolean isOpen() {
        return mIsOpen;
    }

    public long getDuration() {
        return mAnimator.getDuration();
    }

    public void setDuration(long duration) {
        mAnimator.setDuration(duration);
    }

    protected void onPositionChanged(float state, boolean isFinishing) {
        if (mIsOpen && isFinishing) mIsOpen = false;

        setVisibility(state == 0f ? INVISIBLE : VISIBLE);
        if (mImageFrom != null) mImageFrom.setVisibility(state == 0f ? VISIBLE : INVISIBLE);

        if (state == 0f && isFinishing) {
            if (getDrawable() != null) setImageDrawable(null);
            mImageFrom = null; // Not needed anymore
        }

        if (mListener != null) mListener.onImageStateChanged(state, isFinishing);
    }


    public interface OnImageStateChangeListener {

        void onImageStateChanged(float state, boolean isFinishing);

    }

}
