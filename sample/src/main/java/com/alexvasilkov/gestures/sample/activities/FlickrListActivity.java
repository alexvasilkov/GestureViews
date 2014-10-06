package com.alexvasilkov.gestures.sample.activities;

import android.os.Bundle;

import com.alexvasilkov.android.commons.utils.Views;
import com.alexvasilkov.events.EventCallback;
import com.alexvasilkov.events.Events;
import com.alexvasilkov.gestures.sample.R;
import com.alexvasilkov.gestures.sample.items.FlickrListAdapter;
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
                Events.create(R.id.event_load_images)
                        .data(mAdapter.getNextPageIndex(), PaginatedListView.PAGE_SIZE).post();
            }
        });

        // Adapter
        mAdapter = new FlickrListAdapter(this);
        mListView.setAdapter(mAdapter);
    }

    @Events.Callback(R.id.event_load_images)
    private void onPhotosLoaded(EventCallback callback) {
        switch (callback.getStatus()) {
            case RESULT:
                PhotoList page = callback.getResult();
                mAdapter.setNextPage(page);
                mListView.onNextPageLoaded();
                break;
            case ERROR:
                mListView.onNextPageFail();
                break;
        }
    }

}
