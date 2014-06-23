package com.alexvasilkov.gestures;

import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.support.v4.view.ViewPager;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

/**
 * Allows cross movement between view controlled by this {@link GesturesController}
 * and it's parent {@link android.support.v4.view.ViewPager} by splitting scroll movement
 * between view and view pager
 */
public class GesturesControllerPagerFix extends GesturesController {

    /**
     * Because viewpager will immediately return true from onInterceptTouchEvent() method during settling
     * animation we will have no chance to prevent it from doing this.
     * But this listener will be called if viewpager intercepted touch event, so we can try fix this behavior here.
     */
    private static final View.OnTouchListener PAGER_TOUCH_LISTENER = new View.OnTouchListener() {
        private boolean mIsTouchInProgress;

        @Override
        public boolean onTouch(View view, MotionEvent e) {
            // ViewPager will steal touch events during settling regardless of requestDisallowInterceptTouchEvent,
            // so we will prevent it here
            if (!mIsTouchInProgress && e.getActionMasked() == MotionEvent.ACTION_DOWN) {
                mIsTouchInProgress = true;
                // Now viewpager is in drag mode, so it should not intercept DOWN event
                view.dispatchTouchEvent(e);
                mIsTouchInProgress = false;
                return true;
            } else {
                return false;
            }
        }
    };

    private static final int[] TMP_LOCATION = new int[2];

    private final int mTouchSlop;

    private ViewPager mViewPager;

    private MotionEvent mTmpEvent;
    private boolean mIsScrollGestureDetected;
    private boolean mIsScrollingViewPager;
    private boolean mIsSkipViewPager;

    private int mViewPagerX;
    private boolean mIsViewPagerInterceptedScroll;
    private boolean mIsAllowViewPagerScrollY;
    private float mLastViewPagerEventX, mLastViewPagerEventY;
    private float mLastEventX, mLastEventY;

    private boolean mIsSkipNonPrimaryPointers;

    public GesturesControllerPagerFix(Context context, OnStateChangedListener listener) {
        super(context, listener);

        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    public void fixViewPagerScroll(ViewPager pager) {
        mViewPager = pager;
        mViewPager.setOnTouchListener(PAGER_TOUCH_LISTENER);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            mViewPager.setMotionEventSplittingEnabled(false);
        }
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        if (mViewPager == null) {
            return super.onTouch(view, event);
        } else {
            MotionEvent fixedEvent = handleTouch(view, event);
            return fixedEvent == null || super.onTouch(view, fixedEvent);
        }
    }

    /**
     * Handles touch event and returns altered event to pass further or null if event should not propagate
     */
    private MotionEvent handleTouch(View view, MotionEvent event) {
        recycleTmpEvent();

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                mViewPager.requestDisallowInterceptTouchEvent(true);

                mIsSkipViewPager = false;

                mViewPagerX = computeInitialViewPagerX(view, event);
                mIsScrollingViewPager = mViewPagerX != 0;
                break;

            case MotionEvent.ACTION_POINTER_DOWN:
                if (event.getPointerCount() == 2) { // on first non-primary pointer
                    // Skipping non-primary pointers if we're already dragging view pager
                    // to skip scale/rotation gestures
                    mIsSkipNonPrimaryPointers = mViewPagerX != 0;
                    // Skipping view pager fake dragging if we're not started dragging yet
                    // to allow scale/rotation gestures
                    mIsSkipViewPager = mViewPagerX == 0;
                }

                // Fixing event location in case active pointer was changed
                mLastViewPagerEventX += mLastEventX - event.getX();
                mLastViewPagerEventY += mLastEventY - event.getY();
                break;

            case MotionEvent.ACTION_POINTER_UP:
                if (event.getPointerCount() == 2) { // only primary pointer left
                    mIsSkipNonPrimaryPointers = false;
                }

                // Fixing event location in case active pointer was changed
                mLastViewPagerEventX += mLastEventX - event.getX();
                mLastViewPagerEventY += mLastEventY - event.getY();
                break;
        }

        mLastEventX = event.getX();
        mLastEventY = event.getY();

        if (mIsSkipViewPager) return event; // No event adjusting is needed

        boolean isNonPrimaryPointerEvent = event.getActionMasked() == MotionEvent.ACTION_POINTER_DOWN
                || event.getActionMasked() == MotionEvent.ACTION_POINTER_UP;

        if (mIsSkipNonPrimaryPointers && isNonPrimaryPointerEvent) return null; // No event should be passed further

