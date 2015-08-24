package com.alexvasilkov.gestures.sample;

import android.app.Application;
import android.content.Context;

import com.alexvasilkov.events.Events;
import com.alexvasilkov.gestures.internal.GestureDebug;
import com.alexvasilkov.gestures.sample.logic.FlickrApi;

public class SampleApplication extends Application {

    private static Context sAppContext;

    @Override
    public void onCreate() {
        super.onCreate();

        sAppContext = getApplicationContext();

        Events.init(this);
        Events.register(FlickrApi.class);

        GestureDebug.setDebugFps(true);
        GestureDebug.setDebugAnimator(true);
    }

    public static Context getContext() {
        return sAppContext;
    }

}
