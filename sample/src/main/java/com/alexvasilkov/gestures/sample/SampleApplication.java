package com.alexvasilkov.gestures.sample;

import android.app.Application;

import com.alexvasilkov.events.Events;
import com.alexvasilkov.gestures.internal.GestureDebug;
import com.alexvasilkov.gestures.sample.demo.utils.FlickrApi;

public class SampleApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Events.register(FlickrApi.class);

        GestureDebug.setDebugFps(true);
        GestureDebug.setDebugAnimator(true);
    }

}
