package com.alexvasilkov.gestures.sample.demo.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.alexvasilkov.android.commons.ui.Views;
import com.alexvasilkov.gestures.sample.R;
import com.alexvasilkov.gestures.sample.demo.utils.DemoGlideHelper;
import com.alexvasilkov.gestures.sample.demo.utils.RecyclerAdapterHelper;
import com.googlecode.flickrjandroid.photos.Photo;

import java.util.List;

public class PhotoListAdapter extends DefaultEndlessRecyclerAdapter<PhotoListAdapter.ViewHolder> {

    private final long createdAt = System.currentTimeMillis();
    private static final long noAnimationInterval = 300L;

    private List<Photo> photos;
    private boolean hasMore = true;

    private final OnPhotoListener listener;

    private RecyclerView recyclerView;

    public PhotoListAdapter(OnPhotoListener listener) {
        super();
        this.listener = listener;
    }

    public void setPhotos(List<Photo> photos, boolean hasMore) {
        List<Photo> old = this.photos;
        this.photos = photos;
        this.hasMore = hasMore;

        if (old == null && System.currentTimeMillis() - createdAt < noAnimationInterval) {
            notifyDataSetChanged();
        } else {
            RecyclerAdapterHelper.notifyChanges(this, old, photos);
        }
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
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
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


    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        this.recyclerView = recyclerView;
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        this.recyclerView = null;
    }

    public ImageView getImage(int pos) {
        final RecyclerView.ViewHolder holder =
                recyclerView == null ? null : recyclerView.findViewHolderForLayoutPosition(pos);
        return holder == null ? null : ((ViewHolder) holder).image;
    }


    static class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView image;

        ViewHolder(ViewGroup parent) {
            super(Views.inflate(parent, R.layout.demo_item_photo));
            image = itemView.findViewById(R.id.demo_item_image);
        }
    }

    public interface OnPhotoListener {
        void onPhotoClick(int position);
    }

}
