package com.alexvasilkov.gestures.sample.adapters;

import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.alexvasilkov.android.commons.utils.Views;
import com.alexvasilkov.gestures.animation.ViewPositionAnimator;
import com.alexvasilkov.gestures.experimental.AnimatorPagerAdapter;
import com.alexvasilkov.gestures.experimental.RecyclePagerAdapter;
import com.alexvasilkov.gestures.sample.R;
import com.alexvasilkov.gestures.sample.utils.glide.GlideHelper;
import com.alexvasilkov.gestures.views.GestureImageView;
import com.alexvasilkov.gestures.views.interfaces.AnimatorView;
import com.bumptech.glide.Glide;
import com.googlecode.flickrjandroid.photos.Photo;

import java.util.ArrayList;
import java.util.List;

public class FlickrPhotoPagerAdapter extends AnimatorPagerAdapter<FlickrPhotoPagerAdapter.ViewHolder> {

    private final ViewPager mViewPager;
    private List<Photo> mPhotos = new ArrayList<>();

    public FlickrPhotoPagerAdapter(ViewPager viewPager, RecyclerView recyclerView) {
        super(viewPager, recyclerView);
        mViewPager = viewPager;
    }

    public void setPhotos(List<Photo> photos) {
        mPhotos = photos;
        notifyDataSetChanged();
    }

    @Override
    protected int getItemsCount() {
        return mPhotos == null ? 0 : mPhotos.size();
    }

    @Override
    protected AnimatorView getAnimatorView(ViewHolder holder) {
        return holder.image;
    }

    @Override
    protected void onViewsReady(View from, AnimatorView to, int index) {
        super.onViewsReady(from, to, index);

        // Setting image drawable from 'from' view to 'to' to prevent flickering
        ImageView fromImage = (ImageView) from;
        GestureImageView toImage = (GestureImageView) to;
        if (toImage.getDrawable() == null) toImage.setImageDrawable(fromImage.getDrawable());
    }

    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup container) {
        final ViewHolder holder = new ViewHolder(container);
        holder.image.getController().getSettings().setFillViewport(true).setMaxZoom(3f);
        holder.image.getController().enableScrollInViewPager(mViewPager);
        holder.image.getPositionAnimator().addPositionUpdateListener(
                new ViewPositionAnimator.PositionUpdateListener() {
                    @Override
                    public void onPositionUpdate(float state, boolean isLeaving) {
                        holder.progress.setVisibility(state == 1f ? View.VISIBLE : View.INVISIBLE);
                    }
                });
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        // Temporary disabling touch controls
        if (!holder.gesturesDisabled) {
            holder.image.getController().getSettings().disableGestures();
            holder.gesturesDisabled = true;
        }

        holder.progress.animate().alpha(1f);

        Photo photo = mPhotos.get(position);

        // Loading image
        GlideHelper.loadFlickrFull(photo, holder.image,
                new GlideHelper.ImageLoadingListener() {
                    @Override
                    public void onLoaded() {
                        holder.progress.animate().alpha(0f);
                        // Re-enabling touch controls
                        if (holder.gesturesDisabled) {
                            holder.image.getController().getSettings().enableGestures();
                            holder.gesturesDisabled = false;
                        }
                    }

                    @Override
                    public void onFailed() {
                        holder.progress.animate().alpha(0f);
                    }
                });
    }

    @Override
    public void onRecycleViewHolder(@NonNull ViewHolder holder) {
        super.onRecycleViewHolder(holder);

        if (holder.gesturesDisabled) {
            holder.image.getController().getSettings().enableGestures();
            holder.gesturesDisabled = false;
        }

        Glide.clear(holder.image);

        holder.progress.animate().cancel();
        holder.progress.setAlpha(0f);

        holder.image.setImageDrawable(null);
    }

    static class ViewHolder extends RecyclePagerAdapter.ViewHolder {
        final GestureImageView image;
        final View progress;

        boolean gesturesDisabled;

        public ViewHolder(ViewGroup parent) {
            super(Views.inflate(parent, R.layout.item_flickr_full_image));
            image = Views.find(itemView, R.id.flickr_full_image);
            progress = Views.find(itemView, R.id.flickr_full_progress);
        }
    }

}
