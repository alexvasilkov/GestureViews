package com.alexvasilkov.gestures.sample.items;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

public class ExtendedAdapter<VH extends RecyclerView.ViewHolder>
        extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int HEADERS_TYPE_START = Integer.MIN_VALUE >> 1;
    private static final int FOOTERS_TYPE_START = Integer.MIN_VALUE >> 2;

    private final RecyclerView.Adapter<VH> mWrapped;
    private final List<View> mHeaders = new ArrayList<>();
    private final List<View> mFooters = new ArrayList<>();

    public ExtendedAdapter(RecyclerView.Adapter<VH> wrapped) {
        mWrapped = wrapped;

        wrapped.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                ExtendedAdapter.this.notifyDataSetChanged();
            }

            @Override
            public void onItemRangeChanged(int positionStart, int itemCount) {
                ExtendedAdapter.this.notifyItemRangeChanged(positionStart, itemCount);
            }

            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                ExtendedAdapter.this.notifyItemRangeInserted(positionStart, itemCount);
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                ExtendedAdapter.this.notifyItemRangeRemoved(positionStart, itemCount);
            }

            @Override
            public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
                for (int c = 0; c < itemCount; c++) {
                    ExtendedAdapter.this.notifyItemMoved(fromPosition + c, toPosition + c);
                }
            }
        });
    }

    public void addHeader(View view) {
        mHeaders.add(view);
        notifyItemInserted(mHeaders.size());
    }

    public void removeHeader(View view) {
        int pos = mHeaders.indexOf(view);
        if (pos != -1) {
            mHeaders.remove(pos);
            notifyItemRemoved(pos);
        }
    }

    public void addFooter(View view) {
        mFooters.add(view);
        notifyItemInserted(mHeaders.size() + mWrapped.getItemCount() + mFooters.size());
    }

    public void removeFooter(View view) {
        int pos = mFooters.indexOf(view);
        if (pos != -1) {
            mFooters.remove(pos);
            notifyItemRemoved(mHeaders.size() + mWrapped.getItemCount() + pos);
        }
    }

    @Override
    public int getItemCount() {
        return mHeaders.size() + mWrapped.getItemCount() + mFooters.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (position < mHeaders.size()) {
            return HEADERS_TYPE_START + position;
        } else if (position >= mHeaders.size() + mWrapped.getItemCount()) {
            return FOOTERS_TYPE_START + position - mHeaders.size() - mWrapped.getItemCount();
        } else {
            return mWrapped.getItemViewType(position - mHeaders.size());
        }
    }

    @Override
    public long getItemId(int position) {
        if (position >= mHeaders.size() && position < mHeaders.size() + mWrapped.getItemCount()) {
            return mWrapped.getItemId(position - mHeaders.size());
        } else {
            return getItemViewType(position);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType >= HEADERS_TYPE_START && viewType < HEADERS_TYPE_START + mHeaders.size()) {
            int pos = viewType - HEADERS_TYPE_START;
            return new SimpleViewHolder(mHeaders.get(pos));
        } else if (viewType >= FOOTERS_TYPE_START && viewType < FOOTERS_TYPE_START + mFooters.size()) {
            int pos = viewType - FOOTERS_TYPE_START;
            return new SimpleViewHolder(mFooters.get(pos));
        } else {
            return mWrapped.onCreateViewHolder(parent, viewType);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (position >= mHeaders.size() && position < mHeaders.size() + mWrapped.getItemCount()) {
            mWrapped.onBindViewHolder((VH) holder, position - mHeaders.size());
        }
    }


    private static class SimpleViewHolder extends RecyclerView.ViewHolder {
        public SimpleViewHolder(View itemView) {
            super(itemView);
        }
    }

}
