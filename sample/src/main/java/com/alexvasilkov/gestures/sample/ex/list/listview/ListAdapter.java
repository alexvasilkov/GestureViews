package com.alexvasilkov.gestures.sample.ex.list.listview;

import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.alexvasilkov.android.commons.texts.SpannableBuilder;
import com.alexvasilkov.android.commons.ui.Views;
import com.alexvasilkov.gestures.sample.R;
import com.alexvasilkov.gestures.sample.ex.GlideHelper;
import com.alexvasilkov.gestures.sample.ex.Painting;

class ListAdapter extends BaseAdapter implements View.OnClickListener {

    private final Painting[] paintings;
    private final OnPaintingClickListener listener;

    ListAdapter(Painting[] paintings, OnPaintingClickListener listener) {
        this.paintings = paintings;
        this.listener = listener;
    }

    @Override
    public int getCount() {
        return paintings.length;
    }

    @Override
    public Object getItem(int position) {
        return paintings[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            holder = onCreateHolder(parent);
            holder.itemView.setTag(R.id.tag_holder, holder);
        } else {
            holder = (ViewHolder) convertView.getTag(R.id.tag_holder);
        }
        onBindHolder(holder, position);
        return holder.itemView;
    }

    private ViewHolder onCreateHolder(ViewGroup parent) {
        ViewHolder holder = new ViewHolder(parent);
        holder.itemView.setOnClickListener(this);
        return holder;
    }

    private void onBindHolder(ViewHolder holder, int position) {
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

    static ImageView getImageView(View itemView) {
        ViewHolder holder = (ViewHolder) itemView.getTag(R.id.tag_holder);
        return holder == null ? null : holder.image;
    }


    static class ViewHolder {
        final View itemView;
        final ImageView image;
        final TextView title;

        ViewHolder(ViewGroup parent) {
            itemView = Views.inflate(parent, R.layout.list_image_item);
            image = itemView.findViewById(R.id.list_image);
            title = itemView.findViewById(R.id.list_image_title);
        }
    }

    interface OnPaintingClickListener {
        void onPaintingClick(int position);
    }

}
