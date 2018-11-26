package com.alexvasilkov.gestures.commons;

import android.content.Context;
import android.content.res.TypedArray;
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

import com.alexvasilkov.gestures.R;
import com.alexvasilkov.gestures.Settings;
import com.alexvasilkov.gestures.internal.AnimationEngine;
import com.alexvasilkov.gestures.internal.UnitsUtils;
import com.alexvasilkov.gestures.utils.FloatScroller;
import com.alexvasilkov.gestures.utils.GravityUtils;
import com.alexvasilkov.gestures.utils.MathUtils;
import com.alexvasilkov.gestures.views.GestureImageView;

import androidx.annotation.ColorInt;

/**
 * View to draw cropping area above {@link GestureImageView}, useful when implementing cropping.
 * <p>
 * To use this view you should set corresponding {@link GestureImageView} with
 * {@link #setImageView(GestureImageView)} method.
 * <p>
 * In order to control cropping area you should either use {@link #setAspect(float)} method
 * which will calculate cropping area based on cropping view size minus paddings or manually update
 * movement area (see {@link Settings#setMovementArea(int, int)}).
 * Once aspect or movement area is changed you should call {@link #update(boolean)} method
 * to apply changes with optional animation.
 * <p>
 * You may also use rounded corners with {@link #setRounded(boolean)} method, changes between
 * rounded and non-rounded mode can optionally be animated.
 */
@SuppressWarnings("unused") // Public API
public class CropAreaView extends View {

    private static final int BACK_COLOR = Color.argb(160, 0, 0, 0);
    private static final int BORDER_COLOR = Color.WHITE;
    private static final float BORDER_WIDTH_DP = 2f;
    private static final float NO_ASPECT = 0f;

    public static final float ORIGINAL_ASPECT = -1f;

    // Temporary objects
    private static final Rect tmpRect = new Rect();
    private static final RectF tmpRectF = new RectF();

    private final RectF areaRect = new RectF();
    private float rounding = 0f;

    private final RectF strokeRect = new RectF();

    private final RectF startRect = new RectF();
    private final RectF endRect = new RectF();
    private float startRounding;
    private float endRounding;

    private final Paint paint = new Paint();
    private final Paint paintClear = new Paint();

    private final FloatScroller stateScroller = new FloatScroller();
    private final AnimationEngine animationEngine = new LocalAnimationEngine();

    private int backColor;
    private int borderColor;
    private float borderWidth;
    private int horizontalRules;
    private int verticalRules;
    private float rulesWidth;
    private float aspect;

    private GestureImageView imageView;

    public CropAreaView(Context context) {
        this(context, null);
    }

    public CropAreaView(Context context, AttributeSet attrs) {
        super(context, attrs);

        paintClear.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        paintClear.setAntiAlias(true);

        paint.setAntiAlias(true);

        final float defaultBorderWidth =
                UnitsUtils.toPixels(getContext(), TypedValue.COMPLEX_UNIT_DIP, BORDER_WIDTH_DP);

        TypedArray arr = context.obtainStyledAttributes(attrs, R.styleable.CropAreaView);
        backColor = arr.getColor(R.styleable.CropAreaView_gest_backgroundColor, BACK_COLOR);
        borderColor = arr.getColor(R.styleable.CropAreaView_gest_borderColor, BORDER_COLOR);
        borderWidth = arr.getDimension(R.styleable.CropAreaView_gest_borderWidth,
                defaultBorderWidth);
        horizontalRules = arr.getInt(R.styleable.CropAreaView_gest_rulesHorizontal, 0);
        verticalRules = arr.getInt(R.styleable.CropAreaView_gest_rulesVertical, 0);
        rulesWidth = arr.getDimension(R.styleable.CropAreaView_gest_rulesWidth, 0f);
        boolean rounded = arr.getBoolean(R.styleable.CropAreaView_gest_rounded, false);
        aspect = arr.getFloat(R.styleable.CropAreaView_gest_aspect, NO_ASPECT);
        arr.recycle();

        rounding = endRounding = rounded ? 1f : 0f;
    }

    /**
     * Sets background color. Default color is #88000000.
     *
     * @param color Background color
     */
    public void setBackColor(@ColorInt int color) {
        backColor = color;
        invalidate();
    }

    /**
     * Sets borders color. Default color is white.
     *
     * @param color Cropping area borders color
     */
    public void setBorderColor(@ColorInt int color) {
        borderColor = color;
        invalidate();
    }

