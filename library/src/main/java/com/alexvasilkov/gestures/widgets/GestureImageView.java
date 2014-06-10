package com.alexvasilkov.gestures.widgets;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageView;
import com.alexvasilkov.gestures.GesturesController;
import com.alexvasilkov.gestures.GesturesControllerPagerFix;
import com.alexvasilkov.gestures.State;

/**
 * Gestures controlled ImageView
 */
public class GestureImageView extends ImageView implements GesturesController.OnStateChangedListener {

    private final GesturesControllerPagerFix mController;
    private final Matrix mImageMatrix = new Matrix();

    public GestureImageView(Context context) {
        this(context, null, 0);
    }

    public GestureImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GestureImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        mController = new GesturesControllerPagerFix(context, this);

        setScaleType(ImageView.ScaleType.MATRIX);
    }

    /**
     * Makes scrolling between different {@link GestureImageView}
     * within given {@link android.support.v4.view.ViewPager} smoother.
     */
    public void fixViewPagerScroll(ViewPager pager) {
        mController.fixViewPagerScroll(pager);
    }

    /**
     * Returns {@link com.alexvasilkov.gestures.GesturesController}
     * which is a main engine for {@link GestureImageView}.
     * <p/>
     * Use it to apply settings, modify view state and so on.
     */
    public GesturesController getController() {
        return mController;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return mController.onTouch(this, event);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mController.getSettings().setViewport(w, h);
        mController.updateState();
    }

    @Override
    public void onStateChanged(State state) {
        state.applyTo(mImageMatrix);
        setImageMatrix(mImageMatrix);
    }

    @Override
    public void setImageResource(int resId) {
        setImageDrawable(getContext().getResources().getDrawable(resId));
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        super.setImageDrawable(drawable);
        if (drawable == null) {
            mController.getSettings().setSize(0, 0);
        } else {
            mController.getSettings().setSize(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        }
        mController.resetState();
    }

}
