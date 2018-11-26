package com.alexvasilkov.gestures.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageView;

import com.alexvasilkov.gestures.GestureController;
import com.alexvasilkov.gestures.GestureControllerForPager;
import com.alexvasilkov.gestures.Settings;
import com.alexvasilkov.gestures.State;
import com.alexvasilkov.gestures.animation.ViewPositionAnimator;
import com.alexvasilkov.gestures.internal.DebugOverlay;
import com.alexvasilkov.gestures.internal.GestureDebug;
import com.alexvasilkov.gestures.utils.ClipHelper;
import com.alexvasilkov.gestures.utils.CropUtils;
import com.alexvasilkov.gestures.views.interfaces.AnimatorView;
import com.alexvasilkov.gestures.views.interfaces.ClipBounds;
import com.alexvasilkov.gestures.views.interfaces.ClipView;
import com.alexvasilkov.gestures.views.interfaces.GestureView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * {@link ImageView} implementation controlled by {@link GestureController}
 * ({@link #getController()}).
 * <p>
 * View position can be animated with {@link ViewPositionAnimator}
 * ({@link #getPositionAnimator()}).
 */
public class GestureImageView extends ImageView
        implements GestureView, ClipView, ClipBounds, AnimatorView {

    private GestureControllerForPager controller;
    private final ClipHelper clipViewHelper = new ClipHelper(this);
    private final ClipHelper clipBoundsHelper = new ClipHelper(this);
    private final Matrix imageMatrix = new Matrix();

    private ViewPositionAnimator positionAnimator;

    public GestureImageView(Context context) {
        this(context, null, 0);
    }

    public GestureImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GestureImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        ensureControllerCreated();
        controller.getSettings().initFromAttributes(context, attrs);
        controller.addOnStateChangeListener(new GestureController.OnStateChangeListener() {
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
        if (controller == null) {
            controller = new GestureControllerForPager(this);
        }
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        clipBoundsHelper.onPreDraw(canvas);
        clipViewHelper.onPreDraw(canvas);
        super.draw(canvas);
        clipViewHelper.onPostDraw(canvas);
        clipBoundsHelper.onPostDraw(canvas);

        if (GestureDebug.isDrawDebugOverlay()) {
            DebugOverlay.drawDebug(this, canvas);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GestureControllerForPager getController() {
        return controller;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ViewPositionAnimator getPositionAnimator() {
        if (positionAnimator == null) {
            positionAnimator = new ViewPositionAnimator(this);
        }
        return positionAnimator;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clipView(@Nullable RectF rect, float rotation) {
        clipViewHelper.clipView(rect, rotation);
    }

    @Override
    public void clipBounds(@Nullable RectF rect) {
        clipBoundsHelper.clipView(rect, 0f);
    }

    /**
     * Crops bitmap as it is seen inside movement area: {@link Settings#setMovementArea(int, int)}.
     * Result will be delivered to provided snapshot listener.
     *
     * @param listener Snapshot listener
     * @deprecated Use {@link #crop()} method instead.
     */
    @SuppressWarnings({ "deprecation", "unused" }) // Public API
    @Deprecated
    public void getSnapshot(OnSnapshotLoadedListener listener) {
        if (getDrawable() != null) {
            listener.onSnapshotLoaded(crop());
        }
    }

    /**
     * Crops bitmap as it is seen inside movement area: {@link Settings#setMovementArea(int, int)}.
     * <p>
     * Note, that size of cropped bitmap may vary from size of movement area,
     * since we will crop part of original image at base zoom level (zoom == 1).
     *
     * @return Cropped bitmap or null, if no image is set to this image view or if
     * {@link OutOfMemoryError} error was thrown during cropping.
     */
    @Nullable
    public Bitmap crop() {
        return CropUtils.crop(getDrawable(), controller);
    }

    @SuppressLint("ClickableViewAccessibility") // performClick() will be called by controller
    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        return controller.onTouch(this, event);
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
        super.onSizeChanged(width, height, oldWidth, oldHeight);
        controller.getSettings().setViewport(width - getPaddingLeft() - getPaddingRight(),
                height - getPaddingTop() - getPaddingBottom());
        controller.resetState();
    }

    @Override
    public void setImageResource(int resId) {
        setImageDrawable(getDrawable(getContext(), resId));
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        super.setImageDrawable(drawable);

        // Method setImageDrawable can be called from super constructor,
        // so we have to ensure controller instance is created at this point.
        ensureControllerCreated();

        Settings settings = controller.getSettings();

        // Saving old image size
        float oldWidth = settings.getImageW();
        float oldHeight = settings.getImageH();

        // Setting image size
        if (drawable == null) {
            settings.setImage(0, 0);
        } else if (drawable.getIntrinsicWidth() == -1 || drawable.getIntrinsicHeight() == -1) {
            settings.setImage(settings.getMovementAreaW(), settings.getMovementAreaH());
        } else {
            settings.setImage(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        }

        // Getting new image size
        float newWidth = settings.getImageW();
        float newHeight = settings.getImageH();

        if (newWidth > 0f && newHeight > 0f && oldWidth > 0f && oldHeight > 0f) {
            float scaleFactor = Math.min(oldWidth / newWidth, oldHeight / newHeight);
            controller.getStateController().setTempZoomPatch(scaleFactor);
            controller.updateState();
            controller.getStateController().setTempZoomPatch(0f);
        } else {
            controller.resetState();
        }
    }

    protected void applyState(State state) {
        state.get(imageMatrix);
        setImageMatrix(imageMatrix);
    }


    @SuppressWarnings("deprecation")
    private static Drawable getDrawable(Context context, @DrawableRes int id) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return context.getDrawable(id);
        } else {
            return context.getResources().getDrawable(id);
        }
    }


    /**
     * @deprecated Use {@link #crop()} method instead.
     */
    @SuppressWarnings("WeakerAccess") // Public API
    @Deprecated
    public interface OnSnapshotLoadedListener {
        void onSnapshotLoaded(Bitmap bitmap);
    }

}
