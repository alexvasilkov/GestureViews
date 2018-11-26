package com.alexvasilkov.gestures.commons;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Build;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import com.alexvasilkov.gestures.Settings;
import com.alexvasilkov.gestures.internal.UnitsUtils;
import com.alexvasilkov.gestures.utils.GravityUtils;
import com.alexvasilkov.gestures.views.GestureImageView;

import androidx.annotation.ColorInt;

/**
 * View to draw movement area above {@link GestureImageView}, useful when implementing cropping.
 * <p>
 * To use this view you should set corresponding {@link Settings} with
 * {@link #setSettings(Settings)} method. Then whenever movement area is changed
 * (see {@link Settings#setMovementArea(int, int)}) you will need to call {@link #update()}
 * method to apply changes.
 * <p>
 * You may also use rounded corners with {@link #setRounded(boolean)} method, changes between
 * rounded and non-rounded mode can optionally be animated.
 *
 * @deprecated Use {@link CropAreaView} instead.
 */
@Deprecated
@SuppressWarnings("unused") // Kept for backward compatibility
public class FinderView extends View {

    public static final int DEFAULT_BACK_COLOR = Color.argb(128, 0, 0, 0);
    public static final int DEFAULT_BORDER_COLOR = Color.WHITE;
    public static final float DEFAULT_BORDER_WIDTH = 2f;

    // Temporary objects
    private static final Rect tmpRect = new Rect();

    private final RectF rect = new RectF();
    private float rounding = 0f;

    private final RectF strokeRect = new RectF();

    private final Paint paintStroke = new Paint();
    private final Paint paintClear = new Paint();

    private int backColor;
    private Settings settings;

    public FinderView(Context context) {
        this(context, null);
    }

    public FinderView(Context context, AttributeSet attrs) {
        super(context, attrs);

        paintClear.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        paintClear.setAntiAlias(true);

        paintStroke.setStyle(Paint.Style.STROKE);
        paintStroke.setAntiAlias(true);

        // Default values
        setBackColor(DEFAULT_BACK_COLOR);
        setBorderColor(DEFAULT_BORDER_COLOR);
        setBorderWidth(TypedValue.COMPLEX_UNIT_DIP, DEFAULT_BORDER_WIDTH);
    }

    /**
     * Sets background color. Default value is {@link #DEFAULT_BACK_COLOR}.
     *
     * @param color Background color
     */
    public void setBackColor(@ColorInt int color) {
        backColor = color;
    }

    /**
     * Sets borders color. Default value is {@link #DEFAULT_BORDER_COLOR}.
     *
     * @param color Finder area borders color
     */
    public void setBorderColor(@ColorInt int color) {
        paintStroke.setColor(color);
    }

    /**
     * Sets borders width in pixels. Default value is {@link #DEFAULT_BORDER_WIDTH} dp.
     *
     * @param width Finder area borders width in pixels
     */
    public void setBorderWidth(float width) {
        paintStroke.setStrokeWidth(width);
    }

    /**
     * Sets borders width in particular units. Default value is {@link #DEFAULT_BORDER_WIDTH} dp.
     *
     * @param unit One of {@link TypedValue}.COMPLEX_UNIT_* constants
     * @param width Finder area borders width in given unit
     */
    public void setBorderWidth(int unit, float width) {
        setBorderWidth(UnitsUtils.toPixels(getContext(), unit, width));
    }

    /**
     * Sets settings to get movement area from.
     *
     * @param settings Settings of the corresponding {@link GestureImageView}
     */
    public void setSettings(Settings settings) {
        this.settings = settings;
        update();
    }

    /**
     * Whether to round bounds' corners.
     *
     * @param rounded Whether finder area should be rounded or not
     */
    public void setRounded(boolean rounded) {
        rounding = rounded ? 1f : 0f;
        update();
    }

    /**
     * Applies area size, area position and corners rounding.
     *
     * @param animate This paratemter is ignored
     */
    public void update(boolean animate) {
        update();
    }

    /**
     * Updates finder area size and position. Should be called whenever
     * corresponding settings are changed, see {@link #setSettings(Settings)}
     */
    public void update() {
        if (settings != null && getWidth() > 0 && getHeight() > 0) {
            // Updating finder area rectangle
            GravityUtils.getMovementAreaPosition(settings, tmpRect);
            rect.set(tmpRect);
            rect.offset(getPaddingLeft(), getPaddingTop());

            // We want to stroke outside of finder rectangle, while by default stroke is centered
            strokeRect.set(rect);
            float halfStroke = 0.5f * paintStroke.getStrokeWidth();
            strokeRect.inset(-halfStroke, -halfStroke);

            invalidate();
        }
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
        update();
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void onDraw(Canvas canvas) {
        float rx = rounding * 0.5f * rect.width();
        float ry = rounding * 0.5f * rect.height();

        // Punching hole in background color requires offscreen drawing
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            canvas.saveLayer(0, 0, canvas.getWidth(), canvas.getHeight(), null);
        } else {
            canvas.saveLayer(0, 0, canvas.getWidth(), canvas.getHeight(), null, 0);
        }
        canvas.drawColor(backColor);
        canvas.drawRoundRect(rect, rx, ry, paintClear);
        canvas.restore();

        canvas.drawRoundRect(strokeRect, rx, ry, paintStroke);
    }

}
