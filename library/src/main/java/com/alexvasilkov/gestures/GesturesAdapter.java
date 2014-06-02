package com.alexvasilkov.gestures;

import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import com.alexvasilkov.gestures.detectors.RotateGestureDetector;

/**
 * Simple implementation of several gestures related listeners
 */
public class GesturesAdapter implements View.OnTouchListener,
        GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener,
        ScaleGestureDetector.OnScaleGestureListener, RotateGestureDetector.OnRotateGestureListener {

    @Override
    public boolean onTouch(View view, MotionEvent e) {
        return false;
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {
        // no-op
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float v, float v2) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {
        // no-op
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float v, float v2) {
        return false;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        return false;
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        return false;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {
        // no-op
    }

    @Override
    public boolean onRotate(RotateGestureDetector detector) {
        return false;
    }

    @Override
    public boolean onRotateBegin(RotateGestureDetector detector) {
        return false;
    }

    @Override
    public void onRotateEnd(RotateGestureDetector detector) {
        // no-op
    }

}
