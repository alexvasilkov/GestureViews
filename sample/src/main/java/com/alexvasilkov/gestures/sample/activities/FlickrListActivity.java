package com.alexvasilkov.gestures.sample.activities;

import android.os.Bundle;

import com.alexvasilkov.android.commons.utils.Views;
import com.alexvasilkov.events.Events;
import com.alexvasilkov.events.Events.Failure;
import com.alexvasilkov.events.Events.Result;
import com.alexvasilkov.gestures.sample.R;
import com.alexvasilkov.gestures.sample.items.FlickrListAdapter;
import com.alexvasilkov.gestures.sample.logic.FlickrApi;
import com.alexvasilkov.gestures.sample.widgets.EndlessListView;
import com.alexvasilkov.gestures.sample.widgets.PaginatedListView;
import com.googlecode.flickrjandroid.photos.PhotoList;

public class FlickrListActivity extends BaseActivity {

    private PaginatedListView mListView;
    private FlickrListAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_flickr_list);

        mListView = Views.find(this, R.id.flickr_list);
        mListView.setLoadingText(getString(R.string.loading_images));
        mListView.setErrorText(getString(R.string.error_loading_images));

        // Endless loading
        mListView.setEndlessListener(new EndlessListView.EndlessListener() {
            @Override
            public boolean canLoadNextPage() {
                return mAdapter.canLoadNext();
            }

            @Override
            public void onLoadNextPage() {
                Events.create(FlickrApi.LOAD_IMAGES_EVENT)
                        .param(mAdapter.getNextPageIndex(), PaginatedListView.PAGE_SIZE)
                        .post();
            }
        });

        mAdapter = new FlickrListAdapter(this);
        mListView.setAdapter(mAdapter);
    }

    @Result(FlickrApi.LOAD_IMAGES_EVENT)
    private void onPhotosLoaded(PhotoList page) {
        mAdapter.setNextPage(page);
        mListView.onNextPageLoaded();
    }

    @Failure(FlickrApi.LOAD_IMAGES_EVENT)
    private void onPhotosLoadFail() {
        mListView.onNextPageFail();
    }

}
