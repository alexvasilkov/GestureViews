package com.alexvasilkov.gestures.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageView;

import com.alexvasilkov.gestures.GestureController;
import com.alexvasilkov.gestures.GestureControllerForPager;
import com.alexvasilkov.gestures.State;
import com.alexvasilkov.gestures.internal.Snapshot;
import com.alexvasilkov.gestures.views.interfaces.ClipView;
import com.alexvasilkov.gestures.views.interfaces.GestureView;
import com.alexvasilkov.gestures.views.utils.ViewClipHelper;

/**
 * Gestures controlled ImageView
 */
public class GestureImageView extends ImageView implements GestureView, ClipView {

    private final GestureControllerForPager mController;
    private final ViewClipHelper mClipHelper = new ViewClipHelper(this);
    private final Matrix mImageMatrix = new Matrix();

    private OnSnapshotLoadedListener mSnapshotListener;

    public GestureImageView(Context context) {
        this(context, null, 0);
    }

    public GestureImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GestureImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        mController = new GestureControllerForPager(context, new GestureController.OnStateChangeListener() {
            @Override
            public void onStateChanged(State state) {
                applyState(state);
            }

            @Override
            public void onStateReset(State oldState, State newState) {
                applyState(newState);
            }
        });
        mController.attachToView(this);

        setScaleType(ImageView.ScaleType.MATRIX);
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        mClipHelper.onPreDraw(canvas);
        super.draw(canvas);
        mClipHelper.onPostDraw(canvas);

        if (mSnapshotListener != null) {
            Snapshot snapshot = new Snapshot(mController.getSettings());
            super.draw(snapshot.getCanvas());
            mSnapshotListener.onSnapshotLoaded(snapshot.getBitmap());
            mSnapshotListener = null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GestureControllerForPager getController() {
        return mController;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clipView(@Nullable RectF rect) {
        mClipHelper.clipView(rect);
    }

    public void getSnapshot(OnSnapshotLoadedListener listener) {
        mSnapshotListener = listener;
        invalidate();
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        return mController.onTouch(this, event);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mController.getSettings().setViewport(w - getPaddingLeft() - getPaddingRight(),
                h - getPaddingTop() - getPaddingBottom());
        mController.updateState();
    }

    @Override
    public void setImageResource(int resId) {
        setImageDrawable(getDrawable(getContext(), resId));
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        super.setImageDrawable(drawable);
        if (drawable == null) {
            mController.getSettings().setImage(0, 0);
        } else {
            mController.getSettings().setImage(
                    drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        }
        mController.resetState();
    }

    private void applyState(State state) {
        state.get(mImageMatrix);
        setImageMatrix(mImageMatrix);
    }


    @SuppressWarnings("deprecation")
    public static Drawable getDrawable(Context context, @DrawableRes int id) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return context.getDrawable(id);
        } else {
            return context.getResources().getDrawable(id);
        }
    }


    public interface OnSnapshotLoadedListener {
        void onSnapshotLoaded(Bitmap bitmap);
    }

}
