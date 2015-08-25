package com.alexvasilkov.gestures.animation;

import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import com.alexvasilkov.gestures.internal.GestureDebug;
import com.alexvasilkov.gestures.views.interfaces.AnimatorView;
import com.alexvasilkov.gestures.views.interfaces.GestureView;

/**
 * Main purpose of this class is to synchronize views of same item in two different sources
 * to correctly start or update view position animation.
 * <p/>
 * I.e. we need to have both 'to' and 'from' views which represent same item in
 * {@link RecyclerView} and {@link ViewPager} to start animation between them.
 * But when {@link ViewPager} is scrolled we may also need to scroll {@link RecyclerView} to reveal
 * corresponding item's view.
 */
public abstract class ViewsSyncHelper {

    protected static final String TAG = "GestureListAnimator";
    protected static final int NO_INDEX = -1;

    private RequestsListener mRequestsListener;

    private int mRequestedIndex = NO_INDEX, mFromIndex = NO_INDEX, toIndex = NO_INDEX;

    private View mFromView;
    private ViewPosition mFromPos;
    private AnimatorView mToView;

    protected void setRequestsListener(@NonNull RequestsListener requestsListener) {
        mRequestsListener = requestsListener;
    }

    protected void request(int index) {
        cleanupRequest();

        if (GestureDebug.isDebugAnimator()) Log.d(TAG, "Requesting " + index);

        mRequestedIndex = index;
        mRequestsListener.requestFromView(index);
        mRequestsListener.requestToView(index);
    }

    public View getFromView() {
        return mFromView;
    }

    public ViewPosition getFromPos() {
        return mFromPos;
    }

    public void setFromView(int index, @NonNull View fromView) {
        setFromInternal(index, fromView, null);
    }

    public void setFromPos(int index, @NonNull ViewPosition fromPos) {
        setFromInternal(index, null, fromPos);
    }

    private void setFromInternal(int index, View fromView, ViewPosition fromPos) {
        if (mRequestedIndex == NO_INDEX || mRequestedIndex != index) return;

        if (GestureDebug.isDebugAnimator()) Log.d(TAG, "Setting 'from' view for " + index);

        this.mFromIndex = index;
        this.mFromView = fromView;
        this.mFromPos = fromPos;
        notifyWhenReady();
    }

    public AnimatorView getToView() {
        return mToView;
    }

    public void setToView(int index, AnimatorView toView) {
        if (mRequestedIndex == NO_INDEX || mRequestedIndex != index) return;

        if (GestureDebug.isDebugAnimator()) Log.d(TAG, "Setting 'to' view for " + index);

        this.toIndex = index;
        this.mToView = toView;
        notifyWhenReady();
    }

    private void notifyWhenReady() {
        if (mRequestedIndex == NO_INDEX || mFromIndex != mRequestedIndex || toIndex != mRequestedIndex)
            return;

        onReady(mRequestedIndex);
    }

    protected void cleanupRequest() {
        if (GestureDebug.isDebugAnimator()) Log.d(TAG, "Cleaning up request " + mRequestedIndex);

        mFromView = null;
        mFromPos = null;
        mToView = null;
        mRequestedIndex = mFromIndex = toIndex = NO_INDEX;
    }

    protected void cancelRequests() {
        mRequestsListener.cancelRequests();
    }

    /**
     * Called when both 'from' and 'to' views are ready for given index. At this point
     * animation is ready to be started.
     * <p/>
     * Note, that this method will be called each time 'from' or 'to' views are changed.
     *
     * @see #getFromView()
     * @see #getFromPos()
     * @see #getToView()
     */
    protected void onReady(int index) {
        mRequestsListener.onViewsReady(index);
    }


    public interface RequestsListener {
        /**
         * Implementation should find corresponding 'from' {@link View} or {@link ViewPosition}
         * for given {@code index} and provide it using {@link #setFromView(int, View)}
         * or {@link #setFromPos(int, ViewPosition)} methods.
         * <p/>
         * Note, that it may not be possible to provide 'from' view right now (i.e. because
         * we should scroll {@link ListView} to reveal correct view), so receiver can provide
         * the view later when it will be ready.
         */
        void requestFromView(int index);

        /**
         * Implementation should find corresponding 'to' {@link GestureView} for given {@code index}
         * and provide it using {@link #setToView(int, AnimatorView)} method.
         * <p/>
         * Note, that it may not be possible to provide 'to' view right now (i.e. because
         * page from {@link ViewPager} is not yet created), so receiver can provide
         * the view later when it will be ready.
         */
        void requestToView(int index);

        /**
         * Implementation should skip all pending requests (see {@link #requestFromView(int)}
         * and {@link #requestToView(int)}).
         */
        void cancelRequests();

        /**
         * Will be called when both 'from' and 'to' views for given item index are ready.
         * <p/>
         * Note, that this method will be called each time 'from' or 'to' views are changed.
         */
        void onViewsReady(int index);
    }

}
