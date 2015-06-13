package com.alexvasilkov.gestures.sample.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import com.alexvasilkov.gestures.sample.R;

public class PhotosRowLayout extends LinearLayout {

    public PhotosRowLayout(Context context) {
        this(context, null, 0);
    }

    public PhotosRowLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PhotosRowLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setOrientation(HORIZONTAL);
    }

    public void init(int columns) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        for (int i = 0; i < columns; i++) {
            inflater.inflate(R.layout.item_flickr_image, this, true);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int w = MeasureSpec.getSize(widthMeasureSpec);
        computeChildrenSizes(w);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    private void computeChildrenSizes(int width) {
        int size = getChildCount();
        int availableWidth = width - getPaddingLeft() - getPaddingRight();

        for (int i = 0; i < size; i++) {
            LayoutParams params = (LayoutParams) getChildAt(i).getLayoutParams();
            availableWidth -= params.leftMargin + params.rightMargin;
        }

        int childSize = size == 0 || availableWidth < 0 ? 0 : availableWidth / size;

        for (int i = 0; i < size; i++) {
            LayoutParams params = (LayoutParams) getChildAt(i).getLayoutParams();
            params.width = params.height = childSize;
        }
    }

}
