package com.litroenade.yunjiweather.ui.index;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.litroenade.yunjiweather.databinding.ItemLifeIndexCardBinding;
import com.litroenade.yunjiweather.data.model.LifeIndexItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class LifeIndexAdapter extends RecyclerView.Adapter<LifeIndexAdapter.LifeIndexViewHolder> {

    public interface Listener {
        void onClick(LifeIndexItem item);
    }

    private final Listener listener;
    private final List<LifeIndexItem> items = new ArrayList<>();

    public LifeIndexAdapter(Listener listener) {
        this.listener = listener;
    }

    public void submitData(List<LifeIndexItem> nextItems) {
        List<LifeIndexItem> oldItems = new ArrayList<>(items);
        List<LifeIndexItem> newItems = nextItems == null ? new ArrayList<>() : new ArrayList<>(nextItems);
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() {
                return oldItems.size();
            }

            @Override
            public int getNewListSize() {
                return newItems.size();
            }

            @Override
            public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                return Objects.equals(
                        oldItems.get(oldItemPosition).getName(),
                        newItems.get(newItemPosition).getName()
                );
            }

            @Override
            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                LifeIndexItem oldItem = oldItems.get(oldItemPosition);
                LifeIndexItem newItem = newItems.get(newItemPosition);
                return Objects.equals(oldItem.getLevel(), newItem.getLevel())
                        && Objects.equals(oldItem.getAdvice(), newItem.getAdvice())
                        && Objects.equals(oldItem.getDetail(), newItem.getDetail());
            }
        });
        items.clear();
        items.addAll(newItems);
        diffResult.dispatchUpdatesTo(this);
    }

    @NonNull
    @Override
    public LifeIndexViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemLifeIndexCardBinding binding = ItemLifeIndexCardBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new LifeIndexViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull LifeIndexViewHolder holder, int position) {
        holder.bind(items.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static final class LifeIndexViewHolder extends RecyclerView.ViewHolder {
        private final ItemLifeIndexCardBinding binding;

        private LifeIndexViewHolder(ItemLifeIndexCardBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        private void bind(LifeIndexItem item, Listener listener) {
            binding.indexIconText.setText(resolveIconText(item.getName()));
            binding.indexNameText.setText(item.getName());
            binding.indexLevelText.setText(item.getLevel());
            binding.indexAdviceText.setText(item.getAdvice());
            binding.getRoot().setOnClickListener(view -> listener.onClick(item));
        }

        private String resolveIconText(String name) {
            if (name == null || name.trim().isEmpty()) {
                return "指";
            }
            if (name.contains("穿") || name.contains("衣")) {
                return "衣";
            }
            if (name.contains("出行") || name.contains("旅游") || name.contains("旅行")) {
                return "行";
            }
            if (name.contains("运动")) {
                return "动";
            }
            if (name.contains("洗车")) {
                return "车";
            }
            if (name.contains("紫外线")) {
                return "UV";
            }
            if (name.contains("感冒")) {
                return "康";
            }
            if (name.contains("空气")) {
                return "气";
            }
            if (name.contains("舒适")) {
                return "舒";
            }
            if (name.contains("晾晒")) {
                return "晒";
            }
            return name.substring(0, 1);
        }
    }
}
