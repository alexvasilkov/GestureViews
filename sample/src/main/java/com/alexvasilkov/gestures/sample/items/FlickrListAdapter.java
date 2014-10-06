package com.alexvasilkov.gestures.sample.items;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.alexvasilkov.android.commons.adapters.ItemsAdapter;
import com.alexvasilkov.gestures.sample.R;
import com.alexvasilkov.gestures.sample.activities.FlickrImageActivity;
import com.alexvasilkov.gestures.sample.widgets.PhotosRowLayout;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.googlecode.flickrjandroid.photos.Photo;
import com.googlecode.flickrjandroid.photos.PhotoList;

import java.util.ArrayList;
import java.util.List;

public class FlickrListAdapter extends ItemsAdapter<FlickrListAdapter.PhotoRow>
        implements View.OnClickListener {

    private final List<PhotoList> mPages = new ArrayList<PhotoList>();
    private final List<Photo> mPhotos = new ArrayList<Photo>();
    private final int mColumns;

    public FlickrListAdapter(Context context) {
        super(context);
        mColumns = context.getResources().getInteger(R.integer.images_grid_columns);
    }

    public void setNextPage(PhotoList page) {
        mPages.add(page);
        mPhotos.addAll(page);

        // Combine photos into rows
        final int size = mPhotos.size();
        List<PhotoRow> rows = new ArrayList<PhotoRow>(size / mColumns + 1);

        for (int i = 0; i < size; i += mColumns) {
            PhotoRow row = new PhotoRow(mColumns);
            int rowSize = size - i < mColumns ? size - i : mColumns;
            for (int r = 0; r < rowSize; r++) {
                row.add(mPhotos.get(i + r));
            }
            rows.add(row);
        }

        setItemsList(rows);
    }

    public boolean canLoadNext() {
        if (mPages.isEmpty()) return true;

        PhotoList page = mPages.get(mPages.size() - 1);

        return page.getPage() * page.getPerPage() < page.getTotal();
    }

    public int getNextPageIndex() {
        return mPages.size() + 1;
    }

    @Override
    protected View createView(PhotoRow row, int pos, ViewGroup parent, LayoutInflater inflater) {
        PhotosRowLayout layout = (PhotosRowLayout) inflater
                .inflate(R.layout.item_flickr_images_row, parent, false);

        layout.init(mColumns);

        int size = layout.getChildCount();
        for (int i = 0; i < size; i++) {
            layout.getChildAt(i).setOnClickListener(this);
        }

        return layout;
    }

    @Override
    protected void bindView(PhotoRow row, int pos, View view) {
        PhotosRowLayout rowView = (PhotosRowLayout) view;
        int size = row.size();
        for (int i = 0; i < mColumns; i++) {
            ImageView child = (ImageView) rowView.getChildAt(i);
            child.setVisibility(i < size ? View.VISIBLE : View.INVISIBLE);
            bindImageView(child, i < size ? row.get(i) : null);
        }
    }

    private void bindImageView(ImageView image, Photo photo) {
        image.setTag(R.id.tag_item, photo);

        Glide.with(image.getContext())
                .load(photo == null ? null : photo.getMediumUrl())
                .placeholder(R.color.image_background)
                .dontAnimate()
                .thumbnail(Glide.with(image.getContext())
                        .load(photo == null ? null : photo.getThumbnailUrl())
                        .dontTransform()
                        .diskCacheStrategy(DiskCacheStrategy.SOURCE))
                .into(image);
    }

    @Override
    public void onClick(View view) {
        Photo photo = (Photo) view.getTag(R.id.tag_item);
        FlickrImageActivity.start(view.getContext(), photo);
    }


    static class PhotoRow {

        final List<Photo> photos;

        PhotoRow(int size) {
            this.photos = new ArrayList<Photo>(size);
        }

        void add(Photo photo) {
            photos.add(photo);
        }

        Photo get(int index) {
            return photos.get(index);
        }

        int size() {
            return photos.size();
        }

    }

}
