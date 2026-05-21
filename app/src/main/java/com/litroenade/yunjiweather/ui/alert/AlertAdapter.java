package com.litroenade.yunjiweather.ui.alert;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.litroenade.yunjiweather.R;
import com.litroenade.yunjiweather.data.entity.WarningEntity;
import com.litroenade.yunjiweather.databinding.ItemWarningCardBinding;
import com.litroenade.yunjiweather.utils.DateTimeUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public final class AlertAdapter extends RecyclerView.Adapter<AlertAdapter.AlertViewHolder> {

    public interface Listener {
        void onClick(WarningEntity warning);
    }

    private final Listener listener;
    private final List<WarningEntity> warnings = new ArrayList<>();

    public AlertAdapter(Listener listener) {
        this.listener = listener;
    }

    public void submitData(List<WarningEntity> nextWarnings) {
        List<WarningEntity> oldWarnings = new ArrayList<>(warnings);
        List<WarningEntity> newWarnings = nextWarnings == null ? new ArrayList<>() : new ArrayList<>(nextWarnings);
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() {
                return oldWarnings.size();
            }

            @Override
            public int getNewListSize() {
                return newWarnings.size();
            }

            @Override
            public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                return Objects.equals(
                        oldWarnings.get(oldItemPosition).warningId,
                        newWarnings.get(newItemPosition).warningId
                );
            }

            @Override
            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                WarningEntity oldWarning = oldWarnings.get(oldItemPosition);
                WarningEntity newWarning = newWarnings.get(newItemPosition);
                return Objects.equals(oldWarning.title, newWarning.title)
                        && Objects.equals(oldWarning.type, newWarning.type)
                        && Objects.equals(oldWarning.level, newWarning.level)
                        && Objects.equals(oldWarning.content, newWarning.content)
                        && oldWarning.publishTime == newWarning.publishTime
                        && oldWarning.isRead == newWarning.isRead
                        && oldWarning.isNotified == newWarning.isNotified;
            }
        });
        warnings.clear();
        warnings.addAll(newWarnings);
        diffResult.dispatchUpdatesTo(this);
    }

    @NonNull
    @Override
    public AlertViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemWarningCardBinding binding = ItemWarningCardBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new AlertViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull AlertViewHolder holder, int position) {
        holder.bind(warnings.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return warnings.size();
    }

    static final class AlertViewHolder extends RecyclerView.ViewHolder {
        private final ItemWarningCardBinding binding;

        private AlertViewHolder(ItemWarningCardBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        private void bind(WarningEntity warning, Listener listener) {
            int warningColor = WarningStyleUtils.resolveColor(warning.level);
            binding.getRoot().setStrokeColor(warningColor);
            binding.warningLevelStrip.setBackgroundColor(warningColor);
            binding.getRoot().setCardBackgroundColor(binding.getRoot().getContext().getColor(
                    warning.isRead ? R.color.weather_surface : R.color.weather_surface_tint
            ));
            binding.warningLevelText.setText(warning.level);
            binding.warningLevelText.setTextColor(warningColor);
            binding.readStateText.setText(warning.isRead ? "已读" : "未读");
            binding.readStateText.setTextColor(warning.isRead
                    ? binding.getRoot().getContext().getColor(R.color.weather_text_secondary)
                    : warningColor);
            binding.warningTitleText.setText(warning.title);
            String metaText = String.format(Locale.CHINA, "类型：%s", warning.type);
            binding.warningMetaText.setText(metaText);
            binding.warningMetaText.setTextColor(warningColor);
            String publishTimeText = String.format(
                    Locale.CHINA,
                    "发布时间：%s",
                    DateTimeUtils.formatMinuteTime(warning.publishTime)
            );
            binding.warningTimeText.setText(publishTimeText);
            binding.warningContentText.setText(warning.content);
            binding.getRoot().setOnClickListener(view -> listener.onClick(warning));
        }
    }
}
