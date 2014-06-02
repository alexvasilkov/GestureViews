package com.alexvasilkov.gestures.widgets;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageView;
import com.alexvasilkov.gestures.GesturesControllerPagerFix;
import com.alexvasilkov.gestures.State;
import com.alexvasilkov.gestures.GesturesController;

public class GImageView extends ImageView implements GesturesController.OnStateChangedListener {

    private final GesturesControllerPagerFix mController;
    private final Matrix mImageMatrix = new Matrix();

    public GImageView(Context context) {
        this(context, null, 0);
    }

    public GImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        mController = new GesturesControllerPagerFix(context, this);

        setScaleType(ImageView.ScaleType.MATRIX);
    }

    public void setOnGestureListener(GesturesController.OnGestureListener listener) {
        mController.setOnGesturesListener(listener);
    }

    public void fixViewPagerScroll(ViewPager pager) {
        mController.fixViewPagerScroll(pager);
    }

    public GesturesControllerPagerFix getController() {
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
        state.apply(mImageMatrix);
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
