package com.alexvasilkov.gestures.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
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
import com.alexvasilkov.gestures.internal.DebugOverlay;
import com.alexvasilkov.gestures.internal.GestureDebug;
import com.alexvasilkov.gestures.views.interfaces.AnimatorView;
import com.alexvasilkov.gestures.views.interfaces.GestureView;

import androidx.annotation.NonNull;

/**
 * {@link FrameLayout} implementation controlled by {@link GestureController}
 * ({@link #getController()}).
 * <p>
 * View position can be animated with {@link ViewPositionAnimator}
 * ({@link #getPositionAnimator()}).
 * <p>
 * Note: only one children is eligible in this layout.
 */
public class GestureFrameLayout extends FrameLayout implements GestureView, AnimatorView {

    private final GestureControllerForPager controller;

    private ViewPositionAnimator positionAnimator;

    private final Matrix matrix = new Matrix();
    private final Matrix matrixInverse = new Matrix();

    private final RectF tmpFloatRect = new RectF();
    private final float[] tmpPointArray = new float[2];

    private MotionEvent currentMotionEvent;

    public GestureFrameLayout(Context context) {
        this(context, null, 0);
    }

    public GestureFrameLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GestureFrameLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        controller = new GestureControllerForPager(this);
        controller.getSettings().initFromAttributes(context, attrs);
        controller.addOnStateChangeListener(new GestureController.OnStateChangeListener() {
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
        return controller;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ViewPositionAnimator getPositionAnimator() {
        if (positionAnimator == null) {
            positionAnimator = new ViewPositionAnimator(this);
        }
        return positionAnimator;
    }

    @Override
    public boolean dispatchTouchEvent(@NonNull MotionEvent event) {
        currentMotionEvent = event;
        // We should remap given event back to original coordinates
        // so that children can correctly respond to it
        MotionEvent invertedEvent = applyMatrix(event, matrixInverse);
        try {
            return super.dispatchTouchEvent(invertedEvent);
        } finally {
            invertedEvent.recycle();
        }
    }

    // It seems to be fine to use this method instead of suggested onDescendantInvalidated(...)
    @SuppressWarnings("deprecation")
    @Override
    public ViewParent invalidateChildInParent(int[] location, @NonNull Rect dirty) {
        // Invalidating correct rectangle
        applyMatrix(dirty, matrix);
        return super.invalidateChildInParent(location, dirty);
    }

    @Override
    public void requestDisallowInterceptTouchEvent(boolean disallowIntercept) {
        super.requestDisallowInterceptTouchEvent(disallowIntercept);

        if (disallowIntercept) {
            // We should pass "cancel" touch event to make sure controller does not expect
            // any events anymore.
            MotionEvent cancel = MotionEvent.obtain(currentMotionEvent);
            cancel.setAction(MotionEvent.ACTION_CANCEL);
            controller.onInterceptTouch(this, cancel);
            cancel.recycle();
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        // Passing original event to controller
        return controller.onInterceptTouch(this, currentMotionEvent);
    }

    @SuppressLint("ClickableViewAccessibility") // performClick() will be called by controller
    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        // Passing original event to controller
        return controller.onTouch(this, currentMotionEvent);
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
        super.onSizeChanged(width, height, oldWidth, oldHeight);

        controller.getSettings().setViewport(width - getPaddingLeft() - getPaddingRight(),
                height - getPaddingTop() - getPaddingBottom());
        controller.updateState();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        View child = getChildCount() == 0 ? null : getChildAt(0);
        if (child != null) {
            controller.getSettings().setImage(child.getMeasuredWidth(), child.getMeasuredHeight());
            controller.updateState();
        }
    }

    @Override
    protected void measureChildWithMargins(View child, int parentWidthMeasureSpec, int widthUsed,
            int parentHeightMeasureSpec, int heightUsed) {
        final MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();

        final int extraW = getPaddingLeft() + getPaddingRight()
                + lp.leftMargin + lp.rightMargin + widthUsed;
        final int extraH = getPaddingTop() + getPaddingBottom()
                + lp.topMargin + lp.bottomMargin + heightUsed;

        child.measure(getChildMeasureSpecFixed(parentWidthMeasureSpec, extraW, lp.width),
                getChildMeasureSpecFixed(parentHeightMeasureSpec, extraH, lp.height));
    }

    protected void applyState(State state) {
        state.get(matrix);
        matrix.invert(matrixInverse);
        invalidate();
    }

    @Override
    protected void dispatchDraw(@NonNull Canvas canvas) {
        canvas.save();
        canvas.concat(matrix);
        super.dispatchDraw(canvas);
        canvas.restore();

        if (GestureDebug.isDrawDebugOverlay()) {
            DebugOverlay.drawDebug(this, canvas);
        }
    }

    @Override
    public void addView(@NonNull View child, int index, @NonNull ViewGroup.LayoutParams params) {
        if (getChildCount() != 0) {
            throw new IllegalArgumentException("GestureFrameLayout can contain only one child");
        }
        super.addView(child, index, params);
    }


    private MotionEvent applyMatrix(MotionEvent event, Matrix matrix) {
        tmpPointArray[0] = event.getX();
        tmpPointArray[1] = event.getY();
        matrix.mapPoints(tmpPointArray);

        MotionEvent copy = MotionEvent.obtain(event);
        copy.setLocation(tmpPointArray[0], tmpPointArray[1]);
        return copy;
    }

    private void applyMatrix(Rect rect, Matrix matrix) {
        tmpFloatRect.set(rect.left, rect.top, rect.right, rect.bottom);
        matrix.mapRect(tmpFloatRect);
        rect.set(Math.round(tmpFloatRect.left), Math.round(tmpFloatRect.top),
                Math.round(tmpFloatRect.right), Math.round(tmpFloatRect.bottom));
    }


    protected static int getChildMeasureSpecFixed(int spec, int extra, int childDimension) {
        if (childDimension == ViewGroup.LayoutParams.WRAP_CONTENT) {
            return MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(spec), MeasureSpec.UNSPECIFIED);
        } else {
            return getChildMeasureSpec(spec, extra, childDimension);
        }
    }

}
