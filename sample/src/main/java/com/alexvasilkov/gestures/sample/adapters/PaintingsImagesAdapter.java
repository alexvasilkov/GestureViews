package com.alexvasilkov.gestures.sample.adapters;

import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;
import android.view.ViewGroup;

import com.alexvasilkov.gestures.sample.logic.Painting;
import com.alexvasilkov.gestures.sample.utils.glide.GlideHelper;
import com.alexvasilkov.gestures.views.GestureImageView;
import com.alexvasilkov.gestures.views.interfaces.GestureView;
import com.alexvasilkov.gestures.views.utils.RecyclePagerAdapter;

public class PaintingsImagesAdapter extends RecyclePagerAdapter<PaintingsImagesAdapter.ViewHolder> {

    private final ViewPager mViewPager;
    private final Painting[] mPaintings;
    private final OnSetupGestureViewListener mSetupListener;

    public PaintingsImagesAdapter(ViewPager pager, Painting[] paintings,
                                  OnSetupGestureViewListener listener) {
        mViewPager = pager;
        mPaintings = paintings;
        mSetupListener = listener;
    }

    @Override
    public int getCount() {
        return mPaintings.length;
    }

    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup container) {
        ViewHolder holder = new ViewHolder(container);
        holder.image.getController().enableScrollInViewPager(mViewPager);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (mSetupListener != null) mSetupListener.onSetupGestureView(holder.image);
        holder.image.getController().resetState();
        GlideHelper.loadResource(mPaintings[position].getImageId(), holder.image);
    }


    static class ViewHolder extends RecyclePagerAdapter.ViewHolder {
        public final GestureImageView image;

        public ViewHolder(ViewGroup container) {
            super(new GestureImageView(container.getContext()));
            image = (GestureImageView) itemView;
        }
    }

    public interface OnSetupGestureViewListener {
        void onSetupGestureView(GestureView view);
    }

}
