package com.alexvasilkov.gestures.sample.ui.ex5;

import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;
import android.view.ViewGroup;

import com.alexvasilkov.gestures.commons.RecyclePagerAdapter;
import com.alexvasilkov.gestures.sample.logic.Painting;
import com.alexvasilkov.gestures.sample.ui.base.settings.SettingsSetupListener;
import com.alexvasilkov.gestures.sample.utils.glide.GlideHelper;
import com.alexvasilkov.gestures.views.GestureImageView;

class PaintingsPagerAdapter extends RecyclePagerAdapter<PaintingsPagerAdapter.ViewHolder> {

    private final ViewPager viewPager;
    private final Painting[] paintings;
    private final SettingsSetupListener setupListener;

    PaintingsPagerAdapter(ViewPager pager, Painting[] paintings, SettingsSetupListener listener) {
        this.viewPager = pager;
        this.paintings = paintings;
        this.setupListener = listener;
    }

    @Override
    public int getCount() {
        return paintings.length;
    }

    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup container) {
        ViewHolder holder = new ViewHolder(container);
        holder.image.getController().enableScrollInViewPager(viewPager);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (setupListener != null) {
            setupListener.onSetupGestureView(holder.image);
        }
        GlideHelper.loadResource(paintings[position].imageId, holder.image);
    }

    static GestureImageView getImage(RecyclePagerAdapter.ViewHolder holder) {
        return ((ViewHolder) holder).image;
    }


    static class ViewHolder extends RecyclePagerAdapter.ViewHolder {
        final GestureImageView image;

        ViewHolder(ViewGroup container) {
            super(new GestureImageView(container.getContext()));
            image = (GestureImageView) itemView;
        }
    }

}
