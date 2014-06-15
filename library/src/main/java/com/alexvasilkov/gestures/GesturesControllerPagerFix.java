package com.alexvasilkov.gestures;

import android.content.Context;
import android.graphics.Rect;
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

    private static boolean sIsGlobalMotionDetected;

    private static final View.OnTouchListener PAGER_TOUCH_LISTENER = new View.OnTouchListener() {
        private boolean mIsTouchInProgress;

        @Override
        public boolean onTouch(View view, MotionEvent event) {
            if (mIsTouchInProgress) return false; // No views handled this touch (view.dispatchTouchEvent below)

            // ViewPager will steal touch events during settling regardless of requestDisallowInterceptTouchEvent,
            // so we will prevent it here
            if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                ((ViewPager) view).requestDisallowInterceptTouchEvent(true);
                mIsTouchInProgress = true;
                view.dispatchTouchEvent(event);
                mIsTouchInProgress = false;
                return true;
            } else {
                return false;
            }
        }
    };

    private static final int[] TMP_LOCATION = new int[2];

    private final int mPagerSlopX, mPagerSlopY;

    private ViewPager mViewPager;

    private MotionEvent mTmpEvent;
    private boolean mIsScrollGestureDetected;
    private boolean mIsScrollingViewPager;
    private boolean mIsSkipViewPager;
    private float mAccumulateScrollX, mAccumulateScrollY;
    private int mViewPagerX;
    private int mLastViewPagerDragX;

    private boolean mIsLocalMotionDetected;
    private boolean mIsSkipNonPrimaryPointers;

    public GesturesControllerPagerFix(Context context, OnStateChangedListener listener) {
        super(context, listener);

        mPagerSlopX = ViewConfiguration.get(context).getScaledTouchSlop();
        mPagerSlopY = 2 * ViewConfiguration.get(context).getScaledTouchSlop();
    }

    public void fixViewPagerScroll(ViewPager pager) {
        mViewPager = pager;
        mViewPager.setOnTouchListener(PAGER_TOUCH_LISTENER);
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        if (sIsGlobalMotionDetected && !mIsLocalMotionDetected) return false; // Not our event, skip it

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
                sIsGlobalMotionDetected = mIsLocalMotionDetected = true;

                mViewPager.requestDisallowInterceptTouchEvent(true);

                mAccumulateScrollX = mAccumulateScrollY = 0f;
                mIsSkipViewPager = false;

                mViewPagerX = computeInitialViewPagerX(view, event);
                mIsScrollingViewPager = mViewPagerX != 0;
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                sIsGlobalMotionDetected = mIsLocalMotionDetected = false;
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
                break;

            case MotionEvent.ACTION_POINTER_UP:
                if (event.getPointerCount() == 2) { // only primary pointer left
                    mIsSkipNonPrimaryPointers = false;
                }
                break;
        }

        if (mIsSkipViewPager) return event; // No event adjusting is needed

        boolean isNonPrimaryPointerEvent = event.getActionMasked() == MotionEvent.ACTION_POINTER_DOWN
                || event.getActionMasked() == MotionEvent.ACTION_POINTER_UP;

        if (mIsSkipNonPrimaryPointers && isNonPrimaryPointerEvent) return null; // No event should be passed further

        mTmpEvent = mIsSkipNonPrimaryPointers ? obtainOnePointerEvent(event) : MotionEvent.obtain(event);
        // Applying offset to the returned event, offset will be calculated in scrollBy method below
        mTmpEvent.offsetLocation(mViewPagerX, 0f);
        return mTmpEvent;
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

    private MotionEvent obtainOnePointerEvent(MotionEvent e) {
        return MotionEvent.obtain(e.getDownTime(), e.getEventTime(), e.getAction(),
                e.getX(), e.getY(), e.getMetaState());
    }

    private void recycleTmpEvent() {
        if (mTmpEvent != null) {
            mTmpEvent.recycle();
            mTmpEvent = null;
        }
    }

    @Override
    public boolean onDown(MotionEvent e) {
        if (mViewPager != null && !mViewPager.isFakeDragging()) mViewPager.beginFakeDrag();
        mIsScrollGestureDetected = false;
        return super.onDown(e);
    }

    @Override
    protected void onUp(MotionEvent e) {
        if (mViewPager != null && mViewPager.isFakeDragging()) mViewPager.endFakeDrag();
        super.onUp(e);
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

            float fixedDistanceX = -scrollBy(-distanceX, -distanceY);
            // Skipping vertical movement if view pager is dragged
            float fixedDistanceY = mViewPagerX == 0 ? distanceY : 0f;

            return super.onScroll(e1, e2, fixedDistanceX, fixedDistanceY);
        }
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        if (mViewPagerX == 0) {
            return super.onFling(e1, e2, velocityX, velocityY);
        } else {
            // Repeating last drag for smoother paging
            performFakeDrag(mLastViewPagerDragX);
            return false;
        }
    }

    /**
     * Manually scrolls view pager and returns actual distance at which pager was scrolled
     */
    private int performFakeDrag(int dX) {
        if (!mViewPager.isFakeDragging()) return 0;

        int scrollBegin = mViewPager.getScrollX();
        mViewPager.fakeDragBy(dX);
        mLastViewPagerDragX = scrollBegin - mViewPager.getScrollX();
        return mLastViewPagerDragX;
    }

    /**
     * Scrolls view pager when view reached it's bounds. Returns distance (<= dX) at which view can be scrolled.
     * Here we will split given distance (dX) into movement of ViewPager and movement of view itself.
     */
    private float scrollBy(float dX, float dY) {
        if (mIsSkipViewPager) return dX;

        float dViewX, dPagerX;

        final State state = getState();
        final Rect viewMovingBounds = getStateController().getMovingBounds(state);

        if (getSettings().isEnabled()) {
            final float dir = Math.signum(dX);
            final float movementX = Math.abs(dX); // always >= 0, no direction info

            final float viewX = StateController.restrict(state.getX(), viewMovingBounds.left, viewMovingBounds.right);
            // available movement distances (always >= 0, no direction info)
            float availableViewX = dir < 0 ? viewX - viewMovingBounds.left : viewMovingBounds.right - viewX;
            float availablePagerX = dir * mViewPagerX < 0 ? Math.abs(mViewPagerX) : 0;

            if (availablePagerX >= movementX) {
                // Only view pager is moved
                dViewX = 0;
                dPagerX = movementX;
            } else {
                if (availableViewX + availablePagerX >= movementX) {
                    // Moving pager for full available distance and moving view for remaining distance
                    dViewX = movementX - availablePagerX;
                    dPagerX = availablePagerX;
                } else {
                    // Moving view for full available distance and moving pager for remaining distance
                    dViewX = availableViewX;
                    dPagerX = movementX - availableViewX;
                }
            }

            // Applying direction
            dViewX *= dir;
            dPagerX *= dir;
        } else {
            dPagerX = dX;
            dViewX = 0f;
        }

        // Checking vertical and horizontal thresholds
        if (!mIsScrollingViewPager && !mIsSkipViewPager) {
            if (viewMovingBounds.width() < mPagerSlopX) {
                // View have small horizontal movement area, checking if thresholds are passed
                mAccumulateScrollX += dPagerX;
                mAccumulateScrollY += dY;

                if (Math.abs(mAccumulateScrollX) > mPagerSlopX) {
                    // Scrolling view pager if movement in X axis passed threshold
                    mIsScrollingViewPager = true;
                    // Adjusting pager movement for smoother scrolling
                    dPagerX = Math.signum(mAccumulateScrollX) * (Math.abs(mAccumulateScrollX) - mPagerSlopX);
                } else if (Math.abs(mAccumulateScrollY) > mPagerSlopY) {
                    // Skipping view pager if movement in Y axis passed threshold
                    mIsSkipViewPager = true;
                }
            } else {
                // View have wide horizontal movement area - allow view pager to scroll
                mIsScrollingViewPager = true;
            }
        }

        if (mIsScrollingViewPager) {
            int x = Math.round(dPagerX);
            int actualX = performFakeDrag(x);
            mViewPagerX += actualX;
            dViewX += x - actualX;
        } else {
            dViewX += dPagerX;
        }

        return dViewX;
    }

}
