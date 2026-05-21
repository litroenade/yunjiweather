package com.litroenade.yunjiweather.ui.home;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.litroenade.yunjiweather.data.model.WeatherDailyData;
import com.litroenade.yunjiweather.databinding.ItemHomeDailyBinding;
import com.litroenade.yunjiweather.utils.WeatherDisplayUtils;
import com.litroenade.yunjiweather.utils.WeatherIconUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public final class DailyForecastAdapter extends RecyclerView.Adapter<DailyForecastAdapter.DailyViewHolder> {

    private final List<WeatherDailyData> items = new ArrayList<>();
    private String temperatureUnit = WeatherDisplayUtils.TEMPERATURE_CELSIUS;

    public void submitData(List<WeatherDailyData> nextItems, String nextTemperatureUnit) {
        List<WeatherDailyData> oldItems = new ArrayList<>(items);
        List<WeatherDailyData> newItems = nextItems == null ? new ArrayList<>() : new ArrayList<>(nextItems);
        String oldTemperatureUnit = temperatureUnit;
        String newTemperatureUnit = nextTemperatureUnit == null
                ? WeatherDisplayUtils.TEMPERATURE_CELSIUS
                : nextTemperatureUnit;
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
                        oldItems.get(oldItemPosition).getDateText(),
                        newItems.get(newItemPosition).getDateText()
                );
            }

            @Override
            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                WeatherDailyData oldItem = oldItems.get(oldItemPosition);
                WeatherDailyData newItem = newItems.get(newItemPosition);
                return Objects.equals(oldItem.getCondition(), newItem.getCondition())
                        && Objects.equals(oldItem.getTempMin(), newItem.getTempMin())
                        && Objects.equals(oldItem.getTempMax(), newItem.getTempMax())
                        && Objects.equals(oldItem.getIconCode(), newItem.getIconCode())
                        && Objects.equals(oldTemperatureUnit, newTemperatureUnit);
            }
        });
        items.clear();
        items.addAll(newItems);
        temperatureUnit = newTemperatureUnit;
        diffResult.dispatchUpdatesTo(this);
    }

    @NonNull
    @Override
    public DailyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemHomeDailyBinding binding = ItemHomeDailyBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new DailyViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull DailyViewHolder holder, int position) {
        holder.bind(items.get(position), temperatureUnit);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static final class DailyViewHolder extends RecyclerView.ViewHolder {
        private final ItemHomeDailyBinding binding;

        private DailyViewHolder(ItemHomeDailyBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        private void bind(WeatherDailyData item, String temperatureUnit) {
            binding.dailyDateText.setText(item.getDateText());
            binding.dailyIconImage.setImageResource(WeatherIconUtils.getWeatherIconRes(item.getIconCode()));
            binding.dailyConditionText.setText(item.getCondition());
            String temperatureRangeText = String.format(
                    Locale.CHINA,
                    "%s / %s",
                    WeatherDisplayUtils.formatTemperature(item.getTempMin(), temperatureUnit),
                    WeatherDisplayUtils.formatTemperature(item.getTempMax(), temperatureUnit)
            );
            binding.dailyTempText.setText(temperatureRangeText);
        }
    }
}
