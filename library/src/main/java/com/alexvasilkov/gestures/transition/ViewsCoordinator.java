package com.alexvasilkov.gestures.transition;

import android.util.Log;
import android.view.View;

import com.alexvasilkov.gestures.animation.ViewPosition;
import com.alexvasilkov.gestures.internal.GestureDebug;
import com.alexvasilkov.gestures.views.interfaces.AnimatorView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

/**
 * Main purpose of this class is to synchronize views of same item in two different sources
 * to correctly start or update view transition animation.
 * <p>
 * I.e. we need to have both 'to' and 'from' views which represent same item in
 * {@link RecyclerView} and {@link ViewPager} to start transition between them.
 * But when {@link ViewPager} is scrolled we may also need to scroll {@link RecyclerView} to reveal
 * corresponding item's view.
 * <p>
 * Method {@link #request(Object)} should be called when particular item needs to be synced.
 * This method will trigger methods {@link OnRequestViewListener#onRequestView(Object)} of
 * listeners set by {@link #setFromListener(OnRequestViewListener) setFromListener} and
 * {@link #setToListener(OnRequestViewListener) setToListener} methods.
 * <p>
 * When views were requested this class starts waiting for 'from' and 'to' views to be provided
 * with {@link #setFromView(Object, View)} (or {@link #setFromPos(Object, ViewPosition)}) and
 * {@link #setToView(Object, AnimatorView)} methods. When both views are ready method
 * {@link OnViewsReadyListener#onViewsReady(Object)} will be triggered.
 */
@SuppressWarnings("WeakerAccess") // Public API (methods)
public class ViewsCoordinator<ID> {

    private static final String TAG = ViewsCoordinator.class.getSimpleName();

    private OnRequestViewListener<ID> fromListener;
    private OnRequestViewListener<ID> toListener;
    private OnViewsReadyListener<ID> readyListener;

    private ID requestedId;
    private ID fromId;
    private ID toId;

    private View fromView;
    private ViewPosition fromPos;
    private AnimatorView toView;

    public void setFromListener(@NonNull OnRequestViewListener<ID> listener) {
        fromListener = listener;
    }

    public void setToListener(@NonNull OnRequestViewListener<ID> listener) {
        toListener = listener;
    }

    @SuppressWarnings("unused") // Public API
    public void setReadyListener(@Nullable OnViewsReadyListener<ID> listener) {
        readyListener = listener;
    }

    public void request(@NonNull ID id) {
        if (fromListener == null) {
            throw new RuntimeException("'from' listener is not set");
        }
        if (toListener == null) {
            throw new RuntimeException("'to' listener is not set");
        }

        cleanupRequest();

        if (GestureDebug.isDebugAnimator()) {
            Log.d(TAG, "Requesting " + id);
        }

        requestedId = id;
        fromListener.onRequestView(id);
        toListener.onRequestView(id);
    }

    public ID getRequestedId() {
        return requestedId;
    }


    /**
     * @return 'From' view, if set.<br>
     * Note, that once {@link #onViewsReady(Object)} is called both this method and
     * {@link #getFromPos()} may still return null, see {@link #setFromNone(Object)}.
     */
    public View getFromView() {
        return fromView;
    }

    /**
     * @return 'From' position, if set.<br>
     * Note, that once {@link #onViewsReady(Object)} is called, both this method and
     * {@link #getFromView()} may still return null, see {@link #setFromNone(Object)}.
     */
    public ViewPosition getFromPos() {
        return fromPos;
    }

    /**
     * @return 'To' view, if set. Will not be null once {@link #onViewsReady(Object)} is called,
     * but before new request ({@link #request(Object)}).
     */
    public AnimatorView getToView() {
        return toView;
    }


    public void setFromView(@NonNull ID id, @NonNull View fromView) {
        setFromInternal(id, fromView, null);
    }

    @SuppressWarnings("unused") // Public API
    public void setFromPos(@NonNull ID id, @NonNull ViewPosition fromPos) {
        setFromInternal(id, null, fromPos);
    }

    /**
     * Notifies that 'from' view is ready even if there is no such view. Can be used in cases when
     * we know that there will be no 'from' view, but animation should be started anyway.
     *
     * @param id Item ID for related 'to' view
     */
    public void setFromNone(@NonNull ID id) {
        setFromInternal(id, null, null);
    }

    private void setFromInternal(@NonNull ID id, View fromView, ViewPosition fromPos) {
        if (requestedId == null || !requestedId.equals(id)) {
            return;
        }
        if (this.fromView == fromView && fromView != null) {
            return; // Already set
        }

        if (GestureDebug.isDebugAnimator()) {
            Log.d(TAG, "Setting 'from' view for " + id);
        }

        onFromViewChanged(fromView, fromPos);

        fromId = id;
        this.fromView = fromView;
        this.fromPos = fromPos;
        notifyWhenReady();
    }

    protected void onFromViewChanged(@Nullable View fromView, @Nullable ViewPosition fromPos) {
        // Can be overridden to setup views
    }


    public void setToView(@NonNull ID id, @NonNull AnimatorView toView) {
        if (requestedId == null || !requestedId.equals(id)) {
            return;
        }
        if (this.toView == toView) {
            return; // Already set
        }

        if (GestureDebug.isDebugAnimator()) {
            Log.d(TAG, "Setting 'to' view for " + id);
        }

        onToViewChanged(this.toView, toView);

        toId = id;
        this.toView = toView;
        notifyWhenReady();
    }

    protected void onToViewChanged(@Nullable AnimatorView old, @NonNull AnimatorView view) {
        // Can be overridden to setup views
    }


    public boolean isReady() {
        return requestedId != null && requestedId.equals(fromId) && requestedId.equals(toId);
    }

    private void notifyWhenReady() {
        if (isReady()) {
            onViewsReady(requestedId);
        }
    }

    /**
     * Called when both 'from' and 'to' views are ready for given index. At this point
     * transition is ready to be started.
     * <p>
     * Note, that this method will be called each time 'from' or 'to' views are changed.
     *
     * @param id Item ID used for views lookup
     * @see #getFromView()
     * @see #getFromPos()
     * @see #getToView()
     */
    protected void onViewsReady(@NonNull ID id) {
        if (readyListener != null) {
            readyListener.onViewsReady(id);
        }
    }

    protected void cleanupRequest() {
        if (requestedId == null) {
            return;
        }

        if (GestureDebug.isDebugAnimator()) {
            Log.d(TAG, "Cleaning up request " + requestedId);
        }

        fromView = null;
        fromPos = null;
        toView = null;
        requestedId = fromId = toId = null;
    }


    public interface OnRequestViewListener<ID> {
        /**
         * Implementation should find corresponding {@link View} (or {@link ViewPosition})
         * for given {@code index} and provide it back to {@link ViewsCoordinator}.
         * <p>
         * Note, that it may not be possible to provide view right now (i.e. because
         * we should scroll source view to reveal correct view), but it should be provided
         * as soon as it's ready.
         *
         * @param id Item ID for views lookup
         */
        void onRequestView(@NonNull ID id);
    }

    public interface OnViewsReadyListener<ID> {
        /**
         * Will be called when both 'from' and 'to' views for given item index are ready.
         * <p>
         * Note, that this method will be called each time 'from' or 'to' views are changed.
         *
         * @param id Item ID used for views lookup
         */
        void onViewsReady(@NonNull ID id);
    }

}
