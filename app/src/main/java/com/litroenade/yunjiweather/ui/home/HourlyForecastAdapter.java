package com.litroenade.yunjiweather.ui.home;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.litroenade.yunjiweather.data.model.WeatherHourlyData;
import com.litroenade.yunjiweather.databinding.ItemHomeHourlyBinding;
import com.litroenade.yunjiweather.utils.WeatherDisplayUtils;
import com.litroenade.yunjiweather.utils.WeatherIconUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class HourlyForecastAdapter extends RecyclerView.Adapter<HourlyForecastAdapter.HourlyViewHolder> {

    private final List<WeatherHourlyData> items = new ArrayList<>();
    private String temperatureUnit = WeatherDisplayUtils.TEMPERATURE_CELSIUS;

    public void submitData(List<WeatherHourlyData> nextItems, String nextTemperatureUnit) {
        List<WeatherHourlyData> oldItems = new ArrayList<>(items);
        List<WeatherHourlyData> newItems = nextItems == null ? new ArrayList<>() : new ArrayList<>(nextItems);
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
                        oldItems.get(oldItemPosition).getTimeText(),
                        newItems.get(newItemPosition).getTimeText()
                );
            }

            @Override
            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                WeatherHourlyData oldItem = oldItems.get(oldItemPosition);
                WeatherHourlyData newItem = newItems.get(newItemPosition);
                return Objects.equals(oldItem.getCondition(), newItem.getCondition())
                        && Objects.equals(oldItem.getTemperature(), newItem.getTemperature())
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
    public HourlyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemHomeHourlyBinding binding = ItemHomeHourlyBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new HourlyViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull HourlyViewHolder holder, int position) {
        holder.bind(items.get(position), temperatureUnit);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static final class HourlyViewHolder extends RecyclerView.ViewHolder {
        private final ItemHomeHourlyBinding binding;

        private HourlyViewHolder(ItemHomeHourlyBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        private void bind(WeatherHourlyData item, String temperatureUnit) {
            binding.hourlyTimeText.setText(item.getTimeText());
            binding.hourlyIconImage.setImageResource(WeatherIconUtils.getWeatherIconRes(item.getIconCode()));
            binding.hourlyConditionText.setText(item.getCondition());
            binding.hourlyTempText.setText(WeatherDisplayUtils.formatTemperature(item.getTemperature(), temperatureUnit));
        }
    }
}
