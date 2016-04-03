package com.alexvasilkov.gestures.internal;

import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.Gravity;

import com.alexvasilkov.gestures.Settings;
import com.alexvasilkov.gestures.State;
import com.alexvasilkov.gestures.StateController;

/**
 * Encapsulates logic related to movement bounds restriction. It will also apply image gravity
 * provided by {@link Settings#getGravity()} method.
 * <p/>
 * Movement bounds can be represented using regular rectangle most of the time. But if fit method
 * is set to {@link Settings.Fit#OUTSIDE} and image has rotation != 0 than movement bounds will be
 * a rotated rectangle. That will complicate restrictions logic a bit.
 */
public class MovementBounds {

    // Temporary objects
    private static final Matrix matrix = new Matrix();
    private static final float[] pointArr = new float[2];
    private static final PointF point = new PointF();

    private static final Rect rectTmp = new Rect();
    private static final RectF rectTmpF = new RectF();
    private static final RectF rectTmpArea = new RectF();

    private static final RectF rectExtBounds = new RectF();
    private static final Rect rectPos = new Rect();
    private static final Rect rectMovArea = new Rect();
    private static final Point pivot = new Point();

    // Movement bounds parameters
    private final RectF bounds = new RectF();
    private float rotation;
    private float pivotX;
    private float pivotY;


    /**
     * Restricts x & y coordinates to current bounds (see {@link #setup(State, Settings)}).
     */
    public PointF restrict(float x, float y, float overscrollX, float overscrollY) {
        pointArr[0] = x;
        pointArr[1] = y;

        if (rotation != 0f) {
            // Rotating given point so we can apply rectangular bounds.
            matrix.setRotate(-rotation, pivotX, pivotY);
            matrix.mapPoints(pointArr);
        }

        // Applying restrictions
        pointArr[0] = StateController.restrict(pointArr[0],
                bounds.left - overscrollX, bounds.right + overscrollX);
        pointArr[1] = StateController.restrict(pointArr[1],
                bounds.top - overscrollY, bounds.bottom + overscrollY);

        if (rotation != 0f) {
            // Rotating restricted point back to original coordinates
            matrix.setRotate(rotation, pivotX, pivotY);
            matrix.mapPoints(pointArr);
        }

        point.set(pointArr[0], pointArr[1]);
        return point;
    }

    public PointF restrict(float x, float y) {
        return restrict(x, y, 0f, 0f);
    }

    /**
     * Note: do not store returned rect since it will be reused again later by this method.
     */
    public RectF getExternalBounds() {
        if (rotation == 0f) {
            rectExtBounds.set(bounds);
        } else {
            matrix.setRotate(rotation, pivotX, pivotY);
            matrix.mapRect(rectExtBounds, bounds);
        }
        return rectExtBounds;
    }

    public void union(float x, float y) {
        pointArr[0] = x;
        pointArr[1] = y;

        if (rotation != 0f) {
            // Rotating given point so we can add it to bounds
            matrix.setRotate(-rotation, pivotX, pivotY);
            matrix.mapPoints(pointArr);
        }

        bounds.union(pointArr[0], pointArr[1]);
    }

    /**
     * Calculating bounds for {@link State#x} & {@link State#y} values to keep image within
     * viewport and taking image gravity into account (see {@link Settings#setGravity(int)}).
     */
    public void setup(State state, Settings settings) {
        RectF area = rectTmpArea;
        area.set(getMovementAreaWithGravity(settings));
        final Rect pos;

        if (settings.getFitMethod() == Settings.Fit.OUTSIDE) {
            // For OUTSIDE fit method we will rotate area rect instead of image rect,
            // that will help us correctly fit movement area inside image rect
            rotation = state.getRotation();
            pivotX = area.centerX();
            pivotY = area.centerY();

            state.get(matrix);
            matrix.postRotate(-rotation, pivotX, pivotY);
            pos = getPositionWithGravity(matrix, settings);

            matrix.setRotate(-rotation, pivotX, pivotY);
            matrix.mapRect(area);
        } else {
            rotation = 0f;

            state.get(matrix);
            pos = getPositionWithGravity(matrix, settings);
        }

        // Calculating movement bounds for top-left corner of the scaled image

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

        // We should also adjust bounds position, since top-left corner of rotated image rectangle
        // will be somewhere on the edge of non-rotated bounding rectangle.
        // Note: for OUTSIDE fit method image rotation was skipped above, so we will not need
        // to adjust bounds here.
        if (settings.getFitMethod() != Settings.Fit.OUTSIDE) {
            state.get(matrix);

            rectTmpF.set(0, 0, settings.getImageW(), settings.getImageH());
            matrix.mapRect(rectTmpF);

            pointArr[0] = pointArr[1] = 0f;
            matrix.mapPoints(pointArr);

            bounds.offset(pointArr[0] - rectTmpF.left, pointArr[1] - rectTmpF.top);
        }
    }

    public void set(MovementBounds bounds) {
        this.bounds.set(bounds.bounds);
        rotation = bounds.rotation;
        pivotX = bounds.pivotX;
        pivotY = bounds.pivotY;
    }


    public static void setupInitialMovement(State state, Settings settings) {
        state.get(matrix);
        Rect pos = getPositionWithGravity(matrix, settings);
        state.translateTo(pos.left, pos.top);
    }

    /**
     * Returns image position within the viewport area with gravity applied,
     * not taking into account image position specified by matrix.
     */
    private static Rect getPositionWithGravity(Matrix matrix, Settings settings) {
        rectTmpF.set(0, 0, settings.getImageW(), settings.getImageH());
        matrix.mapRect(rectTmpF);
        final int w = Math.round(rectTmpF.width());
        final int h = Math.round(rectTmpF.height());

        // Calculating image position basing on gravity
        rectTmp.set(0, 0, settings.getViewportW(), settings.getViewportH());
        Gravity.apply(settings.getGravity(), w, h, rectTmp, rectPos);

        return rectPos;
    }

    public static Rect getMovementAreaWithGravity(Settings settings) {
        // Calculating movement area position basing on gravity
        rectTmp.set(0, 0, settings.getViewportW(), settings.getViewportH());
        Gravity.apply(settings.getGravity(),
                settings.getMovementAreaW(), settings.getMovementAreaH(), rectTmp, rectMovArea);
        return rectMovArea;
    }

    public static Point getDefaultPivot(Settings settings) {
        // Calculating movement area position basing on gravity
        Rect movArea = getMovementAreaWithGravity(settings);
        Gravity.apply(settings.getGravity(), 0, 0, movArea, rectTmp);
        pivot.set(rectTmp.left, rectTmp.top);
        return pivot;
    }

}
