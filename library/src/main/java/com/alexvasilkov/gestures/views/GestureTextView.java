package com.alexvasilkov.gestures.views;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.ViewParent;
import android.widget.TextView;

import com.alexvasilkov.gestures.GesturesController;
import com.alexvasilkov.gestures.State;
import com.alexvasilkov.gestures.views.interfaces.GestureView;

public class GestureTextView extends TextView implements GestureView {

    private final GesturesController mController;

    private float mOrigSize;
    private float mSize;

    public GestureTextView(Context context) {
        this(context, null, 0);
    }

    public GestureTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GestureTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        mController = new GesturesController(context, new GesturesController.OnStateChangeListener() {
            @Override
            public void onStateChanged(State state) {
                applySize(state);
            }

            @Override
            public void onStateReset(State oldState, State newState) {
                applySize(newState);
            }
        });
        mController.attachToView(this);
        mController.getSettings().setOverzoomFactor(1f).setPanEnabled(false);
        mOrigSize = getTextSize();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GesturesController getController() {
        return mController;
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        if (event.getActionMasked() == MotionEvent.ACTION_POINTER_DOWN) {
            ViewParent parent = getParent();
            if (parent != null) parent.requestDisallowInterceptTouchEvent(true);
        }

        return mController.onTouch(this, event);
    }

    @Override
    public void setTextSize(float size) {
        super.setTextSize(size);
        mOrigSize = getTextSize();
        applySize(mController.getState());
    }

    @Override
    public void setTextSize(int unit, float size) {
        super.setTextSize(unit, size);
        mOrigSize = getTextSize();
        applySize(mController.getState());
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mController.getSettings().setViewport(w, h).setImage(w, h);
        mController.updateState();
    }

    private void applySize(State state) {
        float size = mOrigSize * state.getZoom();
        float maxZoom = mController.getSettings().getMaxZoom();
        size = Math.max(mOrigSize, Math.min(size, mOrigSize * maxZoom));

        if (!State.equals(mSize, size)) {
            mSize = size;
            super.setTextSize(TypedValue.COMPLEX_UNIT_PX, size);
        }
    }

}
