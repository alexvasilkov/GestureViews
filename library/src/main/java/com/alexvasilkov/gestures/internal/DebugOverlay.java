package com.alexvasilkov.gestures.internal;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.view.View;

import com.alexvasilkov.gestures.GestureController;
import com.alexvasilkov.gestures.GestureController.StateSource;
import com.alexvasilkov.gestures.Settings;
import com.alexvasilkov.gestures.animation.ViewPositionAnimator;
import com.alexvasilkov.gestures.utils.GravityUtils;
import com.alexvasilkov.gestures.views.interfaces.AnimatorView;
import com.alexvasilkov.gestures.views.interfaces.GestureView;

import java.lang.reflect.Field;
import java.util.Locale;

public class DebugOverlay {

    private static final float STROKE_WIDTH = 2f;
    private static final float TEXT_SIZE = 16f;

    private static final Paint paint = new Paint();
    private static final RectF rectF = new RectF();
    private static final Rect rect = new Rect();
    private static final Matrix matrix = new Matrix();

    private static Field stateSourceField;

    private DebugOverlay() {}

    public static void drawDebug(View view, Canvas canvas) {
        final GestureController controller = ((GestureView) view).getController();
        final ViewPositionAnimator animator = ((AnimatorView) view).getPositionAnimator();
        final Settings settings = controller.getSettings();
        final Context context = view.getContext();
        final float stroke = UnitsUtils.toPixels(context, STROKE_WIDTH);
        final float textSize = UnitsUtils.toPixels(context, TEXT_SIZE);

        canvas.save();
        canvas.translate(view.getPaddingLeft(), view.getPaddingTop());

        // Viewport
        rectF.set(0f, 0f, settings.getViewportW(), settings.getViewportH());
        drawRect(canvas, rectF, Color.GRAY, stroke);

        // Movement area
        GravityUtils.getMovementAreaPosition(settings, rect);
        rectF.set(rect);
        drawRect(canvas, rectF, Color.GREEN, stroke);

        // Image bounds with rotation
        controller.getState().get(matrix);
        canvas.save();
        canvas.concat(matrix);
        rectF.set(0f, 0f, settings.getImageW(), settings.getImageH());
        drawRect(canvas, rectF, Color.YELLOW, stroke / controller.getState().getZoom());
        canvas.restore();

        // Image bounds
        rectF.set(0f, 0f, settings.getImageW(), settings.getImageH());
        controller.getState().get(matrix);
        matrix.mapRect(rectF);
        drawRect(canvas, rectF, Color.RED, stroke);

        // State source
        float pos = animator.getPosition();
        if (pos == 1f || (pos == 0f && animator.isLeaving())) {
            final StateSource source = getStateSource(controller);

            drawText(canvas, settings, source.name(), Color.CYAN, textSize);

            if (source != StateSource.NONE) {
                view.invalidate();
            }
        } else if (pos > 0f) {
            String direction = animator.isLeaving() ? "EXIT" : "ENTER";
            String text = String.format(Locale.US, "%s %.0f%%", direction, pos * 100f);
            drawText(canvas, settings, text, Color.MAGENTA, textSize);
        }

        canvas.restore();
    }

    private static void drawRect(Canvas canvas, RectF rect, int color, float stroke) {
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(stroke);
        rectF.inset(0.5f * stroke, 0.5f * stroke);
        paint.setColor(color);
        canvas.drawRect(rect, paint);
    }

    private static void drawText(Canvas canvas, Settings settings, String text,
            int color, float textSize) {
        // Text settings
        paint.setTextSize(textSize);
        paint.setTypeface(Typeface.MONOSPACE);
        paint.setTextAlign(Paint.Align.CENTER);

        final float halfSize = 0.5f * textSize;

        // Computing text background
        paint.getTextBounds(text, 0, text.length(), rect);
        rectF.set(rect);
        rectF.offset(-rectF.centerX(), -rectF.centerY());
        GravityUtils.getMovementAreaPosition(settings, rect);
        rectF.offset(rect.centerX(), rect.centerY());
        rectF.inset(-halfSize, -halfSize);

        // Drawing background
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        canvas.drawRoundRect(rectF, halfSize, halfSize, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.GRAY);
        canvas.drawRoundRect(rectF, halfSize, halfSize, paint);

        // Drawing text
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(color);
        canvas.drawText(text, rectF.centerX(), rectF.bottom - halfSize, paint);
    }


    private static StateSource getStateSource(GestureController controller) {
        // We can't have public API for state source in GestureController,
        // since it is only make sense to get it through corresponding listener.
        // Getting field through reflection for debug purpose does not seem to be very bad.
        if (stateSourceField == null) {
            try {
                stateSourceField = GestureController.class.getDeclaredField("stateSource");
                stateSourceField.setAccessible(true);
            } catch (Exception ignored) {
            }
        }

        if (stateSourceField != null) {
            try {
                return (StateSource) stateSourceField.get(controller);
            } catch (Exception ignored) {
            }
        }

        return StateSource.NONE;
    }

}
