package com.alexvasilkov.gestures.animation;

import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import java.util.regex.Pattern;

/**
 * Helper class to compute and store view position used for transitions.
 * <p/>
 * It consists of {@link #view} rectangle, {@link #viewport} rectangle (view rectangle minus
 * paddings) and {@link #image} rectangle (position of the underlying image taking into account
 * {@link ImageView#getScaleType()}, or same as {@link #viewport} if view is not an
 * {@link ImageView} or if {@link ImageView#getDrawable()} is {@code null}).
 * All positions are in screen coordinates.
 * <p/>
 * To create instance of this class use {@link #from(View)} static method. But note, that view
 * should already be laid out and have correct {@link View#getWidth()} and {@link View#getHeight()}
 * values.
 * <p/>
 * You can also serialize and deserialize this class to string using {@link #pack()} and
 * {@link #unpack(String)} methods. This can be useful to pass view position between activities.
 */
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
    public final Rect image;

    private ViewPosition() {
        this.view = new Rect();
        this.viewport = new Rect();
        this.image = new Rect();
    }

    private ViewPosition(@NonNull Rect view, @NonNull Rect viewport, @NonNull Rect image) {
        this.view = view;
        this.viewport = viewport;
        this.image = image;
    }

    public void set(@NonNull ViewPosition pos) {
        this.view.set(pos.view);
        this.viewport.set(pos.viewport);
        this.image.set(pos.image);
    }

    /**
     * @return true if view position is changed, false otherwise
     */
    private boolean init(@NonNull View targetView) {
        // If view is not attached than we can't get it's position
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

        if (targetView instanceof ImageView) {
            ImageView imageView = (ImageView) targetView;
            Drawable drawable = imageView.getDrawable();

            if (drawable == null) {
                image.set(viewport);
            } else {
                int drawableWidth = drawable.getIntrinsicWidth();
                int drawableHeight = drawable.getIntrinsicHeight();

                // Getting image position within the view
                ImageViewHelper.applyScaleType(imageView.getScaleType(),
                        drawableWidth, drawableHeight, viewport.width(), viewport.height(),
                        imageView.getImageMatrix(), tmpMatrix);

                tmpSrc.set(0, 0, drawableWidth, drawableHeight);
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
     * @return true if view position is changed, false otherwise
     */
    public static boolean apply(@NonNull ViewPosition pos, @NonNull View view) {
        return pos.init(view);
    }

    /**
     * Packs this ViewPosition into string, which can be passed i.e. between activities.
     *
     * @see #unpack(String)
     */
    public String pack() {
        String viewStr = view.flattenToString();
        String viewportStr = viewport.flattenToString();
        String imageStr = image.flattenToString();
        return TextUtils.join(DELIMITER, new String[] { viewStr, viewportStr, imageStr });
    }

    /**
     * Restores ViewPosition from the string created by {@link #pack()} method.
     */
    @SuppressWarnings("unused") // Public API
    public static ViewPosition unpack(String str) {
        String[] parts = TextUtils.split(str, SPLIT_PATTERN);
        if (parts.length != 3) {
            throw new IllegalArgumentException("Wrong ViewPosition string: " + str);
        }

        Rect view = Rect.unflattenFromString(parts[0]);
        Rect viewport = Rect.unflattenFromString(parts[1]);
        Rect image = Rect.unflattenFromString(parts[2]);

        if (view == null || viewport == null || image == null) {
            throw new IllegalArgumentException("Wrong ViewPosition string: " + str);
        }

        return new ViewPosition(view, viewport, image);
    }

}
