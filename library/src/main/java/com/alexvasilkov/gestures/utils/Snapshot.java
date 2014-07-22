package com.alexvasilkov.gestures.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import com.alexvasilkov.gestures.Settings;

public class Snapshot {

    private Bitmap mBitmap;
    private Canvas mCanvas;

    public Snapshot(Settings settings) {
        mBitmap = Bitmap.createBitmap(settings.getMovementAreaW(), settings.getMovementAreaH(),
                Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);

        Rect pos = MovementBounds.getMovementAreaWithGravity(settings);
        mCanvas.translate(-pos.left, -pos.top);
    }

    public Canvas getCanvas() {
        return mCanvas;
    }

    public Bitmap getBitmap() {
        return mBitmap;
    }

}
