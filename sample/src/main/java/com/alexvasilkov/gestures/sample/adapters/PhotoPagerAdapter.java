package com.alexvasilkov.gestures.sample.adapters;

import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;

import com.alexvasilkov.android.commons.ui.Views;
import com.alexvasilkov.gestures.animation.ViewPositionAnimator.PositionUpdateListener;
import com.alexvasilkov.gestures.commons.RecyclePagerAdapter;
import com.alexvasilkov.gestures.sample.R;
import com.alexvasilkov.gestures.sample.utils.GestureSettingsSetupListener;
import com.alexvasilkov.gestures.sample.utils.glide.GlideHelper;
import com.alexvasilkov.gestures.views.GestureImageView;
import com.bumptech.glide.Glide;
import com.googlecode.flickrjandroid.photos.Photo;

import java.util.List;

public class PhotoPagerAdapter
        extends RecyclePagerAdapter<PhotoPagerAdapter.ViewHolder> {

    private static final long PROGRESS_DELAY = 300L;

    private final ViewPager viewPager;
    private List<Photo> photos;
    private GestureSettingsSetupListener setupListener;

    private boolean activated;

    public PhotoPagerAdapter(ViewPager viewPager) {
        this.viewPager = viewPager;
    }

    public void setPhotos(List<Photo> photos) {
        this.photos = photos;
        notifyDataSetChanged();
    }

    public Photo getPhoto(int pos) {
        return photos == null || pos < 0 || pos >= photos.size() ? null : photos.get(pos);
    }

    public void setSetupListener(GestureSettingsSetupListener listener) {
        setupListener = listener;
    }

    /**
     * To prevent ViewPager from holding heavy views (with bitmaps)  while it is not showing
     * we may just pretend there are no items in this adapter ("activate" = false).
     * But once we need to run opening animation we should "activate" this adapter again.<br/>
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

        holder.image.getController().getSettings()
            .setMaxZoom(10f)
            .setDoubleTapZoom(3f);

        if (setupListener != null) {
            setupListener.onSetupGestureView(holder.image);
        }

        holder.image.getController().enableScrollInViewPager(viewPager);
        holder.image.getPositionAnimator().addPositionUpdateListener(new PositionUpdateListener() {
            @Override
            public void onPositionUpdate(float position, boolean isLeaving) {
                holder.progress.setVisibility(position == 1f ? View.VISIBLE : View.INVISIBLE);
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        if (setupListener != null) {
            setupListener.onSetupGestureView(holder.image);
        }

        // Temporary disabling touch controls
        if (!holder.gesturesDisabled) {
            holder.image.getController().getSettings().disableGestures();
            holder.gesturesDisabled = true;
        }

        holder.progress.animate().setStartDelay(PROGRESS_DELAY).alpha(1f);

        Photo photo = photos.get(position);

        // Loading image
        GlideHelper.loadFlickrFull(photo, holder.image,
                new GlideHelper.ImageLoadingListener() {
                    @Override
                    public void onLoaded() {
                        holder.progress.animate().cancel();
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

    public static GestureImageView getImage(RecyclePagerAdapter.ViewHolder holder) {
        return ((ViewHolder) holder).image;
    }

    static class ViewHolder extends RecyclePagerAdapter.ViewHolder {
        final GestureImageView image;
        final View progress;

        boolean gesturesDisabled;

        ViewHolder(ViewGroup parent) {
            super(Views.inflate(parent, R.layout.item_photo_full));
            image = Views.find(itemView, R.id.photo_full_image);
            progress = Views.find(itemView, R.id.photo_full_progress);
        }
    }

}
