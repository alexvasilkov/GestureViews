package com.alexvasilkov.gestures.internal;

import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;

import com.alexvasilkov.gestures.Settings;
import com.alexvasilkov.gestures.State;
import com.alexvasilkov.gestures.utils.GravityUtils;
import com.alexvasilkov.gestures.utils.MathUtils;

/**
 * Encapsulates logic related to movement bounds restriction. It will also apply image gravity
 * provided by {@link Settings#getGravity()} method.
 * <p>
 * Movement bounds can be represented using regular rectangle most of the time. But if fit method
 * is set to {@link Settings.Fit#OUTSIDE} and image has rotation != 0 then movement bounds will be
 * a rotated rectangle. That will complicate restrictions logic a bit.
 */
public class MovementBounds {

    // Temporary objects
    private static final Matrix tmpMatrix = new Matrix();
    private static final float[] tmpPointArr = new float[2];
    private static final Point tmpPoint = new Point();
    private static final Rect tmpRect = new Rect();
    private static final RectF tmpRectF = new RectF();

    private final Settings settings;

    // State bounds parameters
    private final RectF bounds = new RectF();
    private float boundsRotation;
    private float boundsPivotX;
    private float boundsPivotY;

    public MovementBounds(Settings settings) {
        this.settings = settings;
    }

    /**
     * Calculating bounds for {@link State#x} &amp; {@link State#y} values to keep image within
     * viewport and taking image gravity into account (see {@link Settings#setGravity(int)}).
     *
     * @param state State for which to calculate movement bounds.
     * @return Current movement bounds object for calls chaining.
     */
    public MovementBounds set(State state) {
        RectF area = tmpRectF;
        GravityUtils.getMovementAreaPosition(settings, tmpRect);
        area.set(tmpRect);

        final Rect pos = tmpRect;

        if (settings.getFitMethod() == Settings.Fit.OUTSIDE) {
            // For OUTSIDE fit method we will rotate area rect instead of image rect,
            // that will help us correctly fit movement area inside image rect
            boundsRotation = state.getRotation();
            boundsPivotX = area.centerX();
            boundsPivotY = area.centerY();

            if (!State.equals(boundsRotation, 0f)) {
                tmpMatrix.setRotate(-boundsRotation, boundsPivotX, boundsPivotY);
                tmpMatrix.mapRect(area);
            }
        } else {
            boundsRotation = 0f;
            boundsPivotX = boundsPivotY = 0f;
        }

        state.get(tmpMatrix);
        if (!State.equals(boundsRotation, 0f)) {
            // Removing image rotation
            tmpMatrix.postRotate(-boundsRotation, boundsPivotX, boundsPivotY);
        }
        GravityUtils.getImagePosition(tmpMatrix, settings, pos);

        // Calculating movement bounds for top-left corner of the scaled image
        switch (settings.getBoundsType()) {
            case NORMAL:
                calculateNormalBounds(area, pos);
                break;
            case INSIDE:
                calculateInsideBounds(area, pos);
                break;
            case OUTSIDE:
                calculateOutsideBounds(area, pos);
                break;
            case PIVOT:
                calculatePivotBounds(pos);
                break;
            case NONE:
            default:
                // Infinite bounds with overflow prevention
                bounds.set(Integer.MIN_VALUE >> 2, Integer.MIN_VALUE >> 2,
                        Integer.MAX_VALUE >> 2, Integer.MAX_VALUE >> 2);
                break;
        }

        // We should also adjust bounds position, since top-left corner of rotated image rectangle
        // will be somewhere on the edge of non-rotated bounding rectangle.
        // Note: for OUTSIDE fit method image rotation was skipped above, so we will not need
        // to adjust bounds here.
        if (settings.getFitMethod() != Settings.Fit.OUTSIDE) {
            state.get(tmpMatrix);

            RectF imageRect = tmpRectF;
            imageRect.set(0, 0, settings.getImageW(), settings.getImageH());
            tmpMatrix.mapRect(imageRect);

            tmpPointArr[0] = tmpPointArr[1] = 0f;
            tmpMatrix.mapPoints(tmpPointArr);

            bounds.offset(tmpPointArr[0] - imageRect.left, tmpPointArr[1] - imageRect.top);
        }

        return this;
    }

