package com.alexvasilkov.gestures;

import android.support.annotation.NonNull;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import com.alexvasilkov.gestures.detectors.RotationGestureDetector;

/**
 * Simple implementation of several gestures related listeners
 */
public class GesturesAdapter implements View.OnTouchListener,
        GestureDetector.OnGestureListener,
        GestureDetector.OnDoubleTapListener,
        ScaleGestureDetector.OnScaleGestureListener,
        RotationGestureDetector.OnRotationGestureListener {

    @Override
    public boolean onTouch(@NonNull View view, @NonNull MotionEvent e) {
        return false;
    }

    @Override
    public boolean onDown(@NonNull MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(@NonNull MotionEvent e) {
        // no-op
    }

    @Override
    public boolean onSingleTapUp(@NonNull MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(@NonNull MotionEvent e1, @NonNull MotionEvent e2, float v, float v2) {
        return false;
    }

    @Override
    public void onLongPress(@NonNull MotionEvent e) {
        // no-op
    }

    @Override
    public boolean onFling(@NonNull MotionEvent e1, @NonNull MotionEvent e2, float v, float v2) {
        return false;
    }

    @Override
    public boolean onSingleTapConfirmed(@NonNull MotionEvent e) {
        return false;
    }

    @Override
    public boolean onDoubleTap(@NonNull MotionEvent e) {
        return false;
    }

    @Override
    public boolean onDoubleTapEvent(@NonNull MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScale(@NonNull ScaleGestureDetector detector) {
        return false;
    }

    @Override
    public boolean onScaleBegin(@NonNull ScaleGestureDetector detector) {
        return false;
    }

    @Override
    public void onScaleEnd(@NonNull ScaleGestureDetector detector) {
        // no-op
    }

    @Override
    public boolean onRotate(@NonNull RotationGestureDetector detector) {
        return false;
    }

    @Override
    public boolean onRotationBegin(@NonNull RotationGestureDetector detector) {
        return false;
    }

    @Override
    public void onRotationEnd(@NonNull RotationGestureDetector detector) {
        // no-op
    }

}
