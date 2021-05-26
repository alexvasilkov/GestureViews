package com.alexvasilkov.gestures.sample.demo.adapter;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.alexvasilkov.android.commons.ui.Views;
import com.alexvasilkov.gestures.GestureController;
import com.alexvasilkov.gestures.Settings;
import com.alexvasilkov.gestures.State;
import com.alexvasilkov.gestures.sample.R;
import com.alexvasilkov.gestures.sample.base.settings.SettingsController;
import com.alexvasilkov.gestures.sample.demo.utils.DemoGlideHelper;
import com.alexvasilkov.gestures.views.GestureImageView;
import com.googlecode.flickrjandroid.photos.Photo;

import java.util.List;

public class PhotoPagerAdapter extends RecyclerView.Adapter<PhotoPagerAdapter.ViewHolder> {

    private static final long PROGRESS_DELAY = 200L;

    private final SettingsController settingsController;
    private List<Photo> photos;
    private ImageClickListener clickListener;

    private boolean activated;

    private RecyclerView recyclerView;

    public PhotoPagerAdapter(SettingsController listener) {
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
    public int getItemCount() {
        return !activated || photos == null ? 0 : photos.size();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup container, int viewType) {
        final ViewHolder holder = new ViewHolder(container);

        holder.image.setOnClickListener(view -> onImageClick());

        settingsController.apply(holder.image);

        holder.image.getPositionAnimator().addPositionUpdateListener((position, isLeaving) ->
                holder.progress.setVisibility(position == 1f ? View.VISIBLE : View.INVISIBLE));

        final GestureController controller = holder.image.getController();
        controller.addOnStateChangeListener(new DynamicZoom(controller.getSettings()));
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        settingsController.apply(holder.image);

        holder.progress.animate().setDuration(150L).setStartDelay(PROGRESS_DELAY).alpha(1f);

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
    public void onViewRecycled(@NonNull ViewHolder holder) {
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


    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        this.recyclerView = recyclerView;
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        this.recyclerView = null;
    }

    public GestureImageView getImage(int pos) {
        final RecyclerView.ViewHolder holder =
                recyclerView == null ? null : recyclerView.findViewHolderForLayoutPosition(pos);
        return holder == null ? null : ((ViewHolder) holder).image;
    }


    static class ViewHolder extends RecyclerView.ViewHolder {
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

    // Dynamically set double tap zoom level to fill the viewport
    private static class DynamicZoom implements GestureController.OnStateChangeListener {
        private final Settings settings;

        DynamicZoom(Settings settings) {
            this.settings = settings;
        }

        @Override
        public void onStateChanged(State state) {
            updateZoomLevels();
        }

        @Override
        public void onStateReset(State oldState, State newState) {
            updateZoomLevels();
        }

        private void updateZoomLevels() {
            final float scaleX = ((float) settings.getViewportW()) / settings.getImageW();
            final float scaleY = ((float) settings.getViewportH()) / settings.getImageH();
            settings.setDoubleTapZoom(Math.max(scaleX, scaleY));
        }
    }

}
