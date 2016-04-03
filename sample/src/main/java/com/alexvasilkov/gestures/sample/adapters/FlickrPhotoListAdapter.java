package com.alexvasilkov.gestures.sample.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.alexvasilkov.android.commons.utils.Views;
import com.alexvasilkov.gestures.sample.R;
import com.alexvasilkov.gestures.sample.utils.RecyclerAdapterHelper;
import com.alexvasilkov.gestures.sample.utils.glide.GlideHelper;
import com.bumptech.glide.Glide;
import com.googlecode.flickrjandroid.photos.Photo;

import java.util.List;

public class FlickrPhotoListAdapter
        extends DefaultEndlessRecyclerAdapter<FlickrPhotoListAdapter.ViewHolder>
        implements View.OnClickListener {

    private List<Photo> photos;
    private boolean hasMore = true;

    private final OnPhotoListener listener;

    public FlickrPhotoListAdapter(OnPhotoListener listener) {
        super();
        this.listener = listener;
    }

    public void setPhotos(List<Photo> photos, boolean hasMore) {
        List<Photo> old = this.photos;
        this.photos = photos;
        this.hasMore = hasMore;

        RecyclerAdapterHelper.notifyChanges(this, old, photos, false);
    }

    @Override
    public int getCount() {
        return photos == null ? 0 : photos.size();
    }

    public boolean canLoadNext() {
        return hasMore;
    }

    @Override
    protected ViewHolder onCreateHolder(ViewGroup parent, int viewType) {
        ViewHolder holder = new ViewHolder(parent);
        holder.image.setOnClickListener(this);
        return holder;
    }

    @Override
    protected void onBindHolder(ViewHolder holder, int position) {
        Photo photo = photos.get(position);
        holder.image.setTag(R.id.tag_item, photo);
        GlideHelper.loadFlickrThumb(photo, holder.image);
    }

    @Override
    protected void onBindLoadingView(TextView loadingText) {
        loadingText.setText(R.string.loading_images);
    }

    @Override
    protected void onBindErrorView(TextView errorText) {
        errorText.setText(R.string.reload_images);
    }

    @Override
    public void onViewRecycled(RecyclerView.ViewHolder holder) {
        super.onViewRecycled(holder);
        if (holder instanceof ViewHolder) {
            Glide.clear(((ViewHolder) holder).image);
        }
    }

    @Override
    public void onClick(@NonNull View view) {
        Photo photo = (Photo) view.getTag(R.id.tag_item);
        int pos = photos.indexOf(photo);
        listener.onPhotoClick(pos);
    }

    public static ImageView getImage(RecyclerView.ViewHolder holder) {
        if (holder instanceof ViewHolder) {
            return ((ViewHolder) holder).image;
        } else {
            return null;
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView image;

        ViewHolder(ViewGroup parent) {
            super(Views.inflate(parent, R.layout.item_flickr_image));
            image = (ImageView) itemView;
        }
    }

    public interface OnPhotoListener {
        void onPhotoClick(int position);
    }

}
