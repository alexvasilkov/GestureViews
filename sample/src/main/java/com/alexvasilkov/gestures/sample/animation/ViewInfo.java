package com.alexvasilkov.gestures.sample.animation;

import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import java.io.Serializable;

public class ViewInfo implements Serializable {

    private static final long serialVersionUID = -2834121097626168375L;

    private static final int[] TMP_LOCATION = new int[2];
    private static final Matrix TMP_MATRIX = new Matrix();
    private static final RectF TMP_SRC = new RectF(), TMP_DST = new RectF();

    public final int viewLeft, viewTop;
    public final int viewWidth, viewHeight;

    public final int imageLeft, imageTop;
    public final int imageWidth, imageHeight;

    public final Serializable data;

    public ViewInfo(ImageView view, Serializable data) {
        view.getLocationOnScreen(TMP_LOCATION);

        viewLeft = TMP_LOCATION[0] + view.getPaddingLeft();
        viewTop = TMP_LOCATION[1] + view.getPaddingTop();
        viewWidth = view.getWidth() - view.getPaddingLeft() - view.getPaddingRight();
        viewHeight = view.getHeight() - view.getPaddingTop() - view.getPaddingBottom();

        Drawable image = view.getDrawable();

        if (image == null) {
            imageLeft = viewLeft;
            imageTop = viewTop;
            imageWidth = viewWidth;
            imageHeight = viewHeight;
        } else {
            int w = image.getIntrinsicWidth(), h = image.getIntrinsicHeight();

            applyScaleType(view.getScaleType(), w, h, viewWidth, viewHeight,
                    view.getImageMatrix(), TMP_MATRIX);

            TMP_SRC.set(0, 0, w, h);
            TMP_MATRIX.mapRect(TMP_DST, TMP_SRC);

            imageLeft = (int) TMP_DST.left + viewLeft;
            imageTop = (int) TMP_DST.top + viewTop;
            imageWidth = (int) TMP_DST.width();
            imageHeight = (int) TMP_DST.height();
        }

        this.data = data;
    }


    /**
     * Helper method to calculate drawing matrix. Based on ImageView source code.
     */
    private static void applyScaleType(ImageView.ScaleType type,
                                       int dwidth, int dheight,
                                       int vwidth, int vheight,
                                       Matrix imageMatrix,
                                       Matrix matrix) {

        if (ImageView.ScaleType.CENTER == type) {
            // Center bitmap in view, no scaling.
            matrix.setTranslate((vwidth - dwidth) * 0.5f,
                    (vheight - dheight) * 0.5f);
        } else if (ImageView.ScaleType.CENTER_CROP == type) {
            float scale;
            float dx = 0, dy = 0;

            if (dwidth * vheight > vwidth * dheight) {
                scale = (float) vheight / (float) dheight;
                dx = (vwidth - dwidth * scale) * 0.5f;
            } else {
                scale = (float) vwidth / (float) dwidth;
                dy = (vheight - dheight * scale) * 0.5f;
            }

            matrix.setScale(scale, scale);
            matrix.postTranslate(dx, dy);
        } else if (ImageView.ScaleType.CENTER_INSIDE == type) {
            float scale;
            float dx;
            float dy;

            if (dwidth <= vwidth && dheight <= vheight) {
                scale = 1.0f;
            } else {
                scale = Math.min((float) vwidth / (float) dwidth,
                        (float) vheight / (float) dheight);
            }

            dx = (vwidth - dwidth * scale) * 0.5f;
            dy = (vheight - dheight * scale) * 0.5f;

            matrix.setScale(scale, scale);
            matrix.postTranslate(dx, dy);
        } else {
            Matrix.ScaleToFit scaleToFit = scaleTypeToScaleToFit(type);
            if (scaleToFit == null) {
                matrix.set(imageMatrix);
            } else {
                // Generate the required transform.
                TMP_SRC.set(0, 0, dwidth, dheight);
                TMP_DST.set(0, 0, vwidth, vheight);
                matrix.setRectToRect(TMP_SRC, TMP_DST, scaleToFit);
            }
        }
    }

    private static Matrix.ScaleToFit scaleTypeToScaleToFit(ImageView.ScaleType type) {
        switch (type) {
            case FIT_XY:
                return Matrix.ScaleToFit.FILL;
            case FIT_START:
                return Matrix.ScaleToFit.START;
            case FIT_CENTER:
                return Matrix.ScaleToFit.CENTER;
            case FIT_END:
                return Matrix.ScaleToFit.END;
            default:
                return null;
        }
    }

}
