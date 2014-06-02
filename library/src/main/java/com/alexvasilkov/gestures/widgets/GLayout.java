package com.alexvasilkov.gestures.widgets;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;
import com.alexvasilkov.gestures.GesturesController;
import com.alexvasilkov.gestures.GesturesControllerPagerFix;
import com.alexvasilkov.gestures.State;

public class GLayout extends FrameLayout implements GesturesController.OnStateChangedListener {

    private final GesturesControllerPagerFix mController;

    private final Matrix mMatrix = new Matrix();
    private final Matrix mMatrixInverse = new Matrix();
    private final float[] mPointArray = new float[2];

    public GLayout(Context context) {
        this(context, null, 0);
    }

    public GLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        mController = new GesturesControllerPagerFix(context, this);
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
    public boolean dispatchTouchEvent(MotionEvent e) {
        MotionEvent copy = MotionEvent.obtain(e);
        mPointArray[0] = e.getX();
        mPointArray[1] = e.getY();
        mMatrixInverse.mapPoints(mPointArray);
        copy.setLocation(mPointArray[0], mPointArray[1]);
        try {
            return super.dispatchTouchEvent(copy);
        } finally {
            copy.recycle();
        }
    }

    @Override
    public ViewParent invalidateChildInParent(int[] location, Rect dirty) {
        RectF r = new RectF(dirty.left, dirty.top, dirty.right, dirty.bottom);
        mMatrix.mapRect(r);
        dirty.set((int) r.left, (int) r.top, (int) r.right, (int) r.bottom);
        return super.invalidateChildInParent(location, dirty);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        MotionEvent copy = MotionEvent.obtain(e);
        mPointArray[0] = e.getX();
        mPointArray[1] = e.getY();
        mMatrix.mapPoints(mPointArray);
        copy.setLocation(mPointArray[0], mPointArray[1]);
        try {
            return mController.onTouch(this, copy);
        } finally {
            copy.recycle();
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mController.getSettings().setViewport(w, h);
        mController.updateState();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        View child = getChildCount() == 0 ? null : getChildAt(0);
        if (child != null) mController.getSettings().setSize(child.getWidth(), child.getHeight());
        mController.updateState();
    }

    @Override
    protected void measureChildren(int widthMeasureSpec, int heightMeasureSpec) {
        super.measureChildren(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    public void onStateChanged(State state) {
        state.apply(mMatrix);
        mMatrix.invert(mMatrixInverse);
        Log.d("TEST", "state changed");
        invalidate();
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        canvas.save();
        canvas.concat(mMatrix);
        super.dispatchDraw(canvas);
        canvas.restore();
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        if (getChildCount() != 0) throw new IllegalArgumentException("GFrameLayout can contain only one child");
        super.addView(child, index, params);
    }
}
