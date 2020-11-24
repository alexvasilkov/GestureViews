package com.alexvasilkov.gestures.sample.ex.custom.scene;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.alexvasilkov.gestures.GestureController;
import com.alexvasilkov.gestures.Settings;
import com.alexvasilkov.gestures.State;
import com.alexvasilkov.gestures.views.interfaces.GestureView;

import java.util.ArrayList;
import java.util.List;

public class SceneView extends View implements GestureView {

    private static final float BORDER_WIDTH = 2f;

    private final List<Item> items = new ArrayList<>();
    private final SparseArray<Drawable> images = new SparseArray<>();

    private final GestureController controller = new GestureController(this);
    private final Matrix matrix = new Matrix();
    private final Matrix inverseMatrix = new Matrix();
    private final float[] point = new float[2];

    private final Paint paint = new Paint();

    private Item selected;

    public SceneView(Context context) {
        super(context);

        // Setting up borders paint
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.STROKE);

        // Gesture controller settings
        controller.getSettings()
                .setRotationEnabled(true)
                .setDoubleTapEnabled(false)
                .setFitMethod(Settings.Fit.INSIDE)
                .setBoundsType(Settings.Bounds.INSIDE)
                .setMinZoom(0.5f)
                .setMaxZoom(0f) // Max zoom level = fit zoom
                .setImage(1, 1); // Setting up fake image size for initial placement

        // Listening for and applying state changes
        controller.addOnStateChangeListener(new GestureController.OnStateChangeListener() {
            @Override
            public void onStateChanged(State state) {
                applyState(state);
            }

            @Override
            public void onStateReset(State oldState, State newState) {
                applyState(newState);
            }
        });

        // Handling items selection
        controller.setOnGesturesListener(new GestureController.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapUp(@NonNull MotionEvent event) {
                selectItem(event.getX(), event.getY());
                return true;
            }
        });
    }

    @NonNull
    @Override
    public GestureController getController() {
        return controller;
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
        super.onSizeChanged(width, height, oldWidth, oldHeight);

        // Updating viewport size
        controller.getSettings().setViewport(
                width - getPaddingLeft() - getPaddingRight(),
                height - getPaddingTop() - getPaddingBottom());
        controller.updateState();
    }

    @SuppressLint("ClickableViewAccessibility") // Will be handled by gestures controller
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return controller.onTouch(this, event);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.save();
        canvas.translate(getPaddingLeft(), getPaddingRight());

        // Drawing all the items
        for (Item item : items) {
            item.getState().get(matrix);

            canvas.save();
            canvas.concat(matrix);
            images.get(item.getImageId()).draw(canvas);
            canvas.restore();
        }

        // Drawing selected item's bounds
        if (selected != null) {
            controller.getState().get(matrix);
            paint.setStrokeWidth(BORDER_WIDTH / controller.getState().getZoom());
            canvas.save();
            canvas.concat(matrix);
            canvas.drawRect(images.get(selected.getImageId()).getBounds(), paint);
            canvas.restore();
        }

        canvas.restore();
    }


    void setItems(@NonNull List<Item> items) {
        this.items.clear();
        this.items.addAll(items);

        // Pre-loading items images
        this.images.clear();
        for (Item item : items) {
            Drawable image = ContextCompat.getDrawable(getContext(), item.getImageId());
            assert image != null;
            image.setBounds(0, 0, image.getIntrinsicWidth(), image.getIntrinsicHeight());
            images.put(item.getImageId(), image);
        }
    }


    private void applyState(State state) {
        // Applying state changes to currently selected item (if any)
        if (selected != null) {
            selected.getState().set(state);
            invalidate();
        }
    }

    private void selectItem(float eventX, float eventY) {
        // Getting first item (in backward order) which contains given click event
        for (int i = items.size() - 1; i >= 0; i--) {
            Item item = items.get(i);
            item.getState().get(matrix);
            matrix.invert(inverseMatrix);

            // Taking item's rotation into account
            point[0] = eventX;
            point[1] = eventY;
            inverseMatrix.mapPoints(point);
            int newX = Math.round(point[0]);
            int newY = Math.round(point[1]);

            // Checking if click event is within item's bounds
            Rect bounds = images.get(item.getImageId()).getBounds();
            if (bounds.contains(newX, newY)) {
                selectItem(item);
                return;
            }
        }

        selectItem(null);
    }

    private void selectItem(Item item) {
        // Finishing state animations and immediately applying state restrictions
        controller.stopAllAnimations();
        controller.updateState();

        selected = item;

        // Init controller from newly selected item
        if (item != null) {
            Rect bounds = images.get(item.getImageId()).getBounds();
            controller.getState().set(item.getState());
            controller.getSettings().setImage(bounds.width(), bounds.height());
        }

        invalidate();
    }

}
