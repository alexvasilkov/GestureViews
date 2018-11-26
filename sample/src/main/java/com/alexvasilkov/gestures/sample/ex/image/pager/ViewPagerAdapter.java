package com.alexvasilkov.gestures.sample.ex.image.pager;

import android.view.ViewGroup;

import com.alexvasilkov.gestures.commons.RecyclePagerAdapter;
import com.alexvasilkov.gestures.sample.base.settings.SettingsController;
import com.alexvasilkov.gestures.sample.ex.utils.GlideHelper;
import com.alexvasilkov.gestures.sample.ex.utils.Painting;
import com.alexvasilkov.gestures.views.GestureImageView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.ViewPager;

class ViewPagerAdapter extends RecyclePagerAdapter<ViewPagerAdapter.ViewHolder> {

    private final ViewPager viewPager;
    private final Painting[] paintings;
    private final SettingsController settingsController;

    ViewPagerAdapter(ViewPager pager, SettingsController listener) {
        this.viewPager = pager;
        this.paintings = Painting.list(pager.getResources());
        this.settingsController = listener;
    }

    @Override
    public int getCount() {
        return paintings.length;
    }

    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup container) {
        final ViewHolder holder = new ViewHolder(container);

        // Applying custom settings
        holder.image.getController().getSettings()
                .setMaxZoom(6f)
                .setDoubleTapZoom(3f);

        // Enabling smooth scrolling when image panning turns into ViewPager scrolling.
        // Otherwise ViewPager scrolling will only be possible when image is in zoomed out state.
        holder.image.getController().enableScrollInViewPager(viewPager);

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Applying settings from toolbar menu, see BaseExampleActivity
        settingsController.apply(holder.image);

        Painting painting = paintings[position];
        GlideHelper.loadFull(holder.image, painting.imageId, painting.thumbId);
    }

    @Override
    public void onRecycleViewHolder(@NonNull ViewHolder holder) {
        GlideHelper.clear(holder.image);
    }

    static class ViewHolder extends RecyclePagerAdapter.ViewHolder {
        final GestureImageView image;

        ViewHolder(ViewGroup container) {
            super(new GestureImageView(container.getContext()));
            image = (GestureImageView) itemView;
        }
    }

}
