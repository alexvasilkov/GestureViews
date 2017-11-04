package com.alexvasilkov.gestures.sample.ex.list.complex;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.alexvasilkov.android.commons.ui.Views;
import com.alexvasilkov.gestures.sample.R;
import com.alexvasilkov.gestures.sample.ex.utils.GlideHelper;
import com.alexvasilkov.gestures.sample.ex.utils.Painting;

import java.util.List;

class ListAdapter extends RecyclerView.Adapter<ViewHolder> {

    private static final int TYPE_IMAGES = 0;
    private static final int TYPE_TEXT = 1;

    private final List<ListItem> items;
    private final OnImageClickListener listener;

    ListAdapter(List<ListItem> items, OnImageClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position).paintings == null ? TYPE_TEXT : TYPE_IMAGES;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_IMAGES:
                final ImagesViewHolder holder = new ImagesViewHolder(parent);
                // Setting up click listeners and saving images positions into tags
                for (int i = 0, size = holder.images.length; i < size; i++) {
                    holder.images[i].setTag(R.id.tag_item, i);
                    holder.images[i].setOnClickListener(this::onImageClick);
                }
                return holder;
            case TYPE_TEXT:
                return new TextViewHolder(parent);
            default:
                throw new IllegalArgumentException();
        }
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (holder instanceof ImagesViewHolder) {
            onBindImages((ImagesViewHolder) holder, items.get(position), position);
        } else if (holder instanceof TextViewHolder) {
            onBindText((TextViewHolder) holder, items.get(position));
        }
    }

    private void onBindImages(ImagesViewHolder holder, ListItem item, int pos) {
        // Computing number of hidden images, starting with no visible images
        int hidden = item.paintings == null ? 0 : item.paintings.size();

        // Going through all available image views
        for (int i = 0, size = holder.images.length; i < size; i++) {
            // Getting painting for current position (if there is one)
            final Painting painting = item.paintings != null && i < item.paintings.size()
                    ? item.paintings.get(i) : null;

            if (painting == null) {
                // No more paintings, hiding current image
                holder.images[i].setVisibility(View.GONE);
            } else {
                // Showing painting's image for current position
                holder.images[i].setVisibility(View.VISIBLE);
                GlideHelper.loadThumb(holder.images[i], painting.thumbId);
                hidden--;
            }
        }

        // Displaying number of hidden paintings, if any
        final String countText = "+" + hidden;
        holder.count.setText(countText);
        holder.count.setVisibility(hidden > 0 ? View.VISIBLE : View.GONE);

        holder.row.setTag(R.id.tag_item, pos);
    }

    private void onBindText(TextViewHolder holder, ListItem item) {
        holder.text.setText(item.text);
    }

    private void onImageClick(View image) {
        final ViewGroup parent = (ViewGroup) image.getParent();
        final int itemPos = (int) parent.getTag(R.id.tag_item);
        final int imagePos = (int) image.getTag(R.id.tag_item);

        listener.onImageClick(itemPos, imagePos);
    }

    static ImageView getImageView(RecyclerView.ViewHolder holder, int pos) {
        if (holder instanceof ImagesViewHolder) {
            final ImageView[] images = ((ImagesViewHolder) holder).images;
            return pos >= 0 && pos < images.length ? images[pos] : null;
        } else {
            return null;
        }
    }


    private static class ImagesViewHolder extends ViewHolder {
        final ViewGroup row;
        final ImageView[] images = new ImageView[2];
        final TextView count;

        ImagesViewHolder(ViewGroup parent) {
            super(Views.inflate(parent, R.layout.complex_list_item_images));
            row = itemView.findViewById(R.id.complex_item_row);
            images[0] = itemView.findViewById(R.id.complex_item_image_1);
            images[1] = itemView.findViewById(R.id.complex_item_image_2);
            count = itemView.findViewById(R.id.complex_item_count);
        }
    }

    private static class TextViewHolder extends ViewHolder {
        final TextView text;

        TextViewHolder(ViewGroup parent) {
            super(Views.inflate(parent, R.layout.complex_list_item_text));
            text = (TextView) itemView;
        }
    }

    interface OnImageClickListener {
        void onImageClick(int itemPos, int imagePos);
    }

}
