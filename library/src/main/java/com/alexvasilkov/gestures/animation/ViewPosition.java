package com.alexvasilkov.gestures.animation;

import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import java.util.regex.Pattern;

import androidx.annotation.NonNull;

/**
 * Helper class to compute and store view position used for transitions.
 * <p>
 * It consists of {@link #view} rectangle, {@link #viewport} rectangle (view rectangle minus
 * padding), {@link #visible} rectangle (part of viewport which is visible on screen)
 * and {@link #image} rectangle (position of the underlying image taking into account
 * {@link ImageView#getScaleType()}, or same as {@link #viewport} if view is not an
 * {@link ImageView} or if {@link ImageView#getDrawable()} is {@code null}).
 * All positions are in screen coordinates.
 * <p>
 * To create instance of this class use {@link #from(View)} static method. But note, that view
 * should already be laid out and have correct {@link View#getWidth()} and {@link View#getHeight()}
 * values.
 * <p>
 * You can also serialize and deserialize this class to string using {@link #pack()} and
 * {@link #unpack(String)} methods. This can be useful to pass view position between activities.
 */
@SuppressWarnings("WeakerAccess") // Public API (fields and methods)
public class ViewPosition {

    private static final String DELIMITER = "#";
    private static final Pattern SPLIT_PATTERN = Pattern.compile(DELIMITER);

    private static final int[] tmpLocation = new int[2];
    private static final Matrix tmpMatrix = new Matrix();
    private static final RectF tmpSrc = new RectF();
    private static final RectF tmpDst = new RectF();

    private static final Rect tmpViewRect = new Rect();

    public final Rect view;
    public final Rect viewport;
    public final Rect visible;
    public final Rect image;

    private ViewPosition() {
        this.view = new Rect();
        this.viewport = new Rect();
        this.visible = new Rect();
        this.image = new Rect();
    }

    private ViewPosition(@NonNull Rect view, @NonNull Rect viewport,
            @NonNull Rect visible, @NonNull Rect image) {
        this.view = view;
        this.viewport = viewport;
        this.visible = visible;
        this.image = image;
    }

    public void set(@NonNull ViewPosition pos) {
        this.view.set(pos.view);
        this.viewport.set(pos.viewport);
        this.visible.set(pos.visible);
        this.image.set(pos.image);
    }

    /**
     * @param targetView View for which we want to get on-screen location
     * @return true if view position is changed, false otherwise
     */
    private boolean init(@NonNull View targetView) {
        // If view is not attached then we can't get it's position
        if (targetView.getWindowToken() == null) {
            return false;
        }

        tmpViewRect.set(view);

        targetView.getLocationOnScreen(tmpLocation);

        view.set(0, 0, targetView.getWidth(), targetView.getHeight());
        view.offset(tmpLocation[0], tmpLocation[1]);

        viewport.set(targetView.getPaddingLeft(),
                targetView.getPaddingTop(),
                targetView.getWidth() - targetView.getPaddingRight(),
                targetView.getHeight() - targetView.getPaddingBottom());
        viewport.offset(tmpLocation[0], tmpLocation[1]);

        boolean isVisible = targetView.getGlobalVisibleRect(visible);
        if (!isVisible) {
            // Assuming we are starting from center of invisible view
            visible.set(view.centerX(), view.centerY(), view.centerX() + 1, view.centerY() + 1);
        }

        if (targetView instanceof ImageView) {
            ImageView imageView = (ImageView) targetView;
            Drawable drawable = imageView.getDrawable();

            if (drawable == null) {
                image.set(viewport);
            } else {
                final int drawableWidth = drawable.getIntrinsicWidth();
                final int drawableHeight = drawable.getIntrinsicHeight();

                // Getting image position within the view
                ImageViewHelper.applyScaleType(imageView.getScaleType(),
                        drawableWidth, drawableHeight, viewport.width(), viewport.height(),
                        imageView.getImageMatrix(), tmpMatrix);

                tmpSrc.set(0f, 0f, drawableWidth, drawableHeight);
                tmpMatrix.mapRect(tmpDst, tmpSrc);

                // Calculating image position on screen
                image.left = viewport.left + (int) tmpDst.left;
                image.top = viewport.top + (int) tmpDst.top;
                image.right = viewport.left + (int) tmpDst.right;
                image.bottom = viewport.top + (int) tmpDst.bottom;
            }
        } else {
            image.set(viewport);
        }

        return !tmpViewRect.equals(view);
    }

    public static ViewPosition newInstance() {
        return new ViewPosition();
    }

    /**
     * Computes and returns view position. Note, that view should be already attached and laid out
     * before calling this method.
     *
     * @param view View for which we want to get on-screen location
     * @return View position
     */
    public static ViewPosition from(@NonNull View view) {
        ViewPosition pos = new ViewPosition();
        pos.init(view);
        return pos;
    }

    /**
     * Computes view position and stores it in given {@code pos}. Note, that view should be already
     * attached and laid out before calling this method.
     *
     * @param pos Output position
     * @param view View for which we want to get on-screen location
     * @return true if view position is changed, false otherwise
     */
    public static boolean apply(@NonNull ViewPosition pos, @NonNull View view) {
        return pos.init(view);
    }

    /**
     * Computes minimal view position for given point.
     *
     * @param pos Output view position
     * @param point Target point
     */
    public static void apply(@NonNull ViewPosition pos, @NonNull Point point) {
        pos.view.set(point.x, point.y, point.x + 1, point.y + 1);
        pos.viewport.set(pos.view);
        pos.visible.set(pos.view);
        pos.image.set(pos.view);
    }

    /**
     * Packs this ViewPosition into string, which can be passed i.e. between activities.
     *
     * @return Serialized position
     * @see #unpack(String)
     */
    public String pack() {
        String viewStr = view.flattenToString();
        String viewportStr = viewport.flattenToString();
        String visibleStr = visible.flattenToString();
        String imageStr = image.flattenToString();
        return TextUtils.join(DELIMITER, new String[] {
                viewStr, viewportStr, visibleStr, imageStr
        });
    }

    /**
     * Restores ViewPosition from the string created by {@link #pack()} method.
     *
     * @param str Serialized position string
     * @return De-serialized position
     */
    @SuppressWarnings("unused") // Public API
    public static ViewPosition unpack(String str) {
        String[] parts = TextUtils.split(str, SPLIT_PATTERN);
        if (parts.length != 4) {
            throw new IllegalArgumentException("Wrong ViewPosition string: " + str);
        }

        Rect view = Rect.unflattenFromString(parts[0]);
        Rect viewport = Rect.unflattenFromString(parts[1]);
        Rect visible = Rect.unflattenFromString(parts[2]);
        Rect image = Rect.unflattenFromString(parts[3]);

        if (view == null || viewport == null || image == null) {
            throw new IllegalArgumentException("Wrong ViewPosition string: " + str);
        }

        return new ViewPosition(view, viewport, visible, image);
    }

}
