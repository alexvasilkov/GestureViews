package com.alexvasilkov.gestures.sample.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.alexvasilkov.android.commons.utils.Views;
import com.alexvasilkov.gestures.sample.R;
import com.alexvasilkov.gestures.sample.utils.glide.GlideHelper;
import com.alexvasilkov.gestures.sample.utils.recycler.AdapterHelper;
import com.googlecode.flickrjandroid.photos.Photo;

import java.util.List;

public class FlickrPhotoListAdapter extends RecyclerView.Adapter<FlickrPhotoListAdapter.ViewHolder>
        implements View.OnClickListener {

    private List<Photo> mPhotos;
    private boolean mHasMore = true;

    private final OnPhotoListener mListener;

    public FlickrPhotoListAdapter(OnPhotoListener listener) {
        super();
        mListener = listener;
    }

    public void setPhotos(List<Photo> photos, boolean hasMore) {
        List<Photo> old = mPhotos;
        mPhotos = photos;
        mHasMore = hasMore;

        AdapterHelper.notifyChanges(this, old, photos, false);
    }

    @Override
    public int getItemCount() {
        return mPhotos == null ? 0 : mPhotos.size();
    }

    public boolean canLoadNext() {
        return mHasMore;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ViewHolder holder = new ViewHolder(parent);
        holder.image.setOnClickListener(this);
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Photo photo = mPhotos.get(position);
        holder.image.setTag(R.id.tag_item, photo);
        GlideHelper.loadFlickrThumb(photo, holder.image);
    }

    @Override
    public void onClick(@NonNull View view) {
        Photo photo = (Photo) view.getTag(R.id.tag_item);
        int pos = mPhotos.indexOf(photo);
        mListener.onPhotoClick(photo, pos, (ImageView) view);
    }


    static class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView image;

        public ViewHolder(ViewGroup parent) {
            super(Views.inflate(parent, R.layout.item_flickr_image));
            image = (ImageView) itemView;
        }
    }

    public interface OnPhotoListener {
        void onPhotoClick(Photo photo, int position, ImageView image);
    }

}
