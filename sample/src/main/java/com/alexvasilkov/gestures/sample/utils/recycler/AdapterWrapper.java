package com.alexvasilkov.gestures.sample.utils.recycler;

import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import static android.support.v7.widget.RecyclerView.AdapterDataObserver;

/**
 * RecyclerView adapter wrapper with headers / footers support
 */
public class AdapterWrapper<VH extends ViewHolder> extends RecyclerView.Adapter<ViewHolder> {

    private static final int H_TYPE_START = Integer.MIN_VALUE >> 1;
    private static final int F_TYPE_START = Integer.MIN_VALUE >> 2;

    private final RecyclerView.Adapter<VH> mWrapped;
    private final List<ItemAdapter<ViewHolder>> mHeaders = new ArrayList<>();
    private final List<ItemAdapter<ViewHolder>> mFooters = new ArrayList<>();

    public AdapterWrapper(RecyclerView.Adapter<VH> wrapped) {
        mWrapped = wrapped;

        wrapped.registerAdapterDataObserver(new AdapterDataObserver() {
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

    @SuppressWarnings("unchecked")
    public void addHeader(ItemAdapter<? extends ViewHolder> item) {
        int pos = find(mHeaders, item);
        if (pos == -1) {
            mHeaders.add((ItemAdapter<ViewHolder>) item);
            notifyItemInserted(getHeadersCount() - 1);
        }
    }

    public void removeHeader(ItemAdapter<? extends ViewHolder> item) {
        int pos = find(mHeaders, item);
        if (pos != -1) {
            mHeaders.remove(pos);
            notifyItemRemoved(pos);
        }
    }

    public void updateHeader(ItemAdapter<? extends ViewHolder> item) {
        int pos = find(mHeaders, item);
        if (pos != -1) notifyItemChanged(pos);
    }

    public int getHeadersCount() {
        return mHeaders.size();
    }

    public boolean isHeader(int pos) {
        return pos < getHeadersCount();
    }

    @SuppressWarnings("unchecked")
    public void addFooter(ItemAdapter<? extends ViewHolder> item) {
        int pos = find(mFooters, item);
        if (pos == -1) {
            mFooters.add((ItemAdapter<ViewHolder>) item);
            notifyItemInserted(getHeadersCount() + getWrappedCount() + getFootersCount() - 1);
        }
    }

    public void removeFooter(ItemAdapter<? extends ViewHolder> item) {
        int pos = find(mFooters, item);
        if (pos != -1) {
            mFooters.remove(pos);
            notifyItemRemoved(getHeadersCount() + getWrappedCount() + pos);
        }
    }

    public void updateFooter(ItemAdapter<? extends ViewHolder> item) {
        int pos = find(mFooters, item);
        if (pos != -1) notifyItemChanged(getHeadersCount() + getWrappedCount() + pos);
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
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType >= H_TYPE_START && viewType < H_TYPE_START + getHeadersCount()) {
            int pos = viewType - H_TYPE_START;
            return mHeaders.get(pos).onCreateViewHolder(parent);
        } else if (viewType >= F_TYPE_START && viewType < F_TYPE_START + getFootersCount()) {
            int pos = viewType - F_TYPE_START;
            return mFooters.get(pos).onCreateViewHolder(parent);
        } else {
            return mWrapped.onCreateViewHolder(parent, viewType);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (isHeader(position)) {
            mHeaders.get(position).onBindViewHolder(holder);
        } else if (isFooter(position)) {
            mFooters.get(position - getHeadersCount() - getWrappedCount()).onBindViewHolder(holder);
        } else {
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

    @SuppressWarnings({"RedundantCast", "unchecked"})
    private static int find(List<ItemAdapter<ViewHolder>> list,
                            ItemAdapter<? extends ViewHolder> item) {
        return list.indexOf((ItemAdapter<ViewHolder>) item);
    }

    public interface ItemAdapter<T extends ViewHolder> {
        T onCreateViewHolder(ViewGroup parent);

        void onBindViewHolder(T holder);
    }

}
