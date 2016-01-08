package com.alexvasilkov.gestures.sample.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import com.alexvasilkov.gestures.sample.R;

public class AspectImageView extends ForegroundImageView {

    public static final float DEFAULT_ASPECT = 16f / 9f;

    private static final int VERTICAL = 0;
    private static final int HORIZONTAL = 0;

    private float aspect = DEFAULT_ASPECT;

    public AspectImageView(Context context) {
        super(context);
    }

    public AspectImageView(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.obtainStyledAttributes(attrs, new int[] { R.attr.aspect });
        aspect = a.getFloat(0, aspect);
        a.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int w = MeasureSpec.getSize(widthMeasureSpec);
        int wMode = MeasureSpec.getMode(widthMeasureSpec);
        int h = MeasureSpec.getSize(heightMeasureSpec);
        int hMode = MeasureSpec.getMode(heightMeasureSpec);

        if (wMode == MeasureSpec.EXACTLY || wMode == MeasureSpec.AT_MOST) {
            h = calculate(w, aspect, VERTICAL);
        } else if (hMode == MeasureSpec.EXACTLY || hMode == MeasureSpec.AT_MOST) {
            w = calculate(h, aspect, HORIZONTAL);
        } else {
            throw new IllegalArgumentException("Either width or height should have exact value");
        }

        int specW = MeasureSpec.makeMeasureSpec(w, MeasureSpec.EXACTLY);
        int specH = MeasureSpec.makeMeasureSpec(h, MeasureSpec.EXACTLY);

        super.onMeasure(specW, specH);
    }

    private int calculate(int size, float aspect, int direction) {
        int wp = getPaddingLeft() + getPaddingRight();
        int hp = getPaddingTop() + getPaddingBottom();
        return direction == VERTICAL
                ? Math.round((size - wp) / aspect) + hp
                : Math.round((size - hp) * aspect) + wp;
    }

}
