package com.alexvasilkov.gestures.sample;

import android.app.Application;
import android.content.Context;

import com.alexvasilkov.events.Events;
import com.alexvasilkov.gestures.sample.logic.FlickrApi;

public class SampleApplication extends Application {

    private static Context sAppContext;

    @Override
    public void onCreate() {
        super.onCreate();

        sAppContext = getApplicationContext();

        Events.setAppContext(this);
        Events.register(new FlickrApi());
    }

    public static Context getContext() {
        return sAppContext;
    }

}
