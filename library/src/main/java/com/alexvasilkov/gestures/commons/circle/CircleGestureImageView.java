package com.alexvasilkov.gestures.commons.circle;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

import com.alexvasilkov.gestures.animation.ViewPositionAnimator;
import com.alexvasilkov.gestures.views.GestureImageView;

public class CircleGestureImageView extends GestureImageView {

    private static final int DEFAULT_PAINT_FLAGS = Paint.FILTER_BITMAP_FLAG | Paint.ANTI_ALIAS_FLAG;

    private final RectF clipRect = new RectF();
    private final Paint bitmapPaint = new Paint(DEFAULT_PAINT_FLAGS);

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

        getPositionAnimator().addPositionUpdateListener(
                new ViewPositionAnimator.PositionUpdateListener() {
                    @Override
                    public void onPositionUpdate(float state, boolean isLeaving) {
                        cornersState = state;
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

            canvas.concat(getImageMatrix());
            canvas.drawRoundRect(clipRect, rx, ry, bitmapPaint);
        }
    }

    @Override
    public void clipView(@Nullable RectF rect) {
        if (rect == null) {
            clipRect.setEmpty();
        } else {
            clipRect.set(rect);
        }
        super.clipView(rect);
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        super.setImageDrawable(drawable);
        setup();
    }

    public void setCircle(boolean isCircle) {
        this.isCircle = isCircle;
        setup();
    }

    @SuppressWarnings("unused") // Public API
    public boolean isCircle() {
        return isCircle;
    }

    private void setup() {
        Bitmap bitmap = isCircle ? getBitmapFromDrawable(getDrawable()) : null;
        if (bitmap != null) {
            bitmapPaint.setShader(new BitmapShader(bitmap, TileMode.CLAMP, TileMode.CLAMP));
        } else {
            bitmapPaint.setShader(null);
        }

        invalidate();
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
