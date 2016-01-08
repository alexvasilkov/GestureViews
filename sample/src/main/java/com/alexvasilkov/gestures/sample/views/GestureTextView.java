package com.alexvasilkov.gestures.sample.views;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.ViewParent;
import android.widget.TextView;

import com.alexvasilkov.gestures.GestureController;
import com.alexvasilkov.gestures.State;
import com.alexvasilkov.gestures.views.interfaces.GestureView;

/**
 * Sample of TextView with added gesture controls.
 */
public class GestureTextView extends TextView implements GestureView {

    private final GestureController mController;

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

        mController = new GestureController(this);
        mController.getSettings().setOverzoomFactor(1f).setPanEnabled(false);
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

        mOrigSize = getTextSize();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GestureController getController() {
        return mController;
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        if (event.getActionMasked() == MotionEvent.ACTION_POINTER_DOWN) {
            ViewParent parent = getParent();
            if (parent != null) {
                parent.requestDisallowInterceptTouchEvent(true);
            }
        }

        return mController.onTouch(this, event);
    }

    @Override
    public void setTextSize(float size) {
        super.setTextSize(size);
        mOrigSize = getTextSize();
        applyState(mController.getState());
    }

    @Override
    public void setTextSize(int unit, float size) {
        super.setTextSize(unit, size);
        mOrigSize = getTextSize();
        applyState(mController.getState());
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mController.getSettings().setViewport(w, h).setImage(w, h);
        mController.updateState();
    }

    protected void applyState(State state) {
        float size = mOrigSize * state.getZoom();
        float maxZoom = mController.getSettings().getMaxZoom();
        size = Math.max(mOrigSize, Math.min(size, mOrigSize * maxZoom));

        if (!State.equals(mSize, size)) {
            mSize = size;
            super.setTextSize(TypedValue.COMPLEX_UNIT_PX, size);
        }
    }

}
