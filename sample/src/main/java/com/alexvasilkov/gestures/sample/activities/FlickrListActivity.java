package com.alexvasilkov.gestures.sample.activities;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;

import com.alexvasilkov.android.commons.utils.Views;
import com.alexvasilkov.events.Events;
import com.alexvasilkov.events.Events.Failure;
import com.alexvasilkov.events.Events.Result;
import com.alexvasilkov.gestures.sample.R;
import com.alexvasilkov.gestures.sample.items.FlickrListAdapter;
import com.alexvasilkov.gestures.sample.logic.FlickrApi;
import com.alexvasilkov.gestures.sample.views.PaginatedRecyclerView;
import com.googlecode.flickrjandroid.photos.PhotoList;

public class FlickrListActivity extends BaseActivity {

    private PaginatedRecyclerView mRecyclerView;
    private FlickrListAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_flickr_list);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mRecyclerView = Views.find(this, R.id.flickr_list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mRecyclerView.setLoadingText(getString(R.string.loading_images));
        mRecyclerView.setErrorText(getString(R.string.reload_images));

        // Endless loading
        mRecyclerView.setEndlessListener(new PaginatedRecyclerView.EndlessListener() {
            @Override
            public boolean canLoadNextPage() {
                return mAdapter.canLoadNext();
            }

            @Override
            public void onLoadNextPage() {
                Events.create(FlickrApi.LOAD_IMAGES_EVENT)
                        .param(mAdapter.getNextPageIndex(), PaginatedRecyclerView.PAGE_SIZE)
                        .post();
            }
        });

        mAdapter = new FlickrListAdapter(this);
        mRecyclerView.setAdapter(mAdapter);
    }

    @Result(FlickrApi.LOAD_IMAGES_EVENT)
    private void onPhotosLoaded(PhotoList page) {
        mAdapter.setNextPage(page);
        mRecyclerView.onNextPageLoaded();
    }

    @Failure(FlickrApi.LOAD_IMAGES_EVENT)
    private void onPhotosLoadFail() {
        mRecyclerView.onNextPageFail();
    }

}
