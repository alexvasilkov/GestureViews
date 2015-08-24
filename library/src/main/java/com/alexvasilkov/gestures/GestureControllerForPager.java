package com.alexvasilkov.gestures;

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
 * {@link android.support.v4.view.ViewPager} by splitting scroll movements between them.
 */
public class GestureControllerForPager extends GestureController {

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

            // User can touch outside of child view, so we will not have a chance to settle ViewPager.
            // If so, this listener should be called and we will be able to settle ViewPager manually.
            settleViewPagerIfFinished((ViewPager) view, e);

            return true; // We should skip view pager touches to prevent some subtle bugs
        }
    };

    private static final int[] TMP_LOCATION = new int[2];

    private final int mTouchSlop;

    private ViewPager mViewPager;
    private boolean mIsViewPagerDisabled;

    private MotionEvent mTmpEvent;
    private boolean mIsScrollGestureDetected;
    private boolean mIsScrollingViewPager;
    private boolean mIsSkipViewPager;

    private int mViewPagerX;
    private boolean mIsViewPagerInterceptedScroll;
    private boolean mIsAllowViewPagerScrollY;
    private float mLastViewPagerEventX, mLastViewPagerEventY;

    public GestureControllerForPager(@NonNull View view) {
        super(view);
        mTouchSlop = ViewConfiguration.get(view.getContext()).getScaledTouchSlop();
    }

    /**
     * Enables scroll inside {@link android.support.v4.view.ViewPager}
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

    public void disableViewPager(boolean disable) {
        mIsViewPagerDisabled = disable;
    }

    @Override
    public boolean onTouch(@NonNull View view, @NonNull MotionEvent event) {
        if (mViewPager == null) {
            return super.onTouch(view, event);
        } else {
            MotionEvent fixedEvent = handleTouch(view, event);
            return fixedEvent == null || super.onTouch(view, fixedEvent);
        }
    }

    /**
     * Handles touch event and returns altered event to pass further
     * or null if event should not propagate
     */
    private MotionEvent handleTouch(View view, MotionEvent event) {
        recycleTmpEvent();

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                mViewPager.requestDisallowInterceptTouchEvent(true);

                mIsSkipViewPager = false;

                mViewPagerX = computeInitialViewPagerX(view, event);
                mIsScrollingViewPager = mViewPagerX != 0;

                mLastViewPagerEventX = event.getX();
                mLastViewPagerEventY = event.getY();
                break;

            case MotionEvent.ACTION_POINTER_DOWN:
                if (event.getPointerCount() == 2) { // on first non-primary pointer
                    // Skipping ViewPager fake dragging if we're not started dragging yet
                    // to allow scale/rotation gestures
                    mIsSkipViewPager = mViewPagerX == 0;
                }
                break;
        }

        if (mIsSkipViewPager) return event; // No event adjustments are needed

        // Applying offset to the returned event, offset will be calculated in scrollBy method below
        mTmpEvent = MotionEvent.obtain(event);
        mTmpEvent.offsetLocation(mViewPagerX, 0f);
        return mTmpEvent;
    }

    private void recycleTmpEvent() {
        if (mTmpEvent != null) {
            mTmpEvent.recycle();
            mTmpEvent = null;
        }
    }

    @Override
    protected boolean onDown(@NonNull MotionEvent e) {
        mIsViewPagerInterceptedScroll = false;
        mIsAllowViewPagerScrollY = true;
        mIsScrollGestureDetected = false;
        passEventToViewPager(e);
        return super.onDown(e);
    }

    @Override
    protected void onUpOrCancel(MotionEvent e) {
        passEventToViewPager(e);
        super.onUpOrCancel(e);
    }

    @Override
    protected boolean onScroll(@NonNull MotionEvent e1, @NonNull MotionEvent e2, float dX, float dY) {
        if (mViewPager == null) {
            return super.onScroll(e1, e2, dX, dY);
        } else {
            if (!mIsScrollGestureDetected) {
                mIsScrollGestureDetected = true;
                // First scroll event can jerk a bit, so we will ignore it for smoother scrolling
                return true;
            }

            float fixedDistanceX = -scrollBy(e2, -dX, -dY);
            // Skipping vertical movement if ViewPager is dragged
            float fixedDistanceY = mViewPagerX == 0 ? dY : 0f;

            return super.onScroll(e1, e2, fixedDistanceX, fixedDistanceY);
        }
    }

    @Override
    protected boolean onFling(@NonNull MotionEvent e1, @NonNull MotionEvent e2, float vX, float vY) {
        return mViewPagerX == 0 && super.onFling(e1, e2, vX, vY);
    }

    @Override
    protected boolean onScaleBegin(@NonNull ScaleGestureDetector detector) {
        return mViewPagerX == 0 && super.onScaleBegin(detector);
    }

    @Override
    protected boolean onRotationBegin(@NonNull RotationGestureDetector detector) {
        return mViewPagerX == 0 && super.onRotationBegin(detector);
    }

    @Override
    protected boolean onDoubleTapEvent(@NonNull MotionEvent e) {
        return mViewPagerX == 0 && super.onDoubleTapEvent(e);
    }

    /**
     * Scrolls ViewPager if view reached bounds. Returns distance at which view can be actually scrolled.
     * Here we will split given distance (dX) into movement of ViewPager and movement of view itself.
     */
    private float scrollBy(MotionEvent e, float dX, float dY) {
        if (mIsSkipViewPager) return dX;

        float dViewX, dPagerX;

        final State state = getState();
        final RectF movBounds = getStateController().getMovementBounds(state).getExternalBounds();

        // Splitting x scroll between viewpager and view
        if (getSettings().isPanEnabled()) {
            final float dir = Math.signum(dX);
            final float movementX = Math.abs(dX); // always >= 0, no direction info

            final float viewX = state.getX();
            // available movement distances (always >= 0, no direction info)
            float availableViewX = dir < 0 ? viewX - movBounds.left : movBounds.right - viewX;
            float availablePagerX = dir * mViewPagerX < 0 ? Math.abs(mViewPagerX) : 0;

            // Not available if already overscrolled in same direction
            if (availableViewX < 0) availableViewX = 0;

            if (availablePagerX >= movementX) {
                // Only ViewPager is moved
                dViewX = 0;
                dPagerX = movementX;
            } else if (availableViewX + availablePagerX >= movementX) {
                // Moving pager for full available distance and moving view for remaining distance
                dViewX = movementX - availablePagerX;
                dPagerX = availablePagerX;
            } else {
                // Moving view for full available distance and moving pager for remaining distance
                dViewX = availableViewX;
                dPagerX = movementX - availableViewX;
            }

            // Applying direction
            dViewX *= dir;
            dPagerX *= dir;
        } else {
            dPagerX = dX;
            dViewX = 0f;
        }

        // Checking vertical and horizontal thresholds
        if (!mIsScrollingViewPager) {
            mIsScrollingViewPager = true;
            // We want ViewPager to stop scrolling horizontally only if view has a small
            // movement area and a vertical scroll is detected.
            // We will allow passing dY to ViewPager so it will be able to stop itself
            // if vertical scroll is detected.
            mIsAllowViewPagerScrollY = movBounds.width() < mTouchSlop;
        }

        if (mIsViewPagerDisabled) dPagerX = 0;

        boolean shouldFixViewX = mIsViewPagerInterceptedScroll && mViewPagerX == 0;
        int actualX = performViewPagerScroll(e, dPagerX, dY);
        mViewPagerX += actualX;
        // Adding back scroll not handled by ViewPager
        if (shouldFixViewX) dViewX += Math.round(dPagerX) - actualX;

        return dViewX;
    }

    private int computeInitialViewPagerX(View view, MotionEvent event) {
        // ViewPager can be in intermediate position, so we should recompute correct mViewPagerX value
        int scroll = mViewPager.getScrollX();
        int widthWithMargin = mViewPager.getWidth() + mViewPager.getPageMargin();
        // After state restore ViewPager can return negative scroll, let's fix it
        while (scroll < 0) scroll += widthWithMargin;

        // Child's event will be in local coordinates, but we want it in ViewPager's coordinates
        float viewPagerTouchX = event.getX();
        view.getLocationOnScreen(TMP_LOCATION);
        viewPagerTouchX += TMP_LOCATION[0];
        mViewPager.getLocationOnScreen(TMP_LOCATION);
        viewPagerTouchX -= TMP_LOCATION[0];

        int touchedItem = (int) ((scroll + viewPagerTouchX) / widthWithMargin);

        int x = widthWithMargin * touchedItem - scroll;
        // Fixing ViewPager rounding issue (it may be off by 1 in settled state)
        return -1 <= x && x <= 1 ? 0 : x;
    }

    private void passEventToViewPager(MotionEvent e) {
        if (mViewPager == null) return;

        MotionEvent fixedEvent = obtainOnePointerEvent(e);
        fixedEvent.setLocation(mLastViewPagerEventX, mLastViewPagerEventY);

        if (mIsViewPagerInterceptedScroll) {
            mViewPager.onTouchEvent(fixedEvent);
        } else {
            mIsViewPagerInterceptedScroll = mViewPager.onInterceptTouchEvent(fixedEvent);
        }

        // If ViewPager intercepted touch it will settle itself automatically,
        // but if touch was not intercepted we should settle it manually
        if (!mIsViewPagerInterceptedScroll) settleViewPagerIfFinished(mViewPager, e);

        // Hack: ViewPager has bug when endFakeDrag() does not work properly.
        // But we need to ensure ViewPager is not in fake drag mode after settleViewPagerIfFinished()
        try {
            if (mViewPager != null && mViewPager.isFakeDragging()) mViewPager.endFakeDrag();
        } catch (Exception ignored) {
        }

        fixedEvent.recycle();
    }

    /**
     * Manually scrolls ViewPager and returns actual distance at which pager was scrolled
     */
    private int performViewPagerScroll(MotionEvent e, float dPagerX, float dY) {
        int scrollBegin = mViewPager.getScrollX();
        mLastViewPagerEventX += dPagerX;
        if (mIsAllowViewPagerScrollY) mLastViewPagerEventY += dY;
        passEventToViewPager(e);
        return scrollBegin - mViewPager.getScrollX();
    }

    private static MotionEvent obtainOnePointerEvent(MotionEvent e) {
        return MotionEvent.obtain(e.getDownTime(), e.getEventTime(), e.getAction(),
                e.getX(), e.getY(), e.getMetaState());
    }

    private static void settleViewPagerIfFinished(ViewPager pager, MotionEvent e) {
        if (e.getActionMasked() != MotionEvent.ACTION_UP && e.getActionMasked() != MotionEvent.ACTION_CANCEL)
            return;

        // Hack: if ViewPager is not settled we should force it to do so, fake drag will help
        try {
            // Pager may throw an annoying exception if there are no internal page state items
            pager.beginFakeDrag();
            if (pager.isFakeDragging()) pager.endFakeDrag();
        } catch (Exception ignored) {
        }
    }

}
