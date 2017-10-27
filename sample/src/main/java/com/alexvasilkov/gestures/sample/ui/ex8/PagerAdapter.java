package com.alexvasilkov.gestures.sample.ui.ex8;

import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;
import android.view.ViewGroup;

import com.alexvasilkov.gestures.commons.RecyclePagerAdapter;
import com.alexvasilkov.gestures.sample.ui.base.settings.SettingsSetupListener;
import com.alexvasilkov.gestures.sample.ui.ex.GlideHelper;
import com.alexvasilkov.gestures.sample.ui.ex.Painting;
import com.alexvasilkov.gestures.views.GestureImageView;

class PagerAdapter extends RecyclePagerAdapter<PagerAdapter.ViewHolder> {

    private final ViewPager viewPager;
    private final SettingsSetupListener setupListener;

    private Painting[] paintings;
    private boolean activated;

    PagerAdapter(ViewPager pager, SettingsSetupListener listener) {
        this.viewPager = pager;
        this.setupListener = listener;
    }

    void setPaintings(Painting[] paintings) {
        this.paintings = paintings;
        notifyDataSetChanged();
    }

    /**
     * To prevent ViewPager from holding heavy views (with bitmaps)  while it is not showing
     * we may just pretend there are no items in this adapter ("activate" = false).
     * But once we need to run opening animation we should "activate" this adapter again.<br/>
     * Adapter is not activated by default.
     */
    void setActivated(boolean activated) {
        if (this.activated != activated) {
            this.activated = activated;
            notifyDataSetChanged();
        }
    }

    @Override
    public int getCount() {
        return !activated || paintings == null ? 0 : paintings.length;
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
        Painting painting = paintings[position];
        GlideHelper.loadFull(holder.image, painting.imageId, painting.thumbId);
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
