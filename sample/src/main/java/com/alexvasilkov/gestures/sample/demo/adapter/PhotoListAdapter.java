package com.alexvasilkov.gestures.sample.demo.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.alexvasilkov.android.commons.ui.Views;
import com.alexvasilkov.gestures.sample.R;
import com.alexvasilkov.gestures.sample.demo.utils.DemoGlideHelper;
import com.alexvasilkov.gestures.sample.demo.utils.RecyclerAdapterHelper;
import com.googlecode.flickrjandroid.photos.Photo;

import java.util.List;

public class PhotoListAdapter extends DefaultEndlessRecyclerAdapter<PhotoListAdapter.ViewHolder> {

    private List<Photo> photos;
    private boolean hasMore = true;

    private final OnPhotoListener listener;

    public PhotoListAdapter(OnPhotoListener listener) {
        super();
        this.listener = listener;
    }

    public void setPhotos(List<Photo> photos, boolean hasMore) {
        List<Photo> old = this.photos;
        this.photos = photos;
        this.hasMore = hasMore;

        RecyclerAdapterHelper.notifyChanges(this, old, photos);
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
        holder.image.setOnClickListener(this::onImageClick);
        return holder;
    }

    @Override
    protected void onBindHolder(ViewHolder holder, int position) {
        Photo photo = photos.get(position);
        holder.image.setTag(R.id.tag_item, photo);
        DemoGlideHelper.loadFlickrThumb(photo, holder.image);
    }

    @Override
    protected void onBindLoadingView(TextView loadingText) {
        loadingText.setText(R.string.demo_loading_photos);
    }

    @Override
    protected void onBindErrorView(TextView errorText) {
        errorText.setText(R.string.demo_reload_photos);
    }

    @Override
    public void onViewRecycled(RecyclerView.ViewHolder holder) {
        super.onViewRecycled(holder);
        if (holder instanceof ViewHolder) {
            DemoGlideHelper.clear(((ViewHolder) holder).image);
        }
    }

    private void onImageClick(@NonNull View image) {
        Photo photo = (Photo) image.getTag(R.id.tag_item);
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
            super(Views.inflate(parent, R.layout.demo_item_photo));
            image = (ImageView) itemView;
        }
    }

    public interface OnPhotoListener {
        void onPhotoClick(int position);
    }

}
