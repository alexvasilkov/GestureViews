package com.alexvasilkov.gestures;

import android.annotation.TargetApi;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewConfiguration;

import com.alexvasilkov.gestures.internal.detectors.RotationGestureDetector;

/**
 * Allows cross movement between view controlled by this {@link GestureController} and it's parent
 * {@link ViewPager} by splitting scroll movements between them.
 */
public class GestureControllerForPager extends GestureController {

    private static final float SCROLL_THRESHOLD = 25f;
    private static final float OVERSCROLL_THRESHOLD_FACTOR = 4f;

    /**
     * Because ViewPager will immediately return true from onInterceptTouchEvent() method during
     * settling animation, we will have no chance to prevent it from doing this.
     * But this listener will be called if ViewPager intercepted touch event,
     * so we can try fix this behavior here.
     */
    private static final View.OnTouchListener PAGER_TOUCH_LISTENER = new View.OnTouchListener() {
        private boolean mIsTouchInProgress;

        @Override
        public boolean onTouch(View view, @NonNull MotionEvent e) {
            // ViewPager will steal touch events during settling regardless of
            // requestDisallowInterceptTouchEvent. We will prevent it here.
            if (!mIsTouchInProgress && e.getActionMasked() == MotionEvent.ACTION_DOWN) {
                mIsTouchInProgress = true;
                // Now ViewPager is in drag mode, so it should not intercept DOWN event
                view.dispatchTouchEvent(e);
                mIsTouchInProgress = false;
                return true;
            }

            // User can touch outside of child view, so we will not have a chance to settle
            // ViewPager. If so, this listener should be called and we will be able to settle
            // ViewPager manually.
            settleViewPagerIfFinished((ViewPager) view, e);

            return true; // We should skip view pager touches to prevent some subtle bugs
        }
    };

    private static final int[] TMP_LOCATION = new int[2];
    private static final Matrix TMP_MATRIX = new Matrix();

    private final int mTouchSlop;

    private ViewPager mViewPager;
    private boolean mIsViewPagerDisabled;

    private boolean mIsScrollGestureDetected;
    private boolean mIsSkipViewPager;

    private int mViewPagerX;
    private float mViewPagerSkippedX;
    private boolean mIsViewPagerInterceptedScroll;
    private float mLastViewPagerEventX;

    public GestureControllerForPager(@NonNull View view) {
        super(view);
        mTouchSlop = ViewConfiguration.get(view.getContext()).getScaledTouchSlop();
    }

