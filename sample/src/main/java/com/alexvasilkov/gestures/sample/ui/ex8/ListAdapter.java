package com.alexvasilkov.gestures.sample.ui.ex8;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.alexvasilkov.android.commons.ui.Views;
import com.alexvasilkov.gestures.sample.R;
import com.alexvasilkov.gestures.sample.ui.ex.GlideHelper;

import java.util.List;

class ListAdapter extends RecyclerView.Adapter<ViewHolder> implements View.OnClickListener {

    private static final int TYPE_IMAGES = 0;
    private static final int TYPE_TEXT = 1;

    private final List<Item> items;
    private final OnImageClickListener listener;

    ListAdapter(List<Item> items, OnImageClickListener listener) {
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
                holder.image1.setOnClickListener(this);
                holder.image2.setOnClickListener(this);
                holder.image3.setOnClickListener(this);
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

    private void onBindImages(ImagesViewHolder holder, Item item, int pos) {
        if (item.paintings != null && item.paintings.length > 0) {
            GlideHelper.loadThumb(holder.image1, item.paintings[0].thumbId);
            if (item.paintings.length > 1) {
                holder.imagesRow.setVisibility(View.VISIBLE);
                GlideHelper.loadThumb(holder.image2, item.paintings[1].thumbId);

                if (item.paintings.length > 2) {
                    holder.image3.setVisibility(View.VISIBLE);
                    GlideHelper.loadThumb(holder.image3, item.paintings[2].thumbId);
                } else {
                    holder.image3.setVisibility(View.INVISIBLE);
                }

            } else {
                holder.imagesRow.setVisibility(View.GONE);
            }
        }

        holder.image1.setTag(R.id.tag_item, pos);
        holder.image2.setTag(R.id.tag_item, pos);
        holder.image3.setTag(R.id.tag_item, pos);
    }

    private void onBindText(TextViewHolder holder, Item item) {
        holder.text.setText(item.text);
    }

    @Override
    public void onClick(View view) {
        final int itemPos = (int) view.getTag(R.id.tag_item);
        final int imagePos;

        switch (view.getId()) {
            case R.id.ex8_item_image_1:
                imagePos = 0;
                break;
            case R.id.ex8_item_image_2:
                imagePos = 1;
                break;
            case R.id.ex8_item_image_3:
                imagePos = 2;
                break;
            default:
                throw new IllegalArgumentException();
        }

        listener.onImageClick(items.get(itemPos), itemPos, imagePos);
    }

    static ImageView getImage(RecyclerView.ViewHolder holder, int pos) {
        if (holder instanceof ImagesViewHolder) {
            final ImagesViewHolder imagesHolder = (ImagesViewHolder) holder;
            switch (pos) {
                case 0:
                    return imagesHolder.image1;
                case 1:
                    return imagesHolder.image2;
                case 2:
                    return imagesHolder.image3;
                default:
                    return null;
            }
        } else {
            return null;
        }
    }


    private static class ImagesViewHolder extends ViewHolder {
        final ImageView image1;
        final ImageView image2;
        final ImageView image3;
        final View imagesRow;

        ImagesViewHolder(ViewGroup parent) {
            super(Views.inflate(parent, R.layout.ex8_item_images));

            image1 = Views.find(itemView, R.id.ex8_item_image_1);
            image2 = Views.find(itemView, R.id.ex8_item_image_2);
            image3 = Views.find(itemView, R.id.ex8_item_image_3);
            imagesRow = Views.find(itemView, R.id.ex8_item_images_row);
        }
    }

    private static class TextViewHolder extends ViewHolder {
        final TextView text;

        TextViewHolder(ViewGroup parent) {
            super(Views.inflate(parent, R.layout.ex8_item_text));
            text = (TextView) itemView;
        }
    }

    interface OnImageClickListener {
        void onImageClick(Item item, int itemPos, int imagePos);
    }

}
