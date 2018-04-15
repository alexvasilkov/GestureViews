package com.alexvasilkov.gestures.internal;

import android.graphics.Matrix;
import android.graphics.RectF;

import com.alexvasilkov.gestures.Settings;
import com.alexvasilkov.gestures.State;
import com.alexvasilkov.gestures.utils.MathUtils;

/**
 * Encapsulates logic related to movement bounds restriction. It will also apply image gravity
 * provided by {@link Settings#getGravity()} method.
 * <p>
 * Movement bounds can be represented using regular rectangle most of the time. But if fit method
 * is set to {@link Settings.Fit#OUTSIDE} and image has rotation != 0 then movement bounds will be
 * a rotated rectangle. That will complicate restrictions logic a bit.
 */
public class ZoomBounds {

    // Temporary objects
    private static final Matrix tmpMatrix = new Matrix();
    private static final RectF tmpRectF = new RectF();

    private final Settings settings;

    // State bounds parameters
    private float minZoom;
    private float maxZoom;
    private float fitZoom;

    public ZoomBounds(Settings settings) {
        this.settings = settings;
    }

    /**
     * Calculates min, max and "fit" zoom levels for given state and according to current settings.
     *
     * @param state State for which to calculate zoom bounds.
     * @return Current zoom bounds object for calls chaining.
     */
    public ZoomBounds set(State state) {
        float imageWidth = settings.getImageW();
        float imageHeight = settings.getImageH();

        float areaWidth = settings.getMovementAreaW();
        float areaHeight = settings.getMovementAreaH();

        if (imageWidth == 0f || imageHeight == 0f || areaWidth == 0f || areaHeight == 0f) {
            minZoom = maxZoom = fitZoom = 1f;
            return this;
        }

        minZoom = settings.getMinZoom();
        maxZoom = settings.getMaxZoom();

        final float rotation = state.getRotation();

        if (!State.equals(rotation, 0f)) {
            if (settings.getFitMethod() == Settings.Fit.OUTSIDE) {
                // Computing movement area size taking rotation into account. We need to inverse
                // rotation, since it will be applied to the area, not to the image itself.
                tmpMatrix.setRotate(-rotation);
                tmpRectF.set(0, 0, areaWidth, areaHeight);
                tmpMatrix.mapRect(tmpRectF);
                areaWidth = tmpRectF.width();
                areaHeight = tmpRectF.height();
            } else {
                // Computing image bounding size taking rotation into account.
                tmpMatrix.setRotate(rotation);
                tmpRectF.set(0, 0, imageWidth, imageHeight);
                tmpMatrix.mapRect(tmpRectF);
                imageWidth = tmpRectF.width();
                imageHeight = tmpRectF.height();
            }
        }

        switch (settings.getFitMethod()) {
            case HORIZONTAL:
                fitZoom = areaWidth / imageWidth;
                break;
            case VERTICAL:
                fitZoom = areaHeight / imageHeight;
                break;
            case INSIDE:
                fitZoom = Math.min(areaWidth / imageWidth, areaHeight / imageHeight);
                break;
            case OUTSIDE:
                fitZoom = Math.max(areaWidth / imageWidth, areaHeight / imageHeight);
                break;
            case NONE:
            default:
                fitZoom = minZoom > 0f ? minZoom : 1f;
        }

        if (minZoom <= 0f) {
            minZoom = fitZoom;
        }
        if (maxZoom <= 0f) {
            maxZoom = fitZoom;
        }

        if (fitZoom > maxZoom) {
            if (settings.isFillViewport()) {
                // zooming to fill entire viewport
                maxZoom = fitZoom;
            } else {
                // restricting fit zoom
                fitZoom = maxZoom;
            }
        }
        // Now we have: fitZoom <= maxZoom

        if (minZoom > maxZoom) {
            minZoom = maxZoom;
        }
        // Now we have: minZoom <= maxZoom

        if (fitZoom < minZoom) {
            if (settings.isFillViewport()) {
                // zooming to fill entire viewport
                minZoom = fitZoom;
            } else {
                // restricting fit zoom
                fitZoom = minZoom;
            }
        }
        // Now we have: minZoom <= fitZoom <= maxZoom
        return this;
    }


    public float getMinZoom() {
        return minZoom;
    }

    public float getMaxZoom() {
        return maxZoom;
    }

    public float getFitZoom() {
        return fitZoom;
    }

    public float restrict(float zoom, float extraZoom) {
        return MathUtils.restrict(zoom, minZoom / extraZoom, maxZoom * extraZoom);
    }

}