    /**
     * Enables scroll inside {@link ViewPager}
     * (by enabling cross movement between ViewPager and it's child view)
     */
    public void enableScrollInViewPager(ViewPager pager) {
        mViewPager = pager;
        pager.setOnTouchListener(PAGER_TOUCH_LISTENER);

        // Disabling motion event splitting
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            pager.setMotionEventSplittingEnabled(false);
        }
    }

    /**
     * Disables ViewPager scroll. Default is false.
     */
    public void disableViewPager(boolean disable) {
        mIsViewPagerDisabled = disable;
    }

    @Override
    public boolean onTouch(@NonNull View view, @NonNull MotionEvent event) {
        if (mViewPager == null) {
            return super.onTouch(view, event);
        } else {
            // Getting motiona event in pager coordinates
            MotionEvent pagerEvent = MotionEvent.obtain(event);
            transformToPagerEvent(pagerEvent, view, mViewPager);

            handleTouch(pagerEvent);

            boolean result = super.onTouch(view, pagerEvent);
            pagerEvent.recycle();
            return result;
        }
    }

    private void handleTouch(MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_POINTER_DOWN:
                if (event.getPointerCount() == 2) { // on first non-primary pointer
                    // Skipping ViewPager fake dragging if we're not started dragging yet
                    // to allow scale/rotation gestures
                    mIsSkipViewPager = !hasViewPagerX();
                }
                break;
        }
    }

    @Override
    protected boolean onDown(@NonNull MotionEvent e) {
        mViewPager.requestDisallowInterceptTouchEvent(true);

        mIsSkipViewPager = false;
        mIsViewPagerInterceptedScroll = false;
        mIsScrollGestureDetected = false;

        mViewPagerX = computeInitialViewPagerScroll(e);
        mLastViewPagerEventX = e.getX();
        mViewPagerSkippedX = 0f;

        passEventToViewPager(e);
        super.onDown(e);
        return true;
    }

    @Override
    protected void onUpOrCancel(@NonNull MotionEvent e) {
        passEventToViewPager(e);
        super.onUpOrCancel(e);
    }

    @Override
    protected boolean onScroll(@NonNull MotionEvent e1, @NonNull MotionEvent e2,
            float dX, float dY) {

        if (mViewPager == null) {
            return super.onScroll(e1, e2, dX, dY);
        } else {
            if (!mIsScrollGestureDetected) {
                mIsScrollGestureDetected = true;
                // First scroll event can stutter a bit, so we will ignore it for smoother scrolling
                return true;
            }

            // Splitting movement between pager and view
            float fixedDistanceX = -scrollBy(e2, -dX);
            // Skipping vertical movement if ViewPager is dragged
            float fixedDistanceY = hasViewPagerX() ? 0f : dY;

            return super.onScroll(e1, e2, fixedDistanceX, fixedDistanceY);
        }
    }

    @Override
    protected boolean onFling(@NonNull MotionEvent e1, @NonNull MotionEvent e2,
            float vX, float vY) {

        return !hasViewPagerX() && super.onFling(e1, e2, vX, vY);
    }

    @Override
    protected boolean onScaleBegin(@NonNull ScaleGestureDetector detector) {
        return !hasViewPagerX() && super.onScaleBegin(detector);
    }

    @Override
    protected boolean onRotationBegin(@NonNull RotationGestureDetector detector) {
        return !hasViewPagerX() && super.onRotationBegin(detector);
    }

    @Override
    protected boolean onDoubleTapEvent(@NonNull MotionEvent e) {
        return !hasViewPagerX() && super.onDoubleTapEvent(e);
    }

    /**
     * Scrolls ViewPager if view reached bounds. Returns distance at which view can be actually
     * scrolled. Here we will split given distance (dX) into movement of ViewPager and movement
     * of view itself.
     */
    private float scrollBy(@NonNull MotionEvent e, float dX) {
        if (mIsSkipViewPager || mIsViewPagerDisabled) {
            return dX;
        }

        final State state = getState();
        final RectF movBounds = getStateController().getMovementBounds(state).getExternalBounds();

        float dPagerX = splitPagerScroll(dX, state, movBounds);
        dPagerX = skipPagerMovement(dPagerX, state, movBounds);
        float dViewX = dX - dPagerX;

        // Applying pager scroll
        boolean shouldFixViewX = mIsViewPagerInterceptedScroll && mViewPagerX == 0;
        int actualX = performViewPagerScroll(e, dPagerX);
        mViewPagerX += actualX;
        if (shouldFixViewX) { // Adding back scroll not handled by ViewPager
            dViewX += Math.round(dPagerX) - actualX;
        }

        // Returning altered scroll left for image
        return dViewX;
    }

    /**
     * Splits x scroll between viewpager and view
     */
    private float splitPagerScroll(float dX, State state, RectF movBounds) {
        if (getSettings().isPanEnabled()) {
            final float dir = Math.signum(dX);
            final float movementX = Math.abs(dX); // always >= 0, no direction info

            final float viewX = state.getX();
            // available movement distances (always >= 0, no direction info)
            float availableViewX = dir < 0 ? viewX - movBounds.left : movBounds.right - viewX;
            float availablePagerX = dir * mViewPagerX < 0 ? Math.abs(mViewPagerX) : 0;

            // Not available if already overscrolled in same direction
            if (availableViewX < 0) {
                availableViewX = 0;
            }

            float pagerMovementX;
            if (availablePagerX >= movementX) {
                // Only ViewPager is moved
                pagerMovementX = movementX;
            } else if (availableViewX + availablePagerX >= movementX) {
                // Moving pager for full available distance and moving view for remaining distance
                pagerMovementX = availablePagerX;
            } else {
                // Moving view for full available distance and moving pager for remaining distance
                pagerMovementX = movementX - availableViewX;
            }

            return pagerMovementX * dir; // Applying direction
        } else {
            return dX;
        }
    }

    /**
     * Skips part of pager movement to make it harder scrolling pager when image is zoomed
     * or when image is over-scrolled in y direction
     */
    private float skipPagerMovement(float dPagerX, State state, RectF movBounds) {
        float overscrollDist = getSettings().getOverscrollDistanceY() * OVERSCROLL_THRESHOLD_FACTOR;

        float overscrollThreshold = 0f;
        if (state.getY() < movBounds.top) {
            overscrollThreshold = (movBounds.top - state.getY()) / overscrollDist;
        } else if (state.getY() > movBounds.bottom) {
            overscrollThreshold = (state.getY() - movBounds.bottom) / overscrollDist;
        }

        float minZoom = getStateController().getEffectiveMinZoom();
        float zoomThreshold = minZoom == 0f ? 0f : state.getZoom() / minZoom - 1f;

        float pagerThreshold = Math.max(overscrollThreshold, zoomThreshold);
        pagerThreshold = (float) Math.sqrt(Math.max(0f, Math.min(pagerThreshold, 1f)));
        pagerThreshold *= SCROLL_THRESHOLD * mTouchSlop;

        // Resetting skipped amount when starting scrolling in different direction
        if (mViewPagerSkippedX * dPagerX < 0f && mViewPagerX == 0) {
            mViewPagerSkippedX = 0f;
        }

        // Ensuring we have full skipped amount if pager is scrolled
        if (hasViewPagerX()) {
            mViewPagerSkippedX = pagerThreshold * Math.signum(mViewPagerX);
        }

        // Skipping pager movement and accumulating skipped amount, if not passed threshold
        if (Math.abs(mViewPagerSkippedX) < pagerThreshold && dPagerX * mViewPagerSkippedX >= 0) {
            mViewPagerSkippedX += dPagerX;
            // Reverting over-skipped amount
            float over = Math.abs(mViewPagerSkippedX) - pagerThreshold;
            over = Math.max(0f, over) * Math.signum(dPagerX);

            mViewPagerSkippedX -= over;
            return over;
        } else {
            return dPagerX;
        }
    }

    private int computeInitialViewPagerScroll(MotionEvent downEvent) {
        // ViewPager can be in intermediate position, we should compute correct initial scroll
        int scroll = mViewPager.getScrollX();
        int pageWidth = mViewPager.getWidth() + mViewPager.getPageMargin();

        // After state restore ViewPager can return negative scroll, let's fix it
        while (scroll < 0) {
            scroll += pageWidth;
        }

        int touchedItem = (int) ((scroll + downEvent.getX()) / pageWidth);
        return pageWidth * touchedItem - scroll;
    }

    private boolean hasViewPagerX() {
        // Looks like ViewPager has a rounding issue (it may be off by 1 in settled state)
        return mViewPagerX < -1 || mViewPagerX > 1;
    }

    /**
     * Manually scrolls ViewPager and returns actual distance at which pager was scrolled
     */
    private int performViewPagerScroll(@NonNull MotionEvent e, float dPagerX) {
        int scrollBegin = mViewPager.getScrollX();
        mLastViewPagerEventX += dPagerX;
        passEventToViewPager(e);
        return scrollBegin - mViewPager.getScrollX();
    }

    private void passEventToViewPager(@NonNull MotionEvent e) {
        if (mViewPager == null) {
            return;
        }

        MotionEvent fixedEvent = obtainOnePointerEvent(e);
        fixedEvent.setLocation(mLastViewPagerEventX, 0f);

        if (mIsViewPagerInterceptedScroll) {
            mViewPager.onTouchEvent(fixedEvent);
        } else {
            mIsViewPagerInterceptedScroll = mViewPager.onInterceptTouchEvent(fixedEvent);
        }

        // If ViewPager intercepted touch it will settle itself automatically,
        // but if touch was not intercepted we should settle it manually
        if (!mIsViewPagerInterceptedScroll && hasViewPagerX()) {
            settleViewPagerIfFinished(mViewPager, e);
        }

        // Hack: ViewPager has bug when endFakeDrag() does not work properly. But we need to ensure
        // ViewPager is not in fake drag mode after settleViewPagerIfFinished()
        try {
            if (mViewPager != null && mViewPager.isFakeDragging()) {
                mViewPager.endFakeDrag();
            }
        } catch (Exception ignored) {
        }

        fixedEvent.recycle();
    }

    private static MotionEvent obtainOnePointerEvent(@NonNull MotionEvent e) {
        return MotionEvent.obtain(e.getDownTime(), e.getEventTime(), e.getAction(),
                e.getX(), e.getY(), e.getMetaState());
    }

    private static void settleViewPagerIfFinished(ViewPager pager, @NonNull MotionEvent e) {
        if (e.getActionMasked() == MotionEvent.ACTION_UP ||
                e.getActionMasked() == MotionEvent.ACTION_CANCEL) {
            // Hack: if ViewPager is not settled we should force it to do so, fake drag will help
            try {
                // Pager may throw an annoying exception if there are no internal page state items
                pager.beginFakeDrag();
                if (pager.isFakeDragging()) {
                    pager.endFakeDrag();
                }
            } catch (Exception ignored) {
            }
        }
    }

    private static void transformToPagerEvent(MotionEvent event, View view, ViewPager pager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            TMP_MATRIX.reset();
            transformMatrixToPager(TMP_MATRIX, view, pager);
            event.transform(TMP_MATRIX);
        } else {
            view.getLocationOnScreen(TMP_LOCATION);
            event.offsetLocation(TMP_LOCATION[0], TMP_LOCATION[1]);
            pager.getLocationOnScreen(TMP_LOCATION);
            event.offsetLocation(-TMP_LOCATION[0], -TMP_LOCATION[1]);
        }
    }

    /**
     * Inspired by hidden method View#transformMatrixToGlobal().
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private static void transformMatrixToPager(Matrix matrix, View view, ViewPager pager) {
        View parent = (View) view.getParent();
        if (parent != null && parent != pager) {
            transformMatrixToPager(matrix, parent, pager);
        }
        if (parent != null) {
            matrix.preTranslate(-parent.getScrollX(), -parent.getScrollY());
        }

        matrix.preTranslate(view.getLeft(), view.getTop());
        matrix.preConcat(view.getMatrix());
    }

}
