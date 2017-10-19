package com.alexvasilkov.gestures.sample.ui.ex.pager;

import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;
import android.view.ViewGroup;

import com.alexvasilkov.gestures.commons.RecyclePagerAdapter;
import com.alexvasilkov.gestures.sample.ui.base.settings.SettingsSetupListener;
import com.alexvasilkov.gestures.sample.ui.ex.GlideHelper;
import com.alexvasilkov.gestures.sample.ui.ex.Painting;
import com.alexvasilkov.gestures.views.GestureImageView;

class ViewPagerAdapter extends RecyclePagerAdapter<ViewPagerAdapter.ViewHolder> {

    private final ViewPager viewPager;
    private final Painting[] paintings;
    private final SettingsSetupListener setupListener;

    ViewPagerAdapter(ViewPager pager, SettingsSetupListener listener) {
        this.viewPager = pager;
        this.paintings = Painting.list(pager.getResources());
        this.setupListener = listener;
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
        setupListener.onSetupGestureView(holder.image);

        GlideHelper.loadResource(paintings[position].imageId, holder.image);
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
