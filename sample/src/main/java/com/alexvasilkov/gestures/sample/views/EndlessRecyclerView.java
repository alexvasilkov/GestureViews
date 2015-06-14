package com.alexvasilkov.gestures.sample.views;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;

public abstract class EndlessRecyclerView extends RecyclerView {

    private boolean mIsLoading;
    private boolean mIsError;

    private EndlessListener mListener;
    private int mLoadingOffset = 0;

    public EndlessRecyclerView(Context context) {
        this(context, null, 0);
    }

    public EndlessRecyclerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EndlessRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        addOnScrollListener(new OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                loadNextPageIfNeeded();
            }
        });
    }

    public void setEndlessListener(EndlessListener listener) {
        mListener = listener;
    }

    public void setLoadingOffset(int offset) {
        mLoadingOffset = offset;
    }

    @Override
    public void setAdapter(Adapter adapter) {
        mIsLoading = false;
        mIsError = false;
        updateState();

        super.setAdapter(adapter);

        loadNextPageIfNeeded();
    }

    private void loadNextPageIfNeeded() {
        if (getAdapter() == null) return;

        View lastVisibleChild = getChildAt(getChildCount() - 1);
        int lastVisiblePos = getChildAdapterPosition(lastVisibleChild);
        int total = getAdapter().getItemCount();

        if (lastVisiblePos >= total - mLoadingOffset) loadNextPage();
    }

    protected void loadNextPage() {
        if (!mIsLoading && !mIsError && mListener.canLoadNextPage()) {
            mIsLoading = true;
            updateState();
            mListener.onLoadNextPage();
        }
    }

    public void reloadNextPage() {
        mIsError = false;
        updateState();
        loadNextPage();
    }

    private void updateState() {
        onStateChanged(mIsLoading, mIsError);
    }

    protected abstract void onStateChanged(boolean isLoading, boolean isError);

    public void onNextPageLoaded() {
        mIsLoading = false;
        mIsError = false;
        updateState();
    }

    public void onNextPageFail() {
        mIsLoading = false;
        mIsError = true;
        updateState();
    }

    public interface EndlessListener {
        boolean canLoadNextPage();

        void onLoadNextPage();
    }

}