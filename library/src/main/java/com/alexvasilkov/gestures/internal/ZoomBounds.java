package com.alexvasilkov.gestures.internal;

import android.graphics.Matrix;
import android.graphics.RectF;

import com.alexvasilkov.gestures.Settings;
import com.alexvasilkov.gestures.State;
import com.alexvasilkov.gestures.utils.MathUtils;

/**
 * Encapsulates logic related to movement bounds restriction. It will also apply image gravity
 * provided by {@link Settings#getGravity()} method.
 * <p/>
 * Movement bounds can be represented using regular rectangle most of the time. But if fit method
 * is set to {@link Settings.Fit#OUTSIDE} and image has rotation != 0 then movement bounds will be
 * a rotated rectangle. That will complicate restrictions logic a bit.
 */
public class ZoomBounds {

    // Temporary objects
    private static final Matrix tmpMatrix = new Matrix();
    private static final RectF tmpRectF = new RectF();

    // State bounds parameters
    private boolean isReady;
    private float minZoom;
    private float maxZoom;

    public void setup(State state, Settings settings) {
        isReady = settings.hasImageSize() && settings.hasViewportSize();
        if (isReady) {
            calculateZoomLevels(state, settings);
        } else {
            minZoom = maxZoom = 1f;
        }
    }

    /**
     * Calculates min and max zoom levels.
     */
    private void calculateZoomLevels(State state, Settings settings) {
        maxZoom = settings.getMaxZoom();

        float imageWidth = settings.getImageW();
        float imageHeight = settings.getImageH();

        float areaWidth = settings.getMovementAreaW();
        float areaHeight = settings.getMovementAreaH();

        final float rotation = state.getRotation();

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

        final float fittingZoom;

        switch (settings.getFitMethod()) {
            case HORIZONTAL:
                fittingZoom = areaWidth / imageWidth;
                break;
            case VERTICAL:
                fittingZoom = areaHeight / imageHeight;
                break;
            case OUTSIDE:
                fittingZoom = Math.max(areaWidth / imageWidth, areaHeight / imageHeight);
                break;
            case INSIDE:
            default:
                fittingZoom = Math.min(areaWidth / imageWidth, areaHeight / imageHeight);
                break;
        }

        if (fittingZoom > maxZoom) {
            if (settings.isFillViewport()) {
                // zooming to fill entire viewport
                minZoom = maxZoom = fittingZoom;
            } else {
                // restricting min zoom
                minZoom = maxZoom;
            }
        } else {
            minZoom = fittingZoom;
            if (!settings.isZoomEnabled()) {
                maxZoom = minZoom;
            }
        }
    }


    public boolean isReady() {
        return isReady;
    }

    public float getMinZoom() {
        return minZoom;
    }

    public float getMaxZoom() {
        return maxZoom;
    }

    public float restrict(float zoom, float extraZoom) {
        return MathUtils.restrict(zoom, minZoom / extraZoom, maxZoom * extraZoom);
    }

}