    /**
     * Sets borders width in pixels. Default value is 2dp.
     *
     * @param width Cropping area borders width in pixels
     */
    public void setBorderWidth(float width) {
        borderWidth = width;
        invalidate();
    }

    /**
     * Sets number of horizontal and vertical rules. No rules by default.
     *
     * @param horizontalRules Number of horizontal rules
     * @param verticalRules Number of vertical rules
     */
    public void setRulesCount(int horizontalRules, int verticalRules) {
        this.horizontalRules = horizontalRules;
        this.verticalRules = verticalRules;
        invalidate();
    }

    /**
     * Sets borders width in pixels. By default uses half of borders width.
     *
     * @param width Rules lines width in pixels
     */
    public void setRulesWidth(float width) {
        rulesWidth = width;
        invalidate();
    }

    /**
     * Whether to round bounds' corners or not.
     * <p>
     * Method {@link #update(boolean)} should be called to apply new config with optional animation.
     *
     * @param rounded Whether cropping area should be rounded or not
     */
    public void setRounded(boolean rounded) {
        startRounding = rounding;
        endRounding = rounded ? 1f : 0f;
    }

    /**
     * If set to a value &gt; 0 cropping area will be a maximum area fitting this view's paddings
     * and having specified aspect ratio.
     * <p>
     * Method {@link #update(boolean)} should be called to apply new config with optional animation.
     *
     * @param aspect Cropping area aspect ratio
     */
    public void setAspect(float aspect) {
        this.aspect = aspect;
    }

    public void setImageView(GestureImageView imageView) {
        this.imageView = imageView;

        // Setting required parameters
        imageView.getController().getSettings()
                .setFitMethod(Settings.Fit.OUTSIDE)
                .setFillViewport(true)
                .setFlingEnabled(false);

        update(false);
    }

    /**
     * Applies area size, area position and corners rounding with optional animation.
     *
     * @param animate Whether to animate changes when applying new cropping area settings
     */
    public void update(boolean animate) {
        final Settings settings =
                imageView == null ? null : imageView.getController().getSettings();

        if (settings != null && getWidth() > 0 && getHeight() > 0) {

            // If aspect is specified we will automatically adjust movement area settings
            if (aspect > 0f || aspect == ORIGINAL_ASPECT) {
                final int width = getWidth() - getPaddingLeft() - getPaddingRight();
                final int height = getHeight() - getPaddingTop() - getPaddingBottom();

                final float realAspect = aspect == ORIGINAL_ASPECT
                        ? settings.getImageW() / (float) settings.getImageH() : aspect;

                // Setting movement area
                if (realAspect > (float) width / (float) height) {
                    // Cropping area is wider
                    settings.setMovementArea(width, (int) (width / realAspect));
                } else {
                    // Cropping area is higher
                    settings.setMovementArea((int) (height * realAspect), height);
                }

                // Animating image to fit new movement area
                if (animate) {
                    imageView.getController().animateKeepInBounds();
                } else {
                    imageView.getController().updateState();
                }
            }

            startRect.set(areaRect);

            GravityUtils.getMovementAreaPosition(settings, tmpRect);
            endRect.set(tmpRect);

            stateScroller.forceFinished();

            if (animate) {
                stateScroller.setDuration(settings.getAnimationsDuration());
                stateScroller.startScroll(0f, 1f);
                animationEngine.start();
            } else {
                setBounds(endRect, endRounding);
            }
        }
    }

    private void setBounds(RectF rect, float rounding) {
        this.areaRect.set(rect);
        this.rounding = rounding;

        // We want to stroke outside of cropping rectangle, while by default stroke is centered
        strokeRect.set(rect);
        float halfStroke = 0.5f * borderWidth;
        strokeRect.inset(-halfStroke, -halfStroke);

        invalidate();
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
        update(false);
        if (imageView != null) {
            imageView.getController().resetState();
        }

        // Setting up cropping area for Android Studio preview mode
        if (isInEditMode()) {
            final float w = width - getPaddingLeft() - getPaddingRight();
            final float h = height - getPaddingTop() - getPaddingBottom();
            final float sizeX;
            final float sizeY;

            if (aspect <= 0f) {
                sizeX = width;
                sizeY = height;
            } else if (aspect > w / h) {
                sizeX = w;
                sizeY = w / aspect;
            } else {
                sizeX = h * aspect;
                sizeY = h;
            }

            areaRect.set(0.5f * (width - sizeX), 0.5f * (height - sizeY),
                    0.5f * (width + sizeX), 0.5f * (height + sizeY));
            strokeRect.set(areaRect);
        }
    }


