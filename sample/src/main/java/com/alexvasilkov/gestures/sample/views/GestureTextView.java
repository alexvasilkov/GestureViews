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

    private final GestureController controller;

    private float origSize;
    private float size;

    public GestureTextView(Context context) {
        this(context, null, 0);
    }

    public GestureTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GestureTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        controller = new GestureController(this);
        controller.getSettings().setOverzoomFactor(1f).setPanEnabled(false);
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

        origSize = getTextSize();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GestureController getController() {
        return controller;
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        if (event.getActionMasked() == MotionEvent.ACTION_POINTER_DOWN) {
            ViewParent parent = getParent();
            if (parent != null) {
                parent.requestDisallowInterceptTouchEvent(true);
            }
        }

        return controller.onTouch(this, event);
    }

    @Override
    public void setTextSize(float size) {
        super.setTextSize(size);
        origSize = getTextSize();
        applyState(controller.getState());
    }

    @Override
    public void setTextSize(int unit, float size) {
        super.setTextSize(unit, size);
        origSize = getTextSize();
        applyState(controller.getState());
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
        super.onSizeChanged(width, height, oldWidth, oldHeight);
        controller.getSettings().setViewport(width, height).setImage(width, height);
        controller.updateState();
    }

    protected void applyState(State state) {
        float size = origSize * state.getZoom();
        float maxZoom = controller.getSettings().getMaxZoom();
        size = Math.max(origSize, Math.min(size, origSize * maxZoom));

        if (!State.equals(this.size, size)) {
            this.size = size;
            super.setTextSize(TypedValue.COMPLEX_UNIT_PX, size);
        }
    }

}
