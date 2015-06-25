package com.alexvasilkov.gestures.sample.views;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.alexvasilkov.android.commons.utils.Views;
import com.alexvasilkov.gestures.sample.R;
import com.alexvasilkov.gestures.sample.utils.recycler.AdapterWrapper;

public class PaginatedRecyclerView extends EndlessRecyclerView {

    public static final int PAGE_SIZE = 30;

    private static final long ERROR_CLICK_DELAY = 120;

    private final Runnable mErrorClickAction = new Runnable() {
        @Override
        public void run() {
            reloadNextPage();
        }
    };

    private boolean mIsFooterAdded;
    private View mFooter;
    private View mFooterProgress;
    private TextView mLoadingText;
    private TextView mFooterError;

    private GridLayoutManager mGridManager;
    private AdapterWrapper<?> mWrappedAdapter;

    public PaginatedRecyclerView(Context context) {
        this(context, null, 0);
    }

    public PaginatedRecyclerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PaginatedRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setLoadingOffset(PAGE_SIZE / 2);
    }

    @Override
    public void setLayoutManager(LayoutManager layout) {
        super.setLayoutManager(layout);

        mFooter = Views.inflate(this, R.layout.item_footer);

        mFooterProgress = Views.find(mFooter, R.id.footer_progress);
        mLoadingText = Views.find(mFooter, R.id.loading_text);
        mFooterError = Views.find(mFooter, R.id.footer_error);

        mFooterError.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(@NonNull View view) {
                // Reloading page with small delay, for smoother pressed state animation
                removeCallbacks(mErrorClickAction);
                postDelayed(mErrorClickAction, ERROR_CLICK_DELAY);
            }
        });

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
        mLoadingText.setText(text);
    }

    public void setErrorText(String text) {
        mFooterError.setText(text);
    }

    @Override
    protected void onStateChanged(boolean isLoading, boolean isError) {
        mFooterProgress.setVisibility(isLoading ? View.VISIBLE : View.INVISIBLE);
        mFooterError.setVisibility(isError ? View.VISIBLE : View.INVISIBLE);

        if (isLoading || isError) {
            if (!mIsFooterAdded) {
                mWrappedAdapter.addFooter(mFooter);
                mIsFooterAdded = true;
            }
        } else {
            if (mIsFooterAdded) {
                mWrappedAdapter.removeFooter(mFooter);
                mIsFooterAdded = false;
            }
        }
    }

}
