package com.alexvasilkov.gestures.sample.items;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.alexvasilkov.android.commons.utils.Views;
import com.alexvasilkov.gestures.sample.R;
import com.alexvasilkov.gestures.sample.activities.FlickrImageActivity;
import com.alexvasilkov.gestures.sample.animation.Helper;
import com.alexvasilkov.gestures.sample.utils.GlideDrawableTarget;
import com.alexvasilkov.gestures.sample.views.PhotosRowLayout;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.googlecode.flickrjandroid.photos.Photo;
import com.googlecode.flickrjandroid.photos.PhotoList;

import java.util.ArrayList;
import java.util.List;

public class FlickrListAdapter extends RecyclerView.Adapter<FlickrListAdapter.ViewHolder>
        implements View.OnClickListener {

    private final List<PhotoList> mPages = new ArrayList<>();
    private final List<Photo> mPhotos = new ArrayList<>();
    private List<PhotoRow> mPhotoRows;

    private final int mColumns;

    public FlickrListAdapter(Context context) {
        super();
        mColumns = context.getResources().getInteger(R.integer.images_grid_columns);
    }

    public void setNextPage(PhotoList page) {
        mPages.add(page);
        mPhotos.addAll(page);

        // Combine photos into rows
        final int size = mPhotos.size();
        mPhotoRows = new ArrayList<>(size / mColumns + 1);

        for (int i = 0; i < size; i += mColumns) {
            PhotoRow row = new PhotoRow(mColumns);
            int rowSize = size - i < mColumns ? size - i : mColumns;
            for (int r = 0; r < rowSize; r++) {
                row.add(mPhotos.get(i + r));
            }
            mPhotoRows.add(row);
        }

        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mPhotoRows == null ? 0 : mPhotoRows.size();
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
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ViewHolder holder = new ViewHolder(parent);
        holder.layout.init(mColumns);

        int size = holder.layout.getChildCount();
        for (int i = 0; i < size; i++) {
            holder.layout.getChildAt(i).setOnClickListener(this);
        }

        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        PhotoRow row = mPhotoRows.get(position);
        int size = row.size();
        for (int i = 0; i < mColumns; i++) {
            ImageView child = (ImageView) holder.layout.getChildAt(i);
            child.setVisibility(i < size ? View.VISIBLE : View.INVISIBLE);
            bindImageView(child, i < size ? row.get(i) : null);
        }
    }

    private void bindImageView(ImageView image, Photo photo) {
        image.setTag(R.id.tag_item, photo);

        Glide.with(image.getContext())
                .load(photo == null ? null : photo.getMediumUrl())
                .dontAnimate()
                .thumbnail(Glide.with(image.getContext())
                        .load(photo == null ? null : photo.getThumbnailUrl())
                        .diskCacheStrategy(DiskCacheStrategy.SOURCE))
                .into(new GlideDrawableTarget(image));
    }

    @Override
    public void onClick(@NonNull View view) {
        Photo photo = (Photo) view.getTag(R.id.tag_item);
        Activity activity = (Activity) view.getContext();
        Intent intent = new Intent(activity, FlickrImageActivity.class);

        new Helper.Starter().from((ImageView) view, photo).start(activity, intent);
    }


    static class PhotoRow {

        final List<Photo> photos;

        PhotoRow(int size) {
            this.photos = new ArrayList<>(size);
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

    static class ViewHolder extends RecyclerView.ViewHolder {
        final PhotosRowLayout layout;

        public ViewHolder(ViewGroup parent) {
            super(Views.inflate(parent, R.layout.item_flickr_images_row));
            layout = (PhotosRowLayout) itemView;
        }
    }

}
