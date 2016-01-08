package com.alexvasilkov.gestures.internal.detectors;

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;

/**
 * Detects rotation transformation gestures using the supplied {@link MotionEvent}s.
 * The {@link OnRotationGestureListener} callback will notify users when a particular
 * gesture event has occurred.
 * <p/>
 * To use this class:
 * <ul>
 * <li>Create an instance of the {@code RotationGestureDetector} for your {@link View}
 * <li>In the {@link View#onTouchEvent(MotionEvent)} method ensure you call
 * {@link #onTouchEvent(MotionEvent)}. The methods defined in your callback will be executed
 * when the events occur.
 * </ul>
 */
public class RotationGestureDetector {

    /**
     * The listener for receiving notifications when gestures occur. If you want to listen for all
     * the different gestures then implement this interface. If you only want to listen for a
     * subset it might be easier to extend {@link SimpleOnRotationGestureListener}.
     * <p/>
     * An application will receive events in the following order:
     * <ul>
     * <li>One {@link OnRotationGestureListener#onRotationBegin(RotationGestureDetector)}
     * <li>Zero or more {@link OnRotationGestureListener#onRotate(RotationGestureDetector)}
     * <li>One {@link OnRotationGestureListener#onRotationEnd(RotationGestureDetector)}
     * </ul>
     */
    public interface OnRotationGestureListener {
        /**
         * Responds to rotation events for a gesture in progress. Reported by pointer motion.
         *
         * @param detector The detector reporting the event - use this to retrieve extended info
         * about event state.
         * @return Whether or not the detector should consider this event as handled. If an event
         * was not handled, the detector will continue to accumulate movement until an event is
         * handled. This can be useful if an application, for example, only wants to update
         * rotation angle if the change is greater than 0.01.
         */
        boolean onRotate(RotationGestureDetector detector);

        /**
         * Responds to the beginning of a rotation gesture. Reported by new pointers going down.
         *
         * @param detector The detector reporting the event - use this to retrieve extended info
         * about event state.
         * @return Whether or not the detector should continue recognizing this gesture.
         * For example, if a gesture is beginning with a focal point outside of a region where
         * it makes sense, onRotationBegin() may return false to ignore the rest of the gesture.
         */
        boolean onRotationBegin(RotationGestureDetector detector);

        /**
         * Responds to the end of a rotation gesture. Reported by existing pointers going up.
         * <p/>
         * Once a rotation has ended, {@link RotationGestureDetector#getFocusX()} and
         * {@link RotationGestureDetector#getFocusY()} will return focal point of the pointers
         * remaining on the screen.
         *
         * @param detector The detector reporting the event - use this to retrieve extended info
         * about event state.
         */
        void onRotationEnd(RotationGestureDetector detector);
    }

    /**
     * A convenience class to extend when you only want to listen for a subset of rotation-related
     * events. This implements all methods in {@link OnRotationGestureListener} but does nothing.
     * {@link OnRotationGestureListener#onRotate(RotationGestureDetector)} returns {@code false}
     * so that a subclass can retrieve the accumulated rotation factor in an overridden
     * onRotationEnd. {@link OnRotationGestureListener#onRotationBegin(RotationGestureDetector)}
     * returns {@code true}.
     */
    public static class SimpleOnRotationGestureListener implements OnRotationGestureListener {

        @Override
        public boolean onRotate(RotationGestureDetector detector) {
            return false;
        }

        @Override
        public boolean onRotationBegin(RotationGestureDetector detector) {
            return true;
        }

        @Override
        public void onRotationEnd(RotationGestureDetector detector) {
            // Intentionally empty
        }
    }

    private static final float ROTATION_SLOP = 5f;

    private final OnRotationGestureListener mListener;

    private float mFocusX, mFocusY;
    private float mInitialAngle, mCurrAngle, mPrevAngle;
    private boolean mIsInProgress;
    private boolean mIsGestureAccepted;

