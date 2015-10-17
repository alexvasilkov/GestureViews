package com.alexvasilkov.gestures.internal;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;

import com.alexvasilkov.gestures.Settings;
import com.alexvasilkov.gestures.internal.MovementBounds;

public class Snapshot {

    private final Bitmap mBitmap;
    private final Canvas mCanvas;

    public Snapshot(Settings settings, int paddingLeft, int paddingTop) {
        mBitmap = Bitmap.createBitmap(settings.getMovementAreaW(), settings.getMovementAreaH(),
                Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);

        Rect pos = MovementBounds.getMovementAreaWithGravity(settings);
        mCanvas.translate(-pos.left - paddingLeft, -pos.top - paddingTop);
    }

    public Canvas getCanvas() {
        return mCanvas;
    }

    public Bitmap getBitmap() {
        return mBitmap;
    }

}
