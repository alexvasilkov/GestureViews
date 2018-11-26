package com.alexvasilkov.gestures;

import android.annotation.SuppressLint;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewConfiguration;

import com.alexvasilkov.gestures.internal.detectors.RotationGestureDetector;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.ViewPager;

/**
 * Allows cross movement between view controlled by this {@link GestureController} and it's parent
 * {@link ViewPager} by splitting scroll movements between them.
 */
public class GestureControllerForPager extends GestureController {

    private static final float SCROLL_THRESHOLD = 15f;
    private static final float OVERSCROLL_THRESHOLD_FACTOR = 4f;

    // Temporary objects
    private static final Matrix tmpMatrix = new Matrix();
    private static final RectF tmpRectF = new RectF();


    /**
     * Because ViewPager will immediately return true from onInterceptTouchEvent() method during
     * settling animation, we will have no chance to prevent it from doing this.
     * But this listener will be called if ViewPager intercepted touch event,
     * so we can try fix this behavior here.
     */
    private static final View.OnTouchListener PAGER_TOUCH_LISTENER = new View.OnTouchListener() {
        private boolean isTouchInProgress;

        @SuppressLint("ClickableViewAccessibility") // Not needed for ViewPager
        @Override
        public boolean onTouch(View view, @NonNull MotionEvent event) {
            // ViewPager will steal touch events during settling regardless of
            // requestDisallowInterceptTouchEvent. We will prevent it here.
            if (!isTouchInProgress && event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                isTouchInProgress = true;
                // Now ViewPager is in drag mode, so it should not intercept DOWN event
                view.dispatchTouchEvent(event);
                isTouchInProgress = false;
                return true;
            }

            // User can touch outside of child view, so we will not have a chance to settle
            // ViewPager. If so, this listener should be called and we will be able to settle
            // ViewPager manually.
            settleViewPagerIfFinished((ViewPager) view, event);

            return true; // We should skip view pager touches to prevent some subtle bugs
        }
    };

    private final int touchSlop;

    private ViewPager viewPager;
    private boolean isViewPagerDisabled;

    private boolean isScrollGestureDetected;
    private boolean isSkipViewPager;

    private int viewPagerX;
    private float viewPagerSkippedX;
    private boolean isViewPagerInterceptedScroll;
    private float lastViewPagerEventX;

    public GestureControllerForPager(@NonNull View view) {
        super(view);
        touchSlop = ViewConfiguration.get(view.getContext()).getScaledTouchSlop();
    }

    /**
     * Enables scroll inside {@link ViewPager}
     * (by enabling cross movement between ViewPager and it's child view).
     *
     * @param pager Target ViewPager
     */
    public void enableScrollInViewPager(ViewPager pager) {
        viewPager = pager;
        //noinspection all - ViewPager is not clickable, it is safe to set touch listener
        pager.setOnTouchListener(PAGER_TOUCH_LISTENER);

        // Disabling motion event splitting
        pager.setMotionEventSplittingEnabled(false);
    }

    /**
     * Disables ViewPager scroll. Default is false.
     *
     * @param disable Whether to disable ViewPager scroll or not
     */
    public void disableViewPager(boolean disable) {
        isViewPagerDisabled = disable;
    }

    @SuppressLint("ClickableViewAccessibility") // performClick() will be called in super class
    @Override
    public boolean onTouch(@NonNull View view, @NonNull MotionEvent event) {
        // We need to always receive touch events to pass them to ViewPager (if provided)
        boolean result = super.onTouch(view, event);
        return viewPager != null || result;
    }

    @Override
    protected boolean onTouchInternal(@NonNull View view, @NonNull MotionEvent event) {
        if (viewPager == null) {
            return super.onTouchInternal(view, event);
        } else {
            // Getting motion event in pager coordinates
            MotionEvent pagerEvent = MotionEvent.obtain(event);
            transformToPagerEvent(pagerEvent, view, viewPager);

            handleTouch(pagerEvent);

            boolean result = super.onTouchInternal(view, pagerEvent);
            pagerEvent.recycle();
            return result;
        }
    }

    @Override
    protected boolean shouldDisallowInterceptTouch(MotionEvent event) {
        // If ViewPager is set then we'll always disallow touch interception
        return viewPager != null || super.shouldDisallowInterceptTouch(event);
    }

