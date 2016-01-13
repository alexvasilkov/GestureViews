package com.alexvasilkov.gestures.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;

import com.alexvasilkov.gestures.GestureController;
import com.alexvasilkov.gestures.GestureControllerForPager;
import com.alexvasilkov.gestures.State;
import com.alexvasilkov.gestures.animation.ViewPositionAnimator;
import com.alexvasilkov.gestures.views.interfaces.AnimatorView;
import com.alexvasilkov.gestures.views.interfaces.GestureView;

/**
 * {@link FrameLayout} implementation controlled by {@link GestureController}
 * ({@link #getController()}).
 * <p/>
 * View position can be animated with {@link ViewPositionAnimator}
 * ({@link #getPositionAnimator()}).
 * <p/>
 * Note: only one children is eligible in this layout.
 */
public class GestureFrameLayout extends FrameLayout implements GestureView, AnimatorView {

    private final GestureControllerForPager mController;

    private ViewPositionAnimator mPositionAnimator;

    private final Matrix mMatrix = new Matrix();
    private final Matrix mMatrixInverse = new Matrix();

    private final RectF mTmpFloatRect = new RectF();
    private final float[] mTmpPointArray = new float[2];

    private MotionEvent mCurrentMotionEvent;

    public GestureFrameLayout(Context context) {
        this(context, null, 0);
    }

    public GestureFrameLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GestureFrameLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        mController = new GestureControllerForPager(this);
        mController.addOnStateChangeListener(new GestureController.OnStateChangeListener() {
            @Override
            public void onStateChanged(State state) {
                applyState(state);
            }

            @Override
            public void onStateReset(State oldState, State newState) {
                applyState(newState);
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GestureControllerForPager getController() {
        return mController;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ViewPositionAnimator getPositionAnimator() {
        if (mPositionAnimator == null) {
            mPositionAnimator = new ViewPositionAnimator(this);
        }
        return mPositionAnimator;
    }

    @Override
    public boolean dispatchTouchEvent(@NonNull MotionEvent event) {
        mCurrentMotionEvent = event;
        // We should remap given event back to original coordinates
        // so children can correctly respond to it
        MotionEvent invertedEvent = applyMatrix(event, mMatrixInverse);
        try {
            return super.dispatchTouchEvent(invertedEvent);
        } finally {
            invertedEvent.recycle();
        }
    }

    @Override
    public ViewParent invalidateChildInParent(int[] location, @NonNull Rect dirty) {
        // Invalidating correct rectangle
        applyMatrix(dirty, mMatrix);
        return super.invalidateChildInParent(location, dirty);
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        // Passing original event to controller
        return mController.onTouch(this, mCurrentMotionEvent);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        mController.getSettings().setViewport(w - getPaddingLeft() - getPaddingRight(),
                h - getPaddingTop() - getPaddingBottom());
        mController.updateState();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        View child = getChildCount() == 0 ? null : getChildAt(0);
        if (child != null) {
            mController.getSettings().setImage(child.getMeasuredWidth(), child.getMeasuredHeight());
            mController.updateState();
        }
    }

    @Override
    protected void measureChildWithMargins(View child, int parentWidthMeasureSpec, int widthUsed,
            int parentHeightMeasureSpec, int heightUsed) {
        final MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();

        final int extraW = getPaddingLeft() + getPaddingRight() +
                lp.leftMargin + lp.rightMargin + widthUsed;
        final int extraH = getPaddingTop() + getPaddingBottom() +
                lp.topMargin + lp.bottomMargin + heightUsed;

        child.measure(getChildMeasureSpecFixed(parentWidthMeasureSpec, extraW, lp.width),
                getChildMeasureSpecFixed(parentHeightMeasureSpec, extraH, lp.height));
    }

    protected void applyState(State state) {
        state.get(mMatrix);
        mMatrix.invert(mMatrixInverse);
        invalidate();
    }

    @Override
    protected void dispatchDraw(@NonNull Canvas canvas) {
        canvas.save();
        canvas.concat(mMatrix);
        super.dispatchDraw(canvas);
        canvas.restore();
    }

    @Override
    public void addView(@NonNull View child, int index, @NonNull ViewGroup.LayoutParams params) {
        if (getChildCount() != 0) {
            throw new IllegalArgumentException("GestureFrameLayout can contain only one child");
        }
        super.addView(child, index, params);
    }


    private MotionEvent applyMatrix(MotionEvent event, Matrix matrix) {
        MotionEvent copy = MotionEvent.obtain(event);
        mTmpPointArray[0] = event.getX();
        mTmpPointArray[1] = event.getY();
        matrix.mapPoints(mTmpPointArray);
        copy.setLocation(mTmpPointArray[0], mTmpPointArray[1]);
        return copy;
    }

    private void applyMatrix(Rect rect, Matrix matrix) {
        mTmpFloatRect.set(rect.left, rect.top, rect.right, rect.bottom);
        matrix.mapRect(mTmpFloatRect);
        rect.set(Math.round(mTmpFloatRect.left), Math.round(mTmpFloatRect.top),
                Math.round(mTmpFloatRect.right), Math.round(mTmpFloatRect.bottom));
    }


    protected static int getChildMeasureSpecFixed(int spec, int extra, int childDimension) {
        if (childDimension == ViewGroup.LayoutParams.WRAP_CONTENT) {
            return MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(spec), MeasureSpec.UNSPECIFIED);
        } else {
            return getChildMeasureSpec(spec, extra, childDimension);
        }
    }

}
