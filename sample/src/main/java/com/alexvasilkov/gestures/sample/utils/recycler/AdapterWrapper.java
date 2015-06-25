package com.alexvasilkov.gestures.sample.utils.recycler;

import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * RecyclerView adapter wrapper with headers / mFooters support
 */
public class AdapterWrapper<VH extends RecyclerView.ViewHolder>
        extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int H_TYPE_START = Integer.MIN_VALUE >> 1;
    private static final int F_TYPE_START = Integer.MIN_VALUE >> 2;

    private final RecyclerView.Adapter<VH> mWrapped;
    private final List<View> mHeaders = new ArrayList<>();
    private final List<View> mFooters = new ArrayList<>();

    public AdapterWrapper(RecyclerView.Adapter<VH> wrapped) {
        mWrapped = wrapped;

        wrapped.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                AdapterWrapper.this.notifyDataSetChanged();
            }

            @Override
            public void onItemRangeChanged(int start, int count) {
                AdapterWrapper.this.notifyItemRangeChanged(start + getHeadersCount(), count);
            }

            @Override
            public void onItemRangeInserted(int start, int count) {
                AdapterWrapper.this.notifyItemRangeInserted(start + getHeadersCount(), count);
            }

            @Override
            public void onItemRangeRemoved(int start, int count) {
                AdapterWrapper.this.notifyItemRangeRemoved(start + getHeadersCount(), count);
            }

            @Override
            public void onItemRangeMoved(int from, int to, int count) {
                int offset = getHeadersCount();
                for (int c = 0; c < count; c++) {
                    AdapterWrapper.this.notifyItemMoved(from + c + offset, to + c + offset);
                }
            }
        });
    }

    public RecyclerView.Adapter<VH> getWrappedAdapter() {
        return mWrapped;
    }

    public int getWrappedCount() {
        return mWrapped.getItemCount();
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

    public int getHeadersCount() {
        return mHeaders.size();
    }

    public boolean isHeader(int pos) {
        return pos < getHeadersCount();
    }

    public void addFooter(View view) {
        mFooters.add(view);
        notifyItemInserted(getHeadersCount() + getWrappedCount() + getFootersCount());
    }

    public void removeFooter(View view) {
        int pos = mFooters.indexOf(view);
        if (pos != -1) {
            mFooters.remove(pos);
            notifyItemRemoved(getHeadersCount() + getWrappedCount() + pos);
        }
    }

    public int getFootersCount() {
        return mFooters.size();
    }

    public boolean isFooter(int pos) {
        return pos >= getHeadersCount() + getWrappedCount();
    }

    public int getWrappedPosition(int pos) {
        if (isHeader(pos) || isFooter(pos)) return -1;
        return pos - getHeadersCount();
    }


    @Override
    public int getItemCount() {
        return getHeadersCount() + getWrappedCount() + getFootersCount();
    }

    @Override
    public int getItemViewType(int position) {
        if (isHeader(position)) {
            return H_TYPE_START + position;
        } else if (isFooter(position)) {
            return F_TYPE_START + position - getHeadersCount() - getWrappedCount();
        } else {
            return mWrapped.getItemViewType(position - getHeadersCount());
        }
    }

    @Override
    public long getItemId(int position) {
        if (isHeader(position) || isFooter(position)) {
            return getItemViewType(position);
        } else {
            return mWrapped.getItemId(position - mHeaders.size());
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType >= H_TYPE_START && viewType < H_TYPE_START + getHeadersCount()) {
            int pos = viewType - H_TYPE_START;
            return new SimpleViewHolder(mHeaders.get(pos));
        } else if (viewType >= F_TYPE_START && viewType < F_TYPE_START + getFootersCount()) {
            int pos = viewType - F_TYPE_START;
            return new SimpleViewHolder(mFooters.get(pos));
        } else {
            return mWrapped.onCreateViewHolder(parent, viewType);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (!isHeader(position) && !isFooter(position)) {
            mWrapped.onBindViewHolder((VH) holder, position - getHeadersCount());
        }
    }

    /**
     * Making headers and footers occupy full width
     */
    public GridLayoutManager.SpanSizeLookup getGridSpanSizes(final GridLayoutManager manager) {
        return new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int pos) {
                return isHeader(pos) || isFooter(pos) ? manager.getSpanCount() : 1;
            }
        };
    }


    private static class SimpleViewHolder extends RecyclerView.ViewHolder {
        public SimpleViewHolder(View itemView) {
            super(itemView);
        }
    }

}
