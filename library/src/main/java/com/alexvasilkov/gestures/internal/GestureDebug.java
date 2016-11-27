package com.alexvasilkov.gestures.internal;

public class GestureDebug {

    private static boolean debugFps;
    private static boolean debugAnimator;
    private static boolean drawDebugOverlay;

    private GestureDebug() {}

    @SuppressWarnings("WeakerAccess") // Public API (kinda)
    public static boolean isDebugFps() {
        return debugFps;
    }

    public static void setDebugFps(boolean debug) {
        debugFps = debug;
    }

    public static boolean isDebugAnimator() {
        return debugAnimator;
    }

    public static void setDebugAnimator(boolean debug) {
        debugAnimator = debug;
    }

    public static boolean isDrawDebugOverlay() {
        return drawDebugOverlay;
    }

    public static void setDrawDebugOverlay(boolean draw) {
        drawDebugOverlay = draw;
    }

}
