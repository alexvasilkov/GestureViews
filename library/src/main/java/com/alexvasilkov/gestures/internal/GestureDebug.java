package com.alexvasilkov.gestures.internal;

public class GestureDebug {

    private static boolean sDebugFps;
    private static boolean sDebugAnimator;

    public static boolean isDebugFps() {
        return sDebugFps;
    }

    public static void setDebugFps(boolean debug) {
        sDebugFps = debug;
    }

    public static boolean isDebugAnimator() {
        return sDebugAnimator;
    }

    public static void setDebugAnimator(boolean debug) {
        sDebugAnimator = debug;
    }

}
