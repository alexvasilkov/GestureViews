package com.alexvasilkov.gestures.sample.logic;

import com.alexvasilkov.events.EventResult;
import com.alexvasilkov.events.Events.Background;
import com.alexvasilkov.events.Events.Cache;
import com.alexvasilkov.events.Events.Result;
import com.alexvasilkov.events.Events.Subscribe;
import com.alexvasilkov.events.cache.MemoryCache;
import com.alexvasilkov.gestures.sample.SampleApplication;
import com.bumptech.glide.Glide;
import com.googlecode.flickrjandroid.Flickr;
import com.googlecode.flickrjandroid.photos.Photo;
import com.googlecode.flickrjandroid.photos.PhotoList;

import java.util.HashSet;
import java.util.Set;

public class FlickrApi {

    public static final String LOAD_IMAGES_EVENT = "LOAD_IMAGES_EVENT";

    private static final String API_KEY = "7f6035774a01a39f9056d6d7bde60002";
    private static final String USER_ID = "99771506@N00";
    private static final int THUMB_SIZE = 100;

    @Background
    @Cache(MemoryCache.class)
    @Subscribe(LOAD_IMAGES_EVENT)
    private static PhotoList loadImages(int page, int perPage) throws Exception {
        Set<String> extra = new HashSet<>();
        extra.add("url_m");
        extra.add("url_l");

        return new Flickr(API_KEY).getPeopleInterface()
                .getPublicPhotos(USER_ID, extra, perPage, page);
    }

    @Result(LOAD_IMAGES_EVENT)
    private static void onImagesLoaded(EventResult result) {
        PhotoList page = result.getResult(0);
        // Pre-loading thumbnails
        for (Photo photo : page) {
            Glide.with(SampleApplication.getContext())
                    .load(photo.getThumbnailUrl()).downloadOnly(THUMB_SIZE, THUMB_SIZE);
        }
    }

}
