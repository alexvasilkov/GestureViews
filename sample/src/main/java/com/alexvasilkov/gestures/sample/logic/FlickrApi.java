package com.alexvasilkov.gestures.sample.logic;

import com.alexvasilkov.events.Event;
import com.alexvasilkov.events.EventCallback;
import com.alexvasilkov.events.Events;
import com.alexvasilkov.events.cache.MemoryCache;
import com.alexvasilkov.gestures.sample.R;
import com.alexvasilkov.gestures.sample.SampleApplication;
import com.bumptech.glide.Glide;
import com.googlecode.flickrjandroid.Flickr;
import com.googlecode.flickrjandroid.photos.Photo;
import com.googlecode.flickrjandroid.photos.PhotoList;

import java.util.HashSet;
import java.util.Set;

public class FlickrApi {

    private static final String API_KEY = "7f6035774a01a39f9056d6d7bde60002";
    private static final String USER_ID = "99771506@N00";
    private static final int THUMB_SIZE = 100;

    @Events.AsyncMethod(R.id.event_load_images)
    @Events.Cache(MemoryCache.class)
    private PhotoList loadImages(Event event) throws Exception {
        int page = event.getData(0);
        int perPage = event.getData(1);

        Set<String> extra = new HashSet<String>();
        extra.add("url_m");
        extra.add("url_l");

        return new Flickr(API_KEY).getPeopleInterface()
                .getPublicPhotos(USER_ID, extra, perPage, page);
    }

    @Events.Callback(R.id.event_load_images)
    private void onImagesLoaded(EventCallback callback) {
        switch (callback.getStatus()) {
            case RESULT:
                PhotoList page = callback.getResult();
                // Pre-loading thumbnails
                for (Photo photo : page) {
                    Glide.with(SampleApplication.getContext())
                            .load(photo.getThumbnailUrl()).downloadOnly(THUMB_SIZE, THUMB_SIZE);
                }
                break;
        }
    }

}
