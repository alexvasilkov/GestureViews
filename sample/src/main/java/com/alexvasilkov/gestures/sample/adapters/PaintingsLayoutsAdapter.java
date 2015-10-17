package com.alexvasilkov.gestures.sample.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.alexvasilkov.android.commons.texts.SpannableBuilder;
import com.alexvasilkov.android.commons.utils.Intents;
import com.alexvasilkov.android.commons.utils.Views;
import com.alexvasilkov.gestures.sample.R;
import com.alexvasilkov.gestures.sample.logic.Painting;
import com.alexvasilkov.gestures.views.GestureFrameLayout;
import com.alexvasilkov.gestures.views.interfaces.GestureView;
import com.alexvasilkov.gestures.views.utils.RecyclePagerAdapter;
import com.bumptech.glide.Glide;

public class PaintingsLayoutsAdapter extends RecyclePagerAdapter<PaintingsLayoutsAdapter.ViewHolder>
        implements View.OnClickListener {

    private final ViewPager mViewPager;
    private final Painting[] mPaintings;
    private final OnSetupGestureViewListener mSetupListener;

    public PaintingsLayoutsAdapter(ViewPager pager, Painting[] paintings,
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
        holder.layout.getController().getSettings().setMaxZoom(1.5f);
        holder.layout.getController().enableScrollInViewPager(mViewPager);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (mSetupListener != null) mSetupListener.onSetupGestureView(holder.layout);
        holder.layout.getController().resetState();

        Context context = holder.itemView.getContext();

        Glide.with(context).load(mPaintings[position].getImageId()).into(holder.image);

        CharSequence titleText = new SpannableBuilder(context)
                .createStyle().setFont(Typeface.DEFAULT_BOLD).apply()
                .append(R.string.paintings_author).append("\n")
                .clearStyle()
                .append(mPaintings[position].getTitle())
                .build();
        holder.title.setText(titleText);

        holder.button.setTag(mPaintings[position].getLink());
        holder.button.setOnClickListener(this);
    }

    @Override
    public void onClick(@NonNull View view) {
        Intents.get(view.getContext()).openWebBrowser((String) view.getTag());
    }

    static class ViewHolder extends RecyclePagerAdapter.ViewHolder {

        public final GestureFrameLayout layout;
        public final ImageView image;
        public final TextView title;
        public final View button;

        public ViewHolder(ViewGroup container) {
            super(Views.inflate(container, R.layout.item_layout));
            layout = Views.find(itemView, R.id.painting_g_layout);
            image = Views.find(layout, R.id.painting_image);
            title = Views.find(layout, R.id.painting_title);
            button = Views.find(layout, R.id.painting_button);
        }

    }

    public interface OnSetupGestureViewListener {
        void onSetupGestureView(GestureView view);
    }

}
