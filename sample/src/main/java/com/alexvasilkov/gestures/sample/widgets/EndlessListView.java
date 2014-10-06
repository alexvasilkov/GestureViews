package com.alexvasilkov.gestures.sample.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListAdapter;
import android.widget.ListView;

public abstract class EndlessListView extends ListView implements OnScrollListener {

    private View mLoadingView;
    private View mErrorView;

    private boolean mIsLoading;
    private boolean mIsError;

    private EndlessListener mListener;
    private int mLoadingOffset = 0;

    public EndlessListView(Context context) {
        this(context, null, 0);
    }

    public EndlessListView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EndlessListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setOnScrollListener(this);
    }

    public void setEndlessListener(EndlessListener listener) {
        mListener = listener;
    }

    public void setLoadingOffset(int offset) {
        mLoadingOffset = offset;
    }

    @Override
    public void setAdapter(ListAdapter adapter) {
        mIsLoading = false;
        mIsError = false;
        updateFooter();
        super.setAdapter(adapter);
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (visibleItemCount + firstVisibleItem >= totalItemCount - mLoadingOffset && getAdapter() != null)
            loadNextPage();
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        // NO-OP
    }

    protected void loadNextPage() {
        if (!mIsLoading && !mIsError && mListener.canLoadNextPage()) {
            mIsLoading = true;
            updateFooter();
            mListener.onLoadNextPage();
        }
    }

    public void reloadNextPage() {
        mIsError = false;
        updateFooter();
        loadNextPage();
    }

    private void updateFooter() {
        onUpdateFooter(mIsLoading, mIsError);
    }

    protected abstract void onUpdateFooter(boolean isLoading, boolean isError);

    public void onNextPageLoaded() {
        mIsLoading = false;
        mIsError = false;
        updateFooter();
    }

    public void onNextPageFail() {
        mIsLoading = false;
        mIsError = true;
        updateFooter();
    }

    public interface EndlessListener {
        boolean canLoadNextPage();

        void onLoadNextPage();
    }

}