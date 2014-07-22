package com.alexvasilkov.gestures.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.ViewParent;
import android.widget.TextView;
import com.alexvasilkov.gestures.GesturesController;
import com.alexvasilkov.gestures.R;
import com.alexvasilkov.gestures.State;

public class GestureTextView extends TextView implements GesturesController.OnStateChangedListener {

    private final GesturesController mController;

    private float mMinTextSize, mMaxTextSize;
    private float mSize;

    public GestureTextView(Context context) {
        this(context, null, 0);
    }

    public GestureTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GestureTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mController = new GesturesController(context, this);

        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.GestureTextView);
            mMinTextSize = a.getDimension(R.styleable.GestureTextView_minTextSize, 0f);
            mMaxTextSize = a.getDimension(R.styleable.GestureTextView_maxTextSize, 0f);
            a.recycle();
        }

        if (mMinTextSize != 0f) {
            applySize(mMinTextSize);
            if (mMaxTextSize != 0f) mController.getSettings().setMaxZoom(mMaxTextSize / mMinTextSize);
        }

        mController.getSettings().setOverzoomFactor(1f).setPanEnabled(false);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getActionMasked() == MotionEvent.ACTION_POINTER_DOWN) {
            ViewParent parent = getParent();
            if (parent != null) parent.requestDisallowInterceptTouchEvent(true);
        }

        return mController.onTouch(this, event);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mController.getSettings()
                .setViewport(w - getPaddingLeft() - getPaddingRight(), h - getPaddingTop() - getPaddingBottom())
                .setSize(w, h);
        mController.updateState();
    }

    @Override
    public void onStateChanged(State state) {
        if (mMinTextSize == 0f) return; // Nothing to do

        float size = mMinTextSize * state.getZoom();
        if (mMaxTextSize != 0f && size > mMaxTextSize) size = mMaxTextSize;
        applySize(Math.round(size));
    }

    private void applySize(float size) {
        if (mSize != size) {
            mSize = size;
            setTextSize(TypedValue.COMPLEX_UNIT_PX, size);
        }
    }

}
