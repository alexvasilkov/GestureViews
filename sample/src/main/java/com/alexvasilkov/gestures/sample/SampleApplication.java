package com.alexvasilkov.gestures.sample;

import android.app.Application;
import android.os.Build;

import androidx.appcompat.app.AppCompatDelegate;

import com.alexvasilkov.events.Events;
import com.alexvasilkov.gestures.internal.GestureDebug;
import com.alexvasilkov.gestures.sample.demo.utils.FlickrApi;

public class SampleApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Events.register(FlickrApi.class);

        GestureDebug.setDebugFps(BuildConfig.DEBUG);
        GestureDebug.setDebugAnimator(BuildConfig.DEBUG);

        if (Build.VERSION.SDK_INT <= 28) {
            // It looks like day night theme does not work well in old Android versions
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

}