    private void handleTouch(MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_POINTER_DOWN:
                if (event.getPointerCount() == 2) { // on first non-primary pointer
                    // Skipping ViewPager fake dragging if we're not started dragging yet
                    // to allow scale/rotation gestures
                    isSkipViewPager = !hasViewPagerX();
                }
                break;
            default:
        }
    }

    @Override
    protected boolean onDown(@NonNull MotionEvent event) {
        if (viewPager == null) {
            return super.onDown(event);
        }

        isSkipViewPager = false;
        isViewPagerInterceptedScroll = false;
        isScrollGestureDetected = false;

        viewPagerX = computeInitialViewPagerScroll(event);
        lastViewPagerEventX = event.getX();
        viewPagerSkippedX = 0f;

        passEventToViewPager(event);
        return super.onDown(event);
    }

    @Override
    protected void onUpOrCancel(@NonNull MotionEvent event) {
        passEventToViewPager(event);
        super.onUpOrCancel(event);
    }

    @Override
    protected boolean onScroll(@NonNull MotionEvent e1, @NonNull MotionEvent e2,
            float dx, float dy) {

        if (viewPager == null) {
            return super.onScroll(e1, e2, dx, dy);
        } else {
            if (!isScrollGestureDetected) {
                isScrollGestureDetected = true;
                // First scroll event can stutter a bit, so we will ignore it for smoother scrolling
                return true;
            }

            // Splitting movement between pager and view
            float fixedDistanceX = -scrollBy(e2, -dx);
            // Skipping vertical movement if ViewPager is dragged
            float fixedDistanceY = hasViewPagerX() ? 0f : dy;

            return super.onScroll(e1, e2, fixedDistanceX, fixedDistanceY);
        }
    }

    @Override
    protected boolean onFling(@NonNull MotionEvent e1, @NonNull MotionEvent e2,
            float vx, float vy) {

        return !hasViewPagerX() && super.onFling(e1, e2, vx, vy);
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
    protected boolean onDoubleTapEvent(@NonNull MotionEvent event) {
        return !hasViewPagerX() && super.onDoubleTapEvent(event);
    }

    /*
     * Scrolls ViewPager if view reached bounds. Returns distance at which view can be actually
     * scrolled. Here we will split given distance (dX) into movement of ViewPager and movement
     * of view itself.
     */
    private float scrollBy(@NonNull MotionEvent event, float dx) {
        if (isSkipViewPager || isViewPagerDisabled) {
            return dx;
        }

        final State state = getState();
        getStateController().getMovementArea(state, tmpRectF);

        float pagerDx = splitPagerScroll(dx, state, tmpRectF);
        pagerDx = skipPagerMovement(pagerDx, state, tmpRectF);
        float viewDx = dx - pagerDx;

        // Applying pager scroll
        boolean shouldFixViewX = isViewPagerInterceptedScroll && viewPagerX == 0;
        int actualX = performViewPagerScroll(event, pagerDx);
        viewPagerX += actualX;
        if (shouldFixViewX) { // Adding back scroll not handled by ViewPager
            viewDx += Math.round(pagerDx) - actualX;
        }

        // Returning altered scroll left for image
        return viewDx;
    }

    /*
     * Splits x scroll between viewpager and view.
     */
    private float splitPagerScroll(float dx, State state, RectF movBounds) {
        if (getSettings().isPanEnabled()) {
            final float dir = Math.signum(dx);
            final float movementX = Math.abs(dx); // always >= 0, no direction info

            final float viewX = state.getX();
            // available movement distances (always >= 0, no direction info)
            float availableViewX = dir < 0 ? viewX - movBounds.left : movBounds.right - viewX;
            float availablePagerX = dir * viewPagerX < 0 ? Math.abs(viewPagerX) : 0;

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
            return dx;
        }
    }

    /*
     * Skips part of pager movement to make it harder scrolling pager when image is zoomed
     * or when image is over-scrolled in y direction.
     */
    private float skipPagerMovement(float pagerDx, State state, RectF movBounds) {
        float overscrollDist = getSettings().getOverscrollDistanceY() * OVERSCROLL_THRESHOLD_FACTOR;

        float overscrollThreshold = 0f;
        if (state.getY() < movBounds.top) {
            overscrollThreshold = (movBounds.top - state.getY()) / overscrollDist;
        } else if (state.getY() > movBounds.bottom) {
            overscrollThreshold = (state.getY() - movBounds.bottom) / overscrollDist;
        }

        float minZoom = getStateController().getFitZoom(state);
        float zoomThreshold = minZoom == 0f ? 0f : state.getZoom() / minZoom - 1f;

        float pagerThreshold = Math.max(overscrollThreshold, zoomThreshold);
        pagerThreshold = (float) Math.sqrt(Math.max(0f, Math.min(pagerThreshold, 1f)));
        pagerThreshold *= SCROLL_THRESHOLD * touchSlop;

        // Resetting skipped amount when starting scrolling in different direction
        if (viewPagerSkippedX * pagerDx < 0f && viewPagerX == 0) {
            viewPagerSkippedX = 0f;
        }

        // Ensuring we have full skipped amount if pager is scrolled
        if (hasViewPagerX()) {
            viewPagerSkippedX = pagerThreshold * Math.signum(viewPagerX);
        }

        // Skipping pager movement and accumulating skipped amount, if not passed threshold
        if (Math.abs(viewPagerSkippedX) < pagerThreshold && pagerDx * viewPagerSkippedX >= 0) {
            viewPagerSkippedX += pagerDx;
            // Reverting over-skipped amount
            float over = Math.abs(viewPagerSkippedX) - pagerThreshold;
            over = Math.max(0f, over) * Math.signum(pagerDx);

            viewPagerSkippedX -= over;
            return over;
        } else {
            return pagerDx;
        }
    }

    private int computeInitialViewPagerScroll(MotionEvent downEvent) {
        // ViewPager can be in intermediate position, we should compute correct initial scroll
        int scroll = viewPager.getScrollX();
        int pageWidth = viewPager.getWidth() + viewPager.getPageMargin();

        // After state restore ViewPager can return negative scroll, let's fix it
        while (scroll < 0) {
            scroll += pageWidth;
        }

        int touchedItem = (int) ((scroll + downEvent.getX()) / pageWidth);
        return pageWidth * touchedItem - scroll;
    }

    private boolean hasViewPagerX() {
        // Looks like ViewPager has a rounding issue (it may be off by 1 in settled state)
        return viewPagerX < -1 || viewPagerX > 1;
    }

    /*
     * Manually scrolls ViewPager and returns actual distance at which pager was scrolled.
     */
    private int performViewPagerScroll(@NonNull MotionEvent event, float pagerDx) {
        int scrollBegin = viewPager.getScrollX();
        lastViewPagerEventX += pagerDx;
        passEventToViewPager(event);
        return scrollBegin - viewPager.getScrollX();
    }

    private void passEventToViewPager(@NonNull MotionEvent event) {
        if (viewPager == null) {
            return;
        }

        MotionEvent fixedEvent = obtainOnePointerEvent(event);
        fixedEvent.setLocation(lastViewPagerEventX, 0f);

        if (isViewPagerInterceptedScroll) {
            viewPager.onTouchEvent(fixedEvent);
        } else {
            isViewPagerInterceptedScroll = viewPager.onInterceptTouchEvent(fixedEvent);
        }

        // If ViewPager intercepted touch it will settle itself automatically,
        // but if touch was not intercepted we should settle it manually
        if (!isViewPagerInterceptedScroll && hasViewPagerX()) {
            settleViewPagerIfFinished(viewPager, event);
        }

        // Hack: ViewPager has bug when endFakeDrag() does not work properly. But we need to ensure
        // ViewPager is not in fake drag mode after settleViewPagerIfFinished()
        try {
            if (viewPager != null && viewPager.isFakeDragging()) {
                viewPager.endFakeDrag();
            }
        } catch (Exception ignored) {
        }

        fixedEvent.recycle();
    }

    private static MotionEvent obtainOnePointerEvent(@NonNull MotionEvent event) {
        return MotionEvent.obtain(event.getDownTime(), event.getEventTime(), event.getAction(),
                event.getX(), event.getY(), event.getMetaState());
    }

    private static void settleViewPagerIfFinished(ViewPager pager, @NonNull MotionEvent event) {
        if (event.getActionMasked() == MotionEvent.ACTION_UP
                || event.getActionMasked() == MotionEvent.ACTION_CANCEL) {
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
        tmpMatrix.reset();
        transformMatrixToPager(tmpMatrix, view, pager);
        event.transform(tmpMatrix);
    }

    /*
     * Inspired by hidden method View#transformMatrixToGlobal().
     */
    private static void transformMatrixToPager(Matrix matrix, View view, ViewPager pager) {
        if (view.getParent() instanceof View) {
            View parent = (View) view.getParent();
            if (parent != pager) {
                transformMatrixToPager(matrix, parent, pager);
            }
            matrix.preTranslate(-parent.getScrollX(), -parent.getScrollY());
        }

        matrix.preTranslate(view.getLeft(), view.getTop());
        matrix.preConcat(view.getMatrix());
    }

}
