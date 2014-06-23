package com.alexvasilkov.gestures.sample.utils;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.Build;
import com.squareup.picasso.LruCache;
import com.squareup.picasso.Picasso;

public class PicassoHelper {

    private static Picasso sInstance;

    public static Picasso get(Context context) {
        if (sInstance == null) {
            Context appContext = context.getApplicationContext();
            LruCache cache = new LruCache(calculateMemoryCacheSize(appContext));
            sInstance = new Picasso.Builder(appContext).memoryCache(cache).build();
        }
        return sInstance;
    }

    private static int calculateMemoryCacheSize(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        boolean largeHeap = (context.getApplicationInfo().flags & ApplicationInfo.FLAG_LARGE_HEAP) != 0;
        int memoryClass = am.getMemoryClass();
        if (largeHeap && Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            memoryClass = ActivityManagerHoneycomb.getLargeMemoryClass(am);
        }
        // Target 50% of the available heap.
        return 1024 * 1024 * memoryClass / 2;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private static class ActivityManagerHoneycomb {
        static int getLargeMemoryClass(ActivityManager activityManager) {
            return activityManager.getLargeMemoryClass();
        }
    }
}
