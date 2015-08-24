package com.alexvasilkov.gestures.sample.views;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.alexvasilkov.android.commons.utils.Views;
import com.alexvasilkov.gestures.sample.R;
import com.alexvasilkov.gestures.sample.utils.recycler.AdapterWrapper;

public class PaginatedRecyclerView extends EndlessRecyclerView {

    private GridLayoutManager mGridManager;
    private AdapterWrapper<?> mWrappedAdapter;

    private final FooterAdapter mFooter = new FooterAdapter();
    private String mLoadingText, mErrorText;
    private boolean mIsLoading, mIsError;

    public PaginatedRecyclerView(Context context) {
        this(context, null, 0);
    }

    public PaginatedRecyclerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PaginatedRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void setLayoutManager(LayoutManager layout) {
        super.setLayoutManager(layout);

        mGridManager = layout instanceof GridLayoutManager ? (GridLayoutManager) layout : null;
        initGridSpanSizes();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void setAdapter(Adapter adapter) {
        super.setAdapter(mWrappedAdapter = new AdapterWrapper<>(adapter));
        initGridSpanSizes();
    }

    private void initGridSpanSizes() {
        if (mGridManager != null && mWrappedAdapter != null)
            mGridManager.setSpanSizeLookup(mWrappedAdapter.getGridSpanSizes(mGridManager));
    }

    public void setLoadingText(String text) {
        mLoadingText = text;
    }

    public void setErrorText(String text) {
        mErrorText = text;
    }

    @Override
    protected void onStateChanged(boolean isLoading, boolean isError) {
        mIsLoading = isLoading;
        mIsError = isError;

        if (isLoading || isError) {
            mWrappedAdapter.addFooter(mFooter);
        } else {
            mWrappedAdapter.removeFooter(mFooter);
        }

        mWrappedAdapter.updateFooter(mFooter);
    }


    private class FooterAdapter implements AdapterWrapper.ItemAdapter<FooterHolder> {
        @Override
        public FooterHolder onCreateViewHolder(ViewGroup parent) {
            FooterHolder holder = new FooterHolder(parent);
            holder.error.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(@NonNull View view) {
                    reloadNextPage();
                }
            });
            return holder;
        }

        @Override
        public void onBindViewHolder(FooterHolder holder) {
            holder.loadingText.setText(mLoadingText);
            holder.error.setText(mErrorText);

            holder.progress.setVisibility(mIsLoading ? View.VISIBLE : View.INVISIBLE);
            holder.error.setVisibility(mIsError ? View.VISIBLE : View.INVISIBLE);
        }
    }

    private static class FooterHolder extends ViewHolder {
        final View progress;
        final TextView loadingText;
        final TextView error;

        public FooterHolder(ViewGroup parent) {
            super(Views.inflate(parent, R.layout.item_footer));

            progress = Views.find(itemView, R.id.footer_progress);
            loadingText = Views.find(itemView, R.id.loading_text);
            error = Views.find(itemView, R.id.footer_error);
        }
    }

}
