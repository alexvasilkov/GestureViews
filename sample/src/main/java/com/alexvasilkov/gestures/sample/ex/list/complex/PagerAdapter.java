package com.alexvasilkov.gestures.sample.ex.list.complex;

import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;
import android.view.ViewGroup;

import com.alexvasilkov.gestures.commons.RecyclePagerAdapter;
import com.alexvasilkov.gestures.sample.base.settings.SettingsSetupListener;
import com.alexvasilkov.gestures.sample.ex.GlideHelper;
import com.alexvasilkov.gestures.sample.ex.Painting;
import com.alexvasilkov.gestures.views.GestureImageView;

import java.util.List;

class PagerAdapter extends RecyclePagerAdapter<PagerAdapter.ViewHolder> {

    private final ViewPager viewPager;
    private final SettingsSetupListener setupListener;

    private List<Painting> paintings;

    PagerAdapter(ViewPager pager, SettingsSetupListener listener) {
        this.viewPager = pager;
        this.setupListener = listener;
    }

    void setPaintings(List<Painting> paintings) {
        this.paintings = paintings;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return paintings == null ? 0 : paintings.size();
    }

    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup container) {
        ViewHolder holder = new ViewHolder(container);
        holder.image.getController().enableScrollInViewPager(viewPager);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        setupListener.onSetupGestureView(holder.image);
        Painting painting = paintings.get(position);
        GlideHelper.loadFull(holder.image, painting.imageId, painting.thumbId);
    }

    static GestureImageView getImageView(RecyclePagerAdapter.ViewHolder holder) {
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