    private void calculateNormalBounds(RectF area, Rect pos) {
        // horizontal bounds
        if (area.width() < pos.width()) {
            // image is bigger than movement area -> restricting image movement with moving area
            bounds.left = area.left - (pos.width() - area.width());
            bounds.right = area.left;
        } else {
            // image is smaller than viewport -> positioning image according to calculated gravity
            // and restricting image movement in this direction
            bounds.left = bounds.right = pos.left;
        }

        // vertical bounds
        if (area.height() < pos.height()) {
            // image is bigger than viewport -> restricting image movement with viewport bounds
            bounds.top = area.top - (pos.height() - area.height());
            bounds.bottom = area.top;
        } else {
            // image is smaller than viewport -> positioning image according to calculated gravity
            // and restricting image movement in this direction
            bounds.top = bounds.bottom = pos.top;
        }
    }

    private void calculateInsideBounds(RectF area, Rect pos) {
        // horizontal bounds
        if (area.width() < pos.width()) {
            // image is bigger than movement area -> restricting image movement with moving area
            bounds.left = area.left - (pos.width() - area.width());
            bounds.right = area.left;
        } else {
            // image is smaller than viewport -> allow image to move inside the area
            bounds.left = area.left;
            bounds.right = area.right - pos.width();
        }

        // vertical bounds
        if (area.height() < pos.height()) {
            // image is bigger than viewport -> restricting image movement with viewport bounds
            bounds.top = area.top - (pos.height() - area.height());
            bounds.bottom = area.top;
        } else {
            // image is smaller than viewport -> allow image to move inside the area
            bounds.top = area.top;
            bounds.bottom = area.bottom - pos.height();
        }
    }

    private void calculateOutsideBounds(RectF area, Rect pos) {
        bounds.left = area.left - pos.width();
        bounds.right = area.right;
        bounds.top = area.top - pos.height();
        bounds.bottom = area.bottom;
    }

    private void calculatePivotBounds(Rect pos) {
        GravityUtils.getDefaultPivot(settings, tmpPoint);
        tmpPointArr[0] = tmpPoint.x;
        tmpPointArr[1] = tmpPoint.y;

        if (!State.equals(boundsRotation, 0f)) {
            tmpMatrix.setRotate(-boundsRotation, boundsPivotX, boundsPivotY);
            tmpMatrix.mapPoints(tmpPointArr);
        }

        bounds.left = tmpPointArr[0] - pos.width();
        bounds.right = tmpPointArr[0];
        bounds.top = tmpPointArr[1] - pos.height();
        bounds.bottom = tmpPointArr[1];
    }


    public void extend(float x, float y) {
        tmpPointArr[0] = x;
        tmpPointArr[1] = y;

        if (boundsRotation != 0f) {
            // Rotating given point so we can add it to bounds
            tmpMatrix.setRotate(-boundsRotation, boundsPivotX, boundsPivotY);
            tmpMatrix.mapPoints(tmpPointArr);
        }

        bounds.union(tmpPointArr[0], tmpPointArr[1]);
    }


    public void getExternalBounds(RectF out) {
        if (boundsRotation == 0f) {
            out.set(bounds);
        } else {
            tmpMatrix.setRotate(boundsRotation, boundsPivotX, boundsPivotY);
            tmpMatrix.mapRect(out, bounds);
        }
    }

    /**
     * Restricts x &amp; y coordinates to current bounds (as calculated in {@link #set(State)}).
     *
     * @param x X coordinate
     * @param y Y coordinate
     * @param extraX Extra area bounds (horizontal)
     * @param extraY Extra area bounds (vertical)
     * @param out Output rectangle
     */
    public void restrict(float x, float y, float extraX, float extraY, PointF out) {
        tmpPointArr[0] = x;
        tmpPointArr[1] = y;

        if (boundsRotation != 0f) {
            // Rotating given point so we can apply rectangular bounds.
            tmpMatrix.setRotate(-boundsRotation, boundsPivotX, boundsPivotY);
            tmpMatrix.mapPoints(tmpPointArr);
        }

        // Applying restrictions
        tmpPointArr[0] = MathUtils.restrict(tmpPointArr[0],
                bounds.left - extraX, bounds.right + extraX);
        tmpPointArr[1] = MathUtils.restrict(tmpPointArr[1],
                bounds.top - extraY, bounds.bottom + extraY);

        if (boundsRotation != 0f) {
            // Rotating restricted point back to original coordinates
            tmpMatrix.setRotate(boundsRotation, boundsPivotX, boundsPivotY);
            tmpMatrix.mapPoints(tmpPointArr);
        }

        out.set(tmpPointArr[0], tmpPointArr[1]);
    }

    public void restrict(float x, float y, PointF out) {
        restrict(x, y, 0f, 0f, out);
    }

}
