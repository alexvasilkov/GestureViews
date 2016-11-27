package com.alexvasilkov.gestures.internal;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.TypedValue;
import android.view.View;

import com.alexvasilkov.gestures.GestureController;
import com.alexvasilkov.gestures.GestureController.StateSource;
import com.alexvasilkov.gestures.Settings;
import com.alexvasilkov.gestures.animation.ViewPositionAnimator;
import com.alexvasilkov.gestures.views.interfaces.AnimatorView;
import com.alexvasilkov.gestures.views.interfaces.GestureView;

import java.lang.reflect.Field;

public class DebugOverlay {

    private static final float STROKE_WIDTH = 2f;
    private static final float TEXT_SIZE = 16f;

    private static final Paint paint = new Paint();
    private static final RectF rect = new RectF();
    private static final Matrix matrix = new Matrix();

    private static Field stateSourceField;

    private DebugOverlay() {}

    public static void drawDebug(View view, Canvas canvas) {
        final GestureController controller = ((GestureView) view).getController();
        final ViewPositionAnimator animator = ((AnimatorView) view).getPositionAnimator();
        final Settings settings = controller.getSettings();
        final Context context = view.getContext();
        final int top = view.getPaddingTop();
        final int left = view.getPaddingLeft();
        final float stroke = toPixels(context, STROKE_WIDTH);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(stroke);

        // Viewport
        rect.set(0f, 0f, settings.getViewportW(), settings.getViewportH());
        drawRect(canvas, Color.GRAY, top, left);

        // Movement area
        rect.set(MovementBounds.getMovementAreaWithGravity(settings));
        drawRect(canvas, Color.GREEN, top, left);

        // Image bounds with rotation
        canvas.save();
        canvas.translate(left, top);
        controller.getState().get(matrix);
        canvas.concat(matrix);
        rect.set(0f, 0f, settings.getImageW(), settings.getImageH());
        paint.setStrokeWidth(stroke / controller.getState().getZoom());
        drawRect(canvas, Color.YELLOW, 0, 0);
        paint.setStrokeWidth(stroke);
        canvas.restore();

        // Image bounds
        rect.set(0f, 0f, settings.getImageW(), settings.getImageH());
        controller.getState().get(matrix);
        matrix.mapRect(rect);
        drawRect(canvas, Color.RED, top, left);

        // State source
        float pos = animator.getPositionState();
        if (pos == 1f || (pos == 0f && animator.isLeaving())) {
            paint.setTextSize(toPixels(context, TEXT_SIZE));
            paint.setTextAlign(Paint.Align.CENTER);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.CYAN);
            float dx = settings.getViewportW() * 0.5f + left;
            float dy = settings.getViewportH() * 0.5f + top;

            StateSource source = getStateSource(controller);
            canvas.drawText(source.name(), dx, dy, paint);

            if (source != StateSource.NONE) {
                view.invalidate();
            }
        }
    }

    private static void drawRect(Canvas canvas, int color, int top, int left) {
        float strokeHalf = paint.getStrokeWidth() * 0.5f;
        rect.offset(left, top);
        rect.inset(strokeHalf, strokeHalf);
        paint.setColor(color);
        canvas.drawRect(rect, paint);
    }

    private static float toPixels(Context context, float value) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value,
                context.getResources().getDisplayMetrics());
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
