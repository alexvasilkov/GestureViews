package com.alexvasilkov.gestures.sample.ui.ex.list;

import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.alexvasilkov.android.commons.texts.SpannableBuilder;
import com.alexvasilkov.android.commons.ui.Views;
import com.alexvasilkov.gestures.sample.R;
import com.alexvasilkov.gestures.sample.ui.ex.GlideHelper;
import com.alexvasilkov.gestures.sample.ui.ex.Painting;

class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder>
        implements View.OnClickListener {

    private final Painting[] paintings;
    private final OnPaintingClickListener listener;

    RecyclerAdapter(Painting[] paintings, OnPaintingClickListener listener) {
        this.paintings = paintings;
        this.listener = listener;
    }

    @Override
    public int getItemCount() {
        return paintings.length;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ViewHolder holder = new ViewHolder(parent);
        holder.itemView.setOnClickListener(this);
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final Painting painting = paintings[position];

        // Storing item position for click handler
        holder.itemView.setTag(R.id.tag_item, position);

        GlideHelper.loadThumb(holder.image, painting.thumbId);

        CharSequence text = new SpannableBuilder(holder.title.getContext())
                .createStyle().setFont(Typeface.DEFAULT_BOLD).apply()
                .append(painting.author).append("\n")
                .clearStyle()
                .append(painting.title)
                .build();
        holder.title.setText(text);
    }

    @Override
    public void onClick(@NonNull View view) {
        int pos = (Integer) view.getTag(R.id.tag_item);
        listener.onPaintingClick(pos);
    }

    static ImageView getImage(RecyclerView.ViewHolder holder) {
        return ((ViewHolder) holder).image;
    }


    static class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView image;
        final TextView title;

        ViewHolder(ViewGroup parent) {
            super(Views.inflate(parent, R.layout.list_image_item));
            image = itemView.findViewById(R.id.list_image);
            title = itemView.findViewById(R.id.list_image_title);
        }
    }

    interface OnPaintingClickListener {
        void onPaintingClick(int position);
    }

}
