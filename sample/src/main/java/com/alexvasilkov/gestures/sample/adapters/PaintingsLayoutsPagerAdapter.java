package com.alexvasilkov.gestures.sample.adapters;

import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.alexvasilkov.android.commons.nav.Navigate;
import com.alexvasilkov.android.commons.texts.SpannableBuilder;
import com.alexvasilkov.android.commons.ui.Views;
import com.alexvasilkov.gestures.commons.RecyclePagerAdapter;
import com.alexvasilkov.gestures.sample.R;
import com.alexvasilkov.gestures.sample.logic.Painting;
import com.alexvasilkov.gestures.sample.utils.GestureSettingsSetupListener;
import com.alexvasilkov.gestures.sample.utils.glide.GlideHelper;
import com.alexvasilkov.gestures.views.GestureFrameLayout;

public class PaintingsLayoutsPagerAdapter
        extends RecyclePagerAdapter<PaintingsLayoutsPagerAdapter.ViewHolder>
        implements View.OnClickListener {

    private final ViewPager viewPager;
    private final Painting[] paintings;
    private final GestureSettingsSetupListener setupListener;

    public PaintingsLayoutsPagerAdapter(ViewPager pager, Painting[] paintings,
            GestureSettingsSetupListener listener) {
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
        holder.layout.getController().getSettings().setMaxZoom(1.5f);
        holder.layout.getController().enableScrollInViewPager(viewPager);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (setupListener != null) {
            setupListener.onSetupGestureView(holder.layout);
        }
        holder.layout.getController().resetState();

        GlideHelper.loadResource(paintings[position].getImageId(), holder.image);

        CharSequence titleText = new SpannableBuilder(holder.itemView.getContext())
                .createStyle().setFont(Typeface.DEFAULT_BOLD).apply()
                .append(paintings[position].getAuthor()).append("\n")
                .clearStyle()
                .append(paintings[position].getTitle())
                .build();
        holder.title.setText(titleText);

        holder.button.setTag(paintings[position].getLink());
        holder.button.setOnClickListener(this);
    }

    @Override
    public void onClick(@NonNull View view) {
        Navigate.from(view.getContext()).external().browser().url((String) view.getTag()).start();
    }

    static class ViewHolder extends RecyclePagerAdapter.ViewHolder {

        final GestureFrameLayout layout;
        final ImageView image;
        final TextView title;
        final View button;

        ViewHolder(ViewGroup container) {
            super(Views.inflate(container, R.layout.item_painting_layout));
            layout = Views.find(itemView, R.id.painting_g_layout);
            image = Views.find(layout, R.id.painting_image);
            title = Views.find(layout, R.id.painting_title);
            button = Views.find(layout, R.id.painting_button);
        }

    }

}