    @Override
    protected void onDraw(Canvas canvas) {
        // Preview does not support offscreen drawing, so we'll always draw a rect hole
        if (rounding == 0f || isInEditMode()) {
            drawRectHole(canvas);
        } else {
            drawRoundedHole(canvas);
        }

        drawBorderAndRules(canvas);
    }

    // If cropping area is not rounded we can draw it much faster
    private void drawRectHole(Canvas canvas) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(backColor);

        // Top part
        tmpRectF.set(0f, 0f, canvas.getWidth(), areaRect.top);
        canvas.drawRect(tmpRectF, paint);

        // Bottom part
        tmpRectF.set(0f, areaRect.bottom, canvas.getWidth(), canvas.getHeight());
        canvas.drawRect(tmpRectF, paint);

        // Left part
        tmpRectF.set(0f, areaRect.top, areaRect.left, areaRect.bottom);
        canvas.drawRect(tmpRectF, paint);

        // Right part
        tmpRectF.set(areaRect.right, areaRect.top, canvas.getWidth(), areaRect.bottom);
        canvas.drawRect(tmpRectF, paint);
    }

    // If cropping area is rounded we have to use off-screen buffer to punch a hole
    @SuppressWarnings("deprecation")
    private void drawRoundedHole(Canvas canvas) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(backColor);

        // Punching hole in background color requires offscreen drawing
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            canvas.saveLayer(0, 0, canvas.getWidth(), canvas.getHeight(), null);
        } else {
            canvas.saveLayer(0, 0, canvas.getWidth(), canvas.getHeight(), null, 0);
        }

        canvas.drawPaint(paint);

        final float rx = rounding * 0.5f * areaRect.width();
        final float ry = rounding * 0.5f * areaRect.height();
        canvas.drawRoundRect(areaRect, rx, ry, paintClear);

        canvas.restore();
    }

    private void drawBorderAndRules(Canvas canvas) {
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(borderColor);
        paint.setStrokeWidth(rulesWidth == 0 ? 0.5f * borderWidth : rulesWidth);

        final float rx = rounding * 0.5f * areaRect.width();
        final float ry = rounding * 0.5f * areaRect.height();

        for (int i = 0; i < verticalRules; i++) {
            float px = areaRect.left + (i + 1) * (areaRect.width() / (verticalRules + 1));
            float dy = getRulesOffset(px, rx, ry, areaRect.left, areaRect.right);
            canvas.drawLine(px, areaRect.top + dy, px, areaRect.bottom - dy, paint);
        }
        for (int i = 0; i < horizontalRules; i++) {
            float py = areaRect.top + (i + 1) * (areaRect.height() / (horizontalRules + 1));
            float dx = getRulesOffset(py, ry, rx, areaRect.top, areaRect.bottom);
            canvas.drawLine(areaRect.left + dx, py, areaRect.right - dx, py, paint);
        }

        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(borderColor);
        paint.setStrokeWidth(borderWidth);
        canvas.drawRoundRect(strokeRect, rx, ry, paint);
    }

    private float getRulesOffset(float x, float rx, float ry, float start, float end) {
        // We need to calculate rules line offset to take into account rounded corners.
        // Cases for X and Y axis are symmetrical, so we can re-use this method for both.
        // Note: ellipse equation is dx^2/rx^2 + dy^2/ry^2 = 1

        float dx = 0f;
        if (x - start < rx) { // Left corner
            dx = start + rx - x; // > 0
        } else if (end - x < rx) { // Right corner
            dx = x - end + rx; // > 0
        }
        return rx == 0f ? 0f : ry * (1f - (float) Math.sqrt(1f - dx * dx / rx / rx));
    }


    private class LocalAnimationEngine extends AnimationEngine {
        LocalAnimationEngine() {
            super(CropAreaView.this);
        }

        @Override
        public boolean onStep() {
            if (!stateScroller.isFinished()) {
                stateScroller.computeScroll();
                float state = stateScroller.getCurr();
                MathUtils.interpolate(areaRect, startRect, endRect, state);
                float rounding = MathUtils.interpolate(startRounding, endRounding, state);
                setBounds(areaRect, rounding);
                return true;
            }
            return false;
        }
    }

}
