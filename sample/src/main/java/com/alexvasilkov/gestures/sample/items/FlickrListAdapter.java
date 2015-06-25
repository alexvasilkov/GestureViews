package com.alexvasilkov.gestures.sample.items;

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
import com.googlecode.flickrjandroid.photos.PhotoList;

import java.util.ArrayList;
import java.util.List;

public class FlickrListAdapter extends RecyclerView.Adapter<FlickrListAdapter.ViewHolder>
        implements View.OnClickListener {

    private final List<PhotoList> mPages = new ArrayList<>();
    private final List<Photo> mPhotos = new ArrayList<>();

    private final OnPhotoListener mListener;

    private Photo mPhotoToListen;

    public FlickrListAdapter(OnPhotoListener listener) {
        super();
        mListener = listener;
    }

    public void listenForViewUpdates(Photo photo) {
        mPhotoToListen = photo;
    }

    public void setNextPage(PhotoList page) {
        List<Photo> old = new ArrayList<>(mPhotos); // Saving copy of old list

        mPages.add(page);
        mPhotos.addAll(page);

        AdapterHelper.notifyChanges(this, old, mPhotos, false);
    }

    @Override
    public int getItemCount() {
        return mPhotos.size();
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
        holder.image.setOnClickListener(this);
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Photo photo = mPhotos.get(position);
        holder.image.setTag(R.id.tag_item, photo);
        GlideHelper.loadFlickrThumb(photo, holder.image);

        if (mPhotoToListen != null && mPhotoToListen.equals(photo))
            mListener.onPhotoViewChanged(photo, holder.image);
    }

    @Override
    public void onClick(@NonNull View view) {
        Photo photo = (Photo) view.getTag(R.id.tag_item);
        mListener.onPhotoClick(photo, (ImageView) view);
    }


    static class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView image;

        public ViewHolder(ViewGroup parent) {
            super(Views.inflate(parent, R.layout.item_flickr_image));
            image = (ImageView) itemView;
        }
    }

    public interface OnPhotoListener {
        void onPhotoClick(Photo photo, ImageView image);

        void onPhotoViewChanged(Photo photo, ImageView image);
    }

}
