package team.itis.ivm.ui.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

public class UniversalAdapter<T> extends RecyclerView.Adapter<UniversalAdapter.ItemViewHolder<T>> {
    private ItemViewHolder.ItemViewHolderCallback<T> itemViewHolderCallback = null;
    private ArrayList<T> mItems = new ArrayList<>();
    private int mRowResId;

    public UniversalAdapter(ArrayList<T> items, int rowResId) {
        mItems = items;
        mRowResId = rowResId;
    }

    public void setItems(ArrayList<T> items) {
        mItems = items;
        notifyDataSetChanged();
    }

    @Override
    public ItemViewHolder<T> onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(mRowResId, parent, false);
        return new ItemViewHolder<>(view, itemViewHolderCallback);
    }

    @Override
    public void onBindViewHolder(final ItemViewHolder<T> holder, int position) {
        holder.setItem(mItems.get(position));
    }

    @Override
    public void onViewRecycled(ItemViewHolder holder) {

    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public void setItemViewHolderCallback(ItemViewHolder.ItemViewHolderCallback<T> itemViewHolderCallback) {
        this.itemViewHolderCallback = itemViewHolderCallback;
    }


    public static class ItemViewHolder<T> extends RecyclerView.ViewHolder implements View.OnClickListener {
        private T mItem;
        private ItemViewHolderCallback<T> itemViewHolderCallback = null;

        ItemViewHolder(View itemView, ItemViewHolderCallback<T> itemViewHolderCallback) {
            super(itemView);
            this.itemViewHolderCallback = itemViewHolderCallback;
            itemView.setOnClickListener(this);
        }

        void setItem(T item) {
            mItem = item;
            if (itemViewHolderCallback != null) itemViewHolderCallback.setItem(itemView, item);
        }

        @Override
        public void onClick(View v) {
            if (itemViewHolderCallback != null) itemViewHolderCallback.onClick(itemView, mItem);
        }

        public static abstract class ItemViewHolderCallback<T> {
            public abstract void onClick(View itemView, T item);

            public abstract void setItem(View itemView, T item);
        }
    }
}