    /**
     * Creates a RotationGestureDetector with the supplied listener.
     * You may only use this constructor from a {@link android.os.Looper Looper} thread.
     *
     * @param context the application's context
     * @param listener the listener invoked for all the callbacks, this must not be null.
     * @throws NullPointerException if {@code listener} is null.
     */
    public RotationGestureDetector(Context context, OnRotationGestureListener listener) {
        mListener = listener;
    }

    /**
     * Accepts MotionEvents and dispatches events to a {@link OnRotationGestureListener}
     * when appropriate.
     * <p/>
     * <p>Applications should pass a complete and consistent event stream to this method.
     * A complete and consistent event stream involves all MotionEvents from the initial
     * ACTION_DOWN to the final ACTION_UP or ACTION_CANCEL.</p>
     *
     * @param event The event to process
     * @return true if the event was processed and the detector wants to receive the
     * rest of the MotionEvents in this event stream.
     */
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:

                cancelRotation();
                break;

            case MotionEvent.ACTION_POINTER_DOWN:

                if (event.getPointerCount() == 2) {
                    // Second finger is placed
                    mInitialAngle = mPrevAngle = mCurrAngle = computeRotation(event);
                }
                break;

            case MotionEvent.ACTION_MOVE:

                if (event.getPointerCount() >= 2 && (!mIsInProgress || mIsGestureAccepted)) {
                    // Moving 2 or more fingers on the screen
                    mCurrAngle = computeRotation(event);
                    mFocusX = 0.5f * (event.getX(1) + event.getX(0));
                    mFocusY = 0.5f * (event.getY(1) + event.getY(0));
                    boolean isAlreadyStarted = mIsInProgress;
                    tryStartRotation();
                    boolean isAccepted = !isAlreadyStarted || processRotation();
                    if (isAccepted) {
                        mPrevAngle = mCurrAngle;
                    }
                }
                break;

            case MotionEvent.ACTION_POINTER_UP:

                if (event.getPointerCount() == 2) {
                    // Only one finger is left
                    cancelRotation();
                }
                break;

        }

        return true;
    }

    private void tryStartRotation() {
        if (mIsInProgress || Math.abs(mInitialAngle - mCurrAngle) < ROTATION_SLOP) {
            return;
        }
        mIsInProgress = true;
        mIsGestureAccepted = mListener.onRotationBegin(this);
    }

    private void cancelRotation() {
        if (!mIsInProgress) {
            return;
        }
        mIsInProgress = false;
        if (mIsGestureAccepted) {
            mListener.onRotationEnd(this);
            mIsGestureAccepted = false;
        }
    }

    private boolean processRotation() {
        return mIsInProgress && mIsGestureAccepted && mListener.onRotate(this);
    }

    private float computeRotation(MotionEvent event) {
        return (float) Math.toDegrees(Math.atan2(
                event.getY(1) - event.getY(0), event.getX(1) - event.getX(0)));
    }

    /**
     * Returns {@code true} if a rotation gesture is in progress.
     */
    public boolean isInProgress() {
        return mIsInProgress;
    }

    /**
     * Get the X coordinate of the current gesture's focal point. If a gesture is in progress,
     * the focal point is between each of the pointers forming the gesture.
     * <p/>
     * If {@link #isInProgress()} would return false, the result of this function is undefined.
     *
     * @return X coordinate of the focal point in pixels.
     */
    public float getFocusX() {
        return mFocusX;
    }

    /**
     * Get the Y coordinate of the current gesture's focal point. If a gesture is in progress,
     * the focal point is between each of the pointers forming the gesture.
     * <p/>
     * If {@link #isInProgress()} would return false, the result of this function is undefined.
     *
     * @return Y coordinate of the focal point in pixels.
     */
    public float getFocusY() {
        return mFocusY;
    }

    /**
     * Return the rotation delta in degrees from the previous rotation event to the current event.
     *
     * @return The current rotation delta in degrees.
     */
    public float getRotationDelta() {
        return mCurrAngle - mPrevAngle;
    }

}