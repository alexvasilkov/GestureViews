package com.alexvasilkov.gestures.views.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.support.annotation.ColorInt;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import com.alexvasilkov.gestures.Settings;
import com.alexvasilkov.gestures.StateController;
import com.alexvasilkov.gestures.internal.AnimationEngine;
import com.alexvasilkov.gestures.internal.FloatScroller;
import com.alexvasilkov.gestures.internal.MovementBounds;
import com.alexvasilkov.gestures.views.GestureImageView;

/**
 * View to draw movement area above {@link GestureImageView}, useful when implementing cropping.
 * <p/>
 * To use this view you should set corresponding {@link Settings} with
 * {@link #setSettings(Settings)} method. Then whenever movement area is changed
 * (see {@link Settings#setMovementArea(int, int)}) you will need to call {@link #update(boolean)}
 * method to apply changes.
 * <p/>
 * You may also use rounded corners with {@link #setRounded(boolean)} method, changes between
 * rounded and non-rounded mode can optionally be animated.
 */
public class FinderView extends View {

    public static final int DEFAULT_BACK_COLOR = Color.argb(128, 0, 0, 0);
    public static final int DEFAULT_BORDER_COLOR = Color.WHITE;
    public static final int DEFAULT_BORDER_WIDTH = 2;

    private final RectF mRect = new RectF();
    private float mRounding = 0f;

    private final RectF mStrokeRect = new RectF();

    private final RectF mStartRect = new RectF(), mEndRect = new RectF();
    private float mStartRounding, mEndRounding;

    private final Paint mPaintStroke = new Paint(), mPaintClear = new Paint();

    private final FloatScroller mStateScroller = new FloatScroller();
    private final AnimationEngine mAnimationEngine = new LocalAnimationEngine();

    private int mBackColor;
    private Settings mSettings;

    public FinderView(Context context) {
        this(context, null);
    }

    public FinderView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mPaintClear.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        mPaintClear.setAntiAlias(true);

        mPaintStroke.setStyle(Paint.Style.STROKE);
        mPaintStroke.setAntiAlias(true);

        // Default values
        setBackColor(DEFAULT_BACK_COLOR);
        setBorderColor(DEFAULT_BORDER_COLOR);
        setBorderWidth(TypedValue.COMPLEX_UNIT_DIP, DEFAULT_BORDER_WIDTH);
    }

    /**
     * Sets background color. Default value is {@link #DEFAULT_BACK_COLOR}.
     */
    public void setBackColor(@ColorInt int color) {
        mBackColor = color;
    }

    /**
     * Sets borders color. Default value is {@link #DEFAULT_BORDER_COLOR}.
     */
    public void setBorderColor(@ColorInt int color) {
        mPaintStroke.setColor(color);
    }

    /**
     * Sets borders width in pixels. Default value is {@link #DEFAULT_BORDER_WIDTH} dp.
     */
    public void setBorderWidth(float width) {
        mPaintStroke.setStrokeWidth(width);
    }

    /**
     * Sets borders width in particular units (see {@link TypedValue}.COMPLEX_UNIT_* constants).
     * Default value is {@link #DEFAULT_BORDER_WIDTH} dp.
     */
    public void setBorderWidth(int unit, float width) {
        setBorderWidth(TypedValue.applyDimension(unit, width, getResources().getDisplayMetrics()));
    }

    /**
     * Sets settings to get movement area from.
     */
    public void setSettings(Settings settings) {
        mSettings = settings;
        update(false);
    }

    /**
     * Whether to round bounds' corners or not. Method {@link #update(boolean)} should be called
     * to apply this setting with optional animation.
     */
    public void setRounded(boolean rounded) {
        mStartRounding = mRounding;
        mEndRounding = rounded ? 1f : 0f;
    }

    /**
     * Applies area size, area position and corners rounding with optional animation.
     */
    public void update(boolean animate) {
        if (mSettings != null && getWidth() > 0 && getHeight() > 0) {
            mStartRect.set(mRect);

            mEndRect.set(MovementBounds.getMovementAreaWithGravity(mSettings));
            mEndRect.offset(getPaddingLeft(), getPaddingTop());

            mStateScroller.forceFinished();

            if (animate) {
                mStateScroller.startScroll(0f, 1f);
                mAnimationEngine.start();
            } else {
                setBounds(mEndRect, mEndRounding);
            }
        }
    }

    private void setBounds(RectF rect, float rounding) {
        mRect.set(rect);
        mRounding = rounding;

        // We want to stroke outside of finder rectangle, while by default stroke is centered
        mStrokeRect.set(rect);
        float halfStroke = 0.5f * mPaintStroke.getStrokeWidth();
        mStrokeRect.inset(-halfStroke, -halfStroke);

        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldW, int oldH) {
        update(false);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float rx = mRounding * 0.5f * mRect.width();
        float ry = mRounding * 0.5f * mRect.height();

        // Punching hole in background color requires offscreen drawing
        canvas.saveLayer(0, 0, canvas.getWidth(), canvas.getHeight(), null, 0);
        canvas.drawColor(mBackColor);
        canvas.drawRoundRect(mRect, rx, ry, mPaintClear);
        canvas.restore();

        canvas.drawRoundRect(mStrokeRect, rx, ry, mPaintStroke);
    }

    private static void interpolate(RectF start, RectF end, RectF out, float factor) {
        out.left = StateController.interpolate(start.left, end.left, factor);
        out.top = StateController.interpolate(start.top, end.top, factor);
        out.right = StateController.interpolate(start.right, end.right, factor);
        out.bottom = StateController.interpolate(start.bottom, end.bottom, factor);
    }


    private class LocalAnimationEngine extends AnimationEngine {
        public LocalAnimationEngine() {
            super(FinderView.this);
        }

        @Override
        public boolean onStep() {
            if (!mStateScroller.isFinished()) {
                mStateScroller.computeScroll();
                float state = mStateScroller.getCurr();
                interpolate(mStartRect, mEndRect, mRect, state);
                float rounding = StateController.interpolate(mStartRounding, mEndRounding, state);
                setBounds(mRect, rounding);
                return true;
            }
            return false;
        }
    }

}
