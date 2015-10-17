package com.alexvasilkov.gestures.sample.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.View;

public abstract class EndlessRecyclerAdapter<VH extends RecyclerView.ViewHolder>
        extends RecyclerView.Adapter<VH> {

    private final RecyclerView.OnScrollListener mScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            loadNextItemsIfNeeded(recyclerView);
        }
    };

    private boolean mIsLoading;
    private boolean mIsError;

    private LoaderCallbacks mCallbacks;
    private int mLoadingOffset = 0;

    public final boolean isLoading() {
        return mIsLoading;
    }

    public final boolean isError() {
        return mIsError;
    }

    public void setCallbacks(LoaderCallbacks callbacks) {
        mCallbacks = callbacks;
    }

    public void setLoadingOffset(int loadingOffset) {
        mLoadingOffset = loadingOffset;
    }

    public void loadNextItems() {
        if (!mIsLoading && !mIsError && mCallbacks != null && mCallbacks.canLoadNextItems()) {
            mIsLoading = true;
            onLoadingStateChanged();
            mCallbacks.loadNextItems();
        }
    }

    public void reloadNextItemsIfError() {
        if (mIsError) {
            mIsError = false;
            onLoadingStateChanged();
            loadNextItems();
        }
    }

    public void onNextItemsLoaded() {
        if (mIsLoading) {
            mIsLoading = false;
            mIsError = false;
            onLoadingStateChanged();
        }
    }

    public void onNextItemsError() {
        if (mIsLoading) {
            mIsLoading = false;
            mIsError = true;
            onLoadingStateChanged();
        }
    }

    protected void onLoadingStateChanged() {
        // No-default-op
    }


    private void loadNextItemsIfNeeded(RecyclerView recyclerView) {
        if (!mIsLoading && !mIsError) {
            View lastVisibleChild = recyclerView.getChildAt(recyclerView.getChildCount() - 1);
            int lastVisiblePos = recyclerView.getChildAdapterPosition(lastVisibleChild);
            int total = getItemCount();

            if (lastVisiblePos >= total - mLoadingOffset) loadNextItems();
        }
    }


    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        recyclerView.addOnScrollListener(mScrollListener);
        loadNextItemsIfNeeded(recyclerView);
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        recyclerView.removeOnScrollListener(mScrollListener);
    }


    public interface LoaderCallbacks {
        boolean canLoadNextItems();

        void loadNextItems();
    }

}