        mTmpEvent = mIsSkipNonPrimaryPointers ? obtainOnePointerEvent(event) : MotionEvent.obtain(event);
        // Applying offset to the returned event, offset will be calculated in scrollBy method below
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
    public boolean onDown(MotionEvent e) {
        mLastViewPagerEventX = e.getX();
        mLastViewPagerEventY = e.getY();
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
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        if (mViewPager == null) {
            return super.onScroll(e1, e2, distanceX, distanceY);
        } else {
            if (!mIsScrollGestureDetected) {
                mIsScrollGestureDetected = true;
                // First scroll event can jerk a bit, so we will ignore it for smoother scrolling
                return true;
            }

            float fixedDistanceX = -scrollBy(e2, -distanceX, -distanceY);
            // Skipping vertical movement if view pager is dragged
            float fixedDistanceY = mViewPagerX == 0 ? distanceY : 0f;

            return super.onScroll(e1, e2, fixedDistanceX, fixedDistanceY);
        }
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return mViewPagerX == 0 && super.onFling(e1, e2, velocityX, velocityY);
    }

    /**
     * Scrolls view pager when view reached it's bounds. Returns distance (<= dX) at which view can be scrolled.
     * Here we will split given distance (dX) into movement of ViewPager and movement of view itself.
     */
    private float scrollBy(MotionEvent e, float dX, float dY) {
        if (mIsSkipViewPager) return dX;

        float dViewX, dPagerX;

        final State state = getState();
        final Rect viewMovingBounds = getStateController().getMovingBounds(state);

        // Splitting x scroll between viewpager and view
        if (getSettings().isEnabled()) {
            final float dir = Math.signum(dX);
            final float movementX = Math.abs(dX); // always >= 0, no direction info

            final float viewX = state.getX();
            // available movement distances (always >= 0, no direction info)
            float availableViewX = dir < 0 ? viewX - viewMovingBounds.left : viewMovingBounds.right - viewX;
            float availablePagerX = dir * mViewPagerX < 0 ? Math.abs(mViewPagerX) : 0;

            // Not available if already overscrolled in same direction
            if (availableViewX < 0) availableViewX = 0;

            if (availablePagerX >= movementX) {
                // Only view pager is moved
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
            // We want viewpager to stop scrolling horizontally only if view has a small movement area and a vertical
            // scroll is detected.
            // We will allow passing dY to viewpager so it will be able to stop self if vertical scroll is detected.
            mIsAllowViewPagerScrollY = viewMovingBounds.width() < mTouchSlop;
        }

        boolean shouldFixViewX = mIsViewPagerInterceptedScroll;
        int actualX = performViewPagerScroll(e, dPagerX, dY);
        mViewPagerX += actualX;
        // Adding back scroll not handled by viewpager
        if (shouldFixViewX) dViewX += Math.round(dPagerX) - actualX;

        return dViewX;
    }


    private int computeInitialViewPagerX(View view, MotionEvent event) {
        // ViewPager can be in intermediate position, so we should recompute correct mViewPagerX value
        int scroll = mViewPager.getScrollX();
        int widthWithMargin = mViewPager.getWidth() + mViewPager.getPageMargin();

        // Child's event will be in local coordinates, but we want it in ViewPager's coordinates
        float viewPagerTouchX = event.getX();
        view.getLocationOnScreen(TMP_LOCATION);
        viewPagerTouchX += TMP_LOCATION[0];
        mViewPager.getLocationOnScreen(TMP_LOCATION);
        viewPagerTouchX -= TMP_LOCATION[0];

        int touchedItem = (int) ((scroll + viewPagerTouchX) / widthWithMargin);

        return widthWithMargin * touchedItem - scroll;
    }

    private void passEventToViewPager(MotionEvent e) {
        if (mViewPager == null) return;

        MotionEvent fixedEvent = obtainOnePointerEvent(e);
        fixedEvent.setLocation(mLastViewPagerEventX, mLastViewPagerEventY);

        // Note: end event should always pass to onTouchEvent method for viewpager to correctly settling

        if (mIsViewPagerInterceptedScroll) {
            mViewPager.onTouchEvent(fixedEvent);
        } else {
            mIsViewPagerInterceptedScroll = mViewPager.onInterceptTouchEvent(fixedEvent);
        }

        boolean isEndEvent = e.getActionMasked() == MotionEvent.ACTION_UP
                || e.getActionMasked() == MotionEvent.ACTION_CANCEL;

        if (isEndEvent && !mIsViewPagerInterceptedScroll) {
            // If viewpager is not settled we should force it to do so, fake drag will help here
            mViewPager.beginFakeDrag();
            mViewPager.endFakeDrag();
        }

        fixedEvent.recycle();
    }

    /**
     * Manually scrolls view pager and returns actual distance at which pager was scrolled
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

}
