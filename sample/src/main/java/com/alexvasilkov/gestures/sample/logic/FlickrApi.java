package com.alexvasilkov.gestures.sample.logic;

import com.alexvasilkov.events.EventResult;
import com.alexvasilkov.events.Events.Background;
import com.alexvasilkov.events.Events.Subscribe;
import com.googlecode.flickrjandroid.Flickr;
import com.googlecode.flickrjandroid.photos.Photo;
import com.googlecode.flickrjandroid.photos.PhotoList;
import com.googlecode.flickrjandroid.photos.SearchParameters;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FlickrApi {

    public static final String LOAD_IMAGES_EVENT = "LOAD_IMAGES_EVENT";

    private static final String API_KEY = "7f6035774a01a39f9056d6d7bde60002";
    private static final String SEARCH_QUERY = "nature";
    private static final int PER_PAGE = 30;
    private static final int MAX_PAGES = 5;
    private static final Set<String> PHOTO_PARAMS = new HashSet<>();

    static {
        PHOTO_PARAMS.add("url_m");
        PHOTO_PARAMS.add("url_l");
        PHOTO_PARAMS.add("owner_name");
    }

    private static final List<Photo> PHOTOS = new ArrayList<>();
    private static final List<PhotoList> PAGES = new ArrayList<>();

    @Background(singleThread = true)
    @Subscribe(LOAD_IMAGES_EVENT)
    private static synchronized EventResult loadImages(int count) throws Exception {
        boolean hasNext = hasNext();

        SearchParameters params = new SearchParameters();
        params.setText(SEARCH_QUERY);
        params.setSafeSearch(Flickr.SAFETYLEVEL_SAFE);
        params.setSort(SearchParameters.RELEVANCE);
        params.setLicense("9"); // Public Domain Dedication (CC0)
        params.setExtras(PHOTO_PARAMS);

        while (PHOTOS.size() < count && hasNext) {
            PhotoList loaded = new Flickr(API_KEY).getPhotosInterface()
                    .search(params, PER_PAGE, PAGES.size() + 1);

            PAGES.add(loaded);
            PHOTOS.addAll(loaded);

            hasNext = hasNext();
        }

        int resultSize;
        if (PHOTOS.size() >= count) {
            resultSize = count;
        } else {
            resultSize = PHOTOS.size();
        }

        List<Photo> result = new ArrayList<>(PHOTOS.subList(0, resultSize));
        if (!hasNext) {
            hasNext = PHOTOS.size() > count;
        }

        return EventResult.create().result(result, hasNext).build();
    }

    private static boolean hasNext() {
        if (PAGES.isEmpty()) {
            return true;
        } else if (PAGES.size() >= MAX_PAGES) {
            return false;
        } else {
            PhotoList page = PAGES.get(PAGES.size() - 1);
            return page.getPage() * page.getPerPage() < page.getTotal();
        }
    }

}
