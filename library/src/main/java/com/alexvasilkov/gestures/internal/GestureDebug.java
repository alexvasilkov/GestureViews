package com.alexvasilkov.gestures.internal;

public class GestureDebug {

    private static boolean debugFps;
    private static boolean debugAnimator;

    private GestureDebug() {}

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

}
