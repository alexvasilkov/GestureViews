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
import com.alexvasilkov.gestures.Settings;
import com.alexvasilkov.gestures.State;
import com.alexvasilkov.gestures.animation.ViewPositionAnimator;
import com.alexvasilkov.gestures.internal.CropUtils;
import com.alexvasilkov.gestures.views.interfaces.AnimatorView;
import com.alexvasilkov.gestures.views.interfaces.ClipView;
import com.alexvasilkov.gestures.views.interfaces.GestureView;
import com.alexvasilkov.gestures.views.utils.ViewClipHelper;

/**
 * {@link ImageView} implementation controlled by {@link GestureController}
 * ({@link #getController()}).
 * <p/>
 * View position can be animated with {@link ViewPositionAnimator}
 * ({@link #getPositionAnimator()}).
 */
public class GestureImageView extends ImageView implements GestureView, ClipView, AnimatorView {

    private GestureControllerForPager mController;
    private final ViewClipHelper mClipHelper = new ViewClipHelper(this);
    private final Matrix mImageMatrix = new Matrix();

    private ViewPositionAnimator mPositionAnimator;

    public GestureImageView(Context context) {
        this(context, null, 0);
    }

    public GestureImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GestureImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        ensureControllerCreated();
        mController.addOnStateChangeListener(new GestureController.OnStateChangeListener() {
            @Override
            public void onStateChanged(State state) {
                applyState(state);
            }

            @Override
            public void onStateReset(State oldState, State newState) {
                applyState(newState);
            }
        });

        setScaleType(ImageView.ScaleType.MATRIX);
    }

    private void ensureControllerCreated() {
        if (mController == null) {
            mController = new GestureControllerForPager(this);
        }
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        mClipHelper.onPreDraw(canvas);
        super.draw(canvas);
        mClipHelper.onPostDraw(canvas);
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
    public ViewPositionAnimator getPositionAnimator() {
        if (mPositionAnimator == null) {
            mPositionAnimator = new ViewPositionAnimator(this);
        }
        return mPositionAnimator;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clipView(@Nullable RectF rect) {
        mClipHelper.clipView(rect);
    }

    /**
     * Crops bitmap as it is seen inside movement area: {@link Settings#setMovementArea(int, int)}.
     * Result will be delivered to provided snapshot listener.
     *
     * @deprecated Use {@link #crop()} method instead.
     */
    @SuppressWarnings("deprecation")
    @Deprecated
    public void getSnapshot(OnSnapshotLoadedListener listener) {
        if (getDrawable() != null) {
            listener.onSnapshotLoaded(crop());
        }
    }

    /**
     * Crops bitmap as it is seen inside movement area: {@link Settings#setMovementArea(int, int)}.
     * <p/>
     * Note, that size of cropped bitmap may vary from size of movement area,
     * since we will crop part of original image at base zoom level (zoom == 1).
     *
     * @return Cropped bitmap or null, if no image is set to this image view or if
     * {@link OutOfMemoryError} error was thrown during cropping.
     */
    @Nullable
    public Bitmap crop() {
        return CropUtils.crop(getDrawable(), mController.getState(), mController.getSettings());
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
    public void setImageDrawable(Drawable dr) {
        super.setImageDrawable(dr);

        // Method setImageDrawable can be called from super constructor,
        // so we have to ensure controller instance is created at this point.
        ensureControllerCreated();

        Settings settings = mController.getSettings();
        int oldW = settings.getImageW(), oldH = settings.getImageH();

        if (dr == null) {
            settings.setImage(0, 0);
        } else if (dr.getIntrinsicWidth() == -1 || dr.getIntrinsicHeight() == -1) {
            settings.setImage(settings.getMovementAreaW(), settings.getMovementAreaH());
        } else {
            settings.setImage(dr.getIntrinsicWidth(), dr.getIntrinsicHeight());
        }

        if (oldW != settings.getImageW() || oldH != settings.getImageH()) {
            mController.resetState();
        }
    }

    protected void applyState(State state) {
        state.get(mImageMatrix);
        setImageMatrix(mImageMatrix);
    }


    @SuppressWarnings("deprecation")
    private static Drawable getDrawable(Context context, @DrawableRes int id) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return context.getDrawable(id);
        } else {
            return context.getResources().getDrawable(id);
        }
    }


    @Deprecated
    public interface OnSnapshotLoadedListener {
        void onSnapshotLoaded(Bitmap bitmap);
    }

}
