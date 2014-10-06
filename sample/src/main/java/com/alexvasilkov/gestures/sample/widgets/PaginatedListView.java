package com.alexvasilkov.gestures.sample.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;
import com.alexvasilkov.android.commons.utils.Views;
import com.alexvasilkov.gestures.sample.R;

public class PaginatedListView extends EndlessListView {

    public static final int PAGE_SIZE = 30;

    private static final long ERROR_CLICK_DELAY = 120;

    private final Runnable mErrorClickAction = new Runnable() {
        @Override
        public void run() {
            reloadNextPage();
        }
    };

    private View mFooterProgress;
    private View mFooterError;
    private TextView mLoadingText;
    private TextView mErrorText;

    public PaginatedListView(Context context) {
        this(context, null, 0);
    }

    public PaginatedListView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PaginatedListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        View footer = Views.inflate(this, R.layout.item_footer);
        addFooterView(footer);

        mFooterProgress = Views.find(footer, R.id.footer_progress);
        mFooterError = Views.find(footer, R.id.footer_error);
        mLoadingText = Views.find(footer, R.id.loading_text);
        mErrorText = Views.find(footer, R.id.error_text);

        mFooterError.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Reloading page with small delay, for smoother pressed state animation
                removeCallbacks(mErrorClickAction);
                postDelayed(mErrorClickAction, ERROR_CLICK_DELAY);
            }
        });

        setLoadingOffset(PAGE_SIZE / 2);
    }

    public void setLoadingText(String text) {
        mLoadingText.setText(text);
    }

    public void setErrorText(String text) {
        mErrorText.setText(text);
    }

    @Override
    protected void onUpdateFooter(boolean isLoading, boolean isError) {
        mFooterProgress.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        mFooterError.setVisibility(isError ? View.VISIBLE : View.GONE);
    }

}
