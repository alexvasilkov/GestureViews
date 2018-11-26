package com.alexvasilkov.gestures.commons.circle;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import com.alexvasilkov.gestures.animation.ViewPositionAnimator.PositionUpdateListener;
import com.alexvasilkov.gestures.internal.DebugOverlay;
import com.alexvasilkov.gestures.internal.GestureDebug;
import com.alexvasilkov.gestures.utils.MathUtils;
import com.alexvasilkov.gestures.views.GestureImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class CircleGestureImageView extends GestureImageView {

    private static final int DEFAULT_PAINT_FLAGS = Paint.FILTER_BITMAP_FLAG | Paint.ANTI_ALIAS_FLAG;
    private static final Matrix tmpMatrix = new Matrix();

    private final Paint bitmapPaint = new Paint(DEFAULT_PAINT_FLAGS);

    private final RectF clipRect = new RectF();
    private float clipRotation;

    private boolean isCircle = true;
    private float cornersState;

    public CircleGestureImageView(Context context) {
        this(context, null, 0);
    }

    public CircleGestureImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircleGestureImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        getPositionAnimator().addPositionUpdateListener(new PositionUpdateListener() {
            @Override
            public void onPositionUpdate(float position, boolean isLeaving) {
                float interpolatedPosition = position / getPositionAnimator().getToPosition();
                cornersState = MathUtils.restrict(interpolatedPosition, 0f, 1f);
            }
        });
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        if (cornersState == 1f || clipRect.isEmpty() || bitmapPaint.getShader() == null) {
            super.draw(canvas);
        } else {
            float rx = 0.5f * clipRect.width() * (1f - cornersState);
            float ry = 0.5f * clipRect.height() * (1f - cornersState);

            canvas.rotate(clipRotation, clipRect.centerX(), clipRect.centerY());
            canvas.drawRoundRect(clipRect, rx, ry, bitmapPaint);
            canvas.rotate(-clipRotation, clipRect.centerX(), clipRect.centerY());

            if (GestureDebug.isDrawDebugOverlay()) {
                DebugOverlay.drawDebug(this, canvas);
            }
        }
    }

    @Override
    public void clipView(@Nullable RectF rect, float rotation) {
        if (rect == null) {
            clipRect.setEmpty();
        } else {
            clipRect.set(rect);
        }
        clipRotation = rotation;
        updateShaderMatrix();

        super.clipView(rect, rotation);
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        super.setImageDrawable(drawable);
        setup();
    }

    @SuppressWarnings("unused") // Public API
    public void setCircle(boolean isCircle) {
        this.isCircle = isCircle;
        setup();
    }

    @Override
    public void setImageMatrix(Matrix matrix) {
        super.setImageMatrix(matrix);
        updateShaderMatrix();
    }

    private void setup() {
        Bitmap bitmap = isCircle ? getBitmapFromDrawable(getDrawable()) : null;
        if (bitmap != null) {
            bitmapPaint.setShader(new BitmapShader(bitmap, TileMode.CLAMP, TileMode.CLAMP));
            updateShaderMatrix();
        } else {
            bitmapPaint.setShader(null);
        }

        invalidate();
    }

    private void updateShaderMatrix() {
        if (!clipRect.isEmpty() && bitmapPaint.getShader() != null) {
            getController().getState().get(tmpMatrix);

            // Including paddings & reverting rotation (will be applied later in draw() method)
            tmpMatrix.postTranslate(getPaddingLeft(), getPaddingTop());
            tmpMatrix.postRotate(-clipRotation, clipRect.centerX(), clipRect.centerY());

            bitmapPaint.getShader().setLocalMatrix(tmpMatrix);
        }
    }

    protected Bitmap getBitmapFromDrawable(Drawable drawable) {
        if (drawable == null) {
            return null;
        } else if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        } else {
            throw new RuntimeException("For better performance only BitmapDrawables are supported,"
                    + " but you can override getBitmapFromDrawable() and build bitmap on your own");
        }
    }

}
