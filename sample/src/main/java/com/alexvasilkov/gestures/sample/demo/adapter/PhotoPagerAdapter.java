package com.alexvasilkov.gestures.sample.demo.adapter;

import android.view.View;
import android.view.ViewGroup;

import com.alexvasilkov.android.commons.ui.Views;
import com.alexvasilkov.gestures.commons.RecyclePagerAdapter;
import com.alexvasilkov.gestures.sample.R;
import com.alexvasilkov.gestures.sample.base.settings.SettingsController;
import com.alexvasilkov.gestures.sample.demo.utils.DemoGlideHelper;
import com.alexvasilkov.gestures.views.GestureImageView;
import com.googlecode.flickrjandroid.photos.Photo;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.ViewPager;

public class PhotoPagerAdapter extends RecyclePagerAdapter<PhotoPagerAdapter.ViewHolder> {

    private static final long PROGRESS_DELAY = 300L;

    private final ViewPager viewPager;
    private final SettingsController settingsController;
    private List<Photo> photos;
    private ImageClickListener clickListener;

    private boolean activated;

    public PhotoPagerAdapter(ViewPager viewPager, SettingsController listener) {
        this.viewPager = viewPager;
        this.settingsController = listener;
    }

    public void setPhotos(List<Photo> photos) {
        this.photos = photos;
        notifyDataSetChanged();
    }

    public Photo getPhoto(int pos) {
        return photos == null || pos < 0 || pos >= photos.size() ? null : photos.get(pos);
    }

    public void setImageClickListener(ImageClickListener clickListener) {
        this.clickListener = clickListener;
    }

    /**
     * To prevent ViewPager from holding heavy views (with bitmaps)  while it is not showing
     * we may just pretend there are no items in this adapter ("activate" = false).
     * But once we need to run opening animation we should "activate" this adapter again.<br>
     * Adapter is not activated by default.
     */
    public void setActivated(boolean activated) {
        if (this.activated != activated) {
            this.activated = activated;
            notifyDataSetChanged();
        }
    }

    @Override
    public int getCount() {
        return !activated || photos == null ? 0 : photos.size();
    }

    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup container) {
        final ViewHolder holder = new ViewHolder(container);

        holder.image.setOnClickListener(view -> onImageClick());

        settingsController.apply(holder.image);

        holder.image.getController().enableScrollInViewPager(viewPager);
        holder.image.getPositionAnimator().addPositionUpdateListener((position, isLeaving) ->
                holder.progress.setVisibility(position == 1f ? View.VISIBLE : View.INVISIBLE));
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        settingsController.apply(holder.image);

        holder.progress.animate().setStartDelay(PROGRESS_DELAY).alpha(1f);

        Photo photo = photos.get(position);

        // Loading image
        DemoGlideHelper.loadFlickrFull(photo, holder.image, new DemoGlideHelper.LoadingListener() {
            @Override
            public void onSuccess() {
                holder.progress.animate().cancel();
                holder.progress.animate().alpha(0f);
            }

            @Override
            public void onError() {
                holder.progress.animate().alpha(0f);
            }
        });
    }

    @Override
    public void onRecycleViewHolder(@NonNull ViewHolder holder) {
        super.onRecycleViewHolder(holder);

        DemoGlideHelper.clear(holder.image);

        holder.progress.animate().cancel();
        holder.progress.setAlpha(0f);

        holder.image.setImageDrawable(null);
    }

    private void onImageClick() {
        if (clickListener != null) {
            clickListener.onFullImageClick();
        }
    }

    public static GestureImageView getImage(RecyclePagerAdapter.ViewHolder holder) {
        return ((ViewHolder) holder).image;
    }

    static class ViewHolder extends RecyclePagerAdapter.ViewHolder {
        final GestureImageView image;
        final View progress;

        ViewHolder(ViewGroup parent) {
            super(Views.inflate(parent, R.layout.demo_item_photo_full));
            image = itemView.findViewById(R.id.photo_full_image);
            progress = itemView.findViewById(R.id.photo_full_progress);
        }
    }

    public interface ImageClickListener {
        void onFullImageClick();
    }

}
