package com.litroenade.yunjiweather.ui.city;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.litroenade.yunjiweather.R;
import com.litroenade.yunjiweather.data.entity.CityEntity;
import com.litroenade.yunjiweather.data.model.CityWeatherSummary;
import com.litroenade.yunjiweather.databinding.ItemCityCardBinding;
import com.litroenade.yunjiweather.utils.WeatherDisplayUtils;
import com.litroenade.yunjiweather.utils.WeatherIconUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class CityAdapter extends RecyclerView.Adapter<CityAdapter.CityViewHolder> {

    public interface Listener {
        void onSetDefault(CityEntity city);

        void onDelete(CityEntity city);
    }

    private final Listener listener;
    private final List<CityEntity> cities = new ArrayList<>();
    private Map<String, CityWeatherSummary> summaries = Collections.emptyMap();
    private String defaultCity = "";
    private String temperatureUnit = WeatherDisplayUtils.TEMPERATURE_CELSIUS;

    public CityAdapter(Listener listener) {
        this.listener = listener;
    }

    public void submitData(
            List<CityEntity> nextCities,
            Map<String, CityWeatherSummary> nextSummaries,
            String nextDefaultCity,
            String nextTemperatureUnit
    ) {
        List<CityEntity> oldCities = new ArrayList<>(cities);
        Map<String, CityWeatherSummary> oldSummaries = summaries;
        String oldDefaultCity = defaultCity;
        String oldTemperatureUnit = temperatureUnit;
        List<CityEntity> newCities = nextCities == null ? Collections.emptyList() : new ArrayList<>(nextCities);
        Map<String, CityWeatherSummary> newSummaries = nextSummaries == null ? Collections.emptyMap() : nextSummaries;
        String newDefaultCity = nextDefaultCity == null ? "" : nextDefaultCity;
        String newTemperatureUnit = nextTemperatureUnit == null ? WeatherDisplayUtils.TEMPERATURE_CELSIUS : nextTemperatureUnit;
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() {
                return oldCities.size();
            }

            @Override
            public int getNewListSize() {
                return newCities.size();
            }

            @Override
            public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                return Objects.equals(
                        oldCities.get(oldItemPosition).locationId,
                        newCities.get(newItemPosition).locationId
                );
            }

            @Override
            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                CityEntity oldCity = oldCities.get(oldItemPosition);
                CityEntity newCity = newCities.get(newItemPosition);
                String oldText = CitySummaryFormatter.format(
                        oldCity,
                        oldCity.cityName.equals(oldDefaultCity),
                        oldSummaries.get(oldCity.locationId),
                        oldTemperatureUnit
                );
                String newText = CitySummaryFormatter.format(
                        newCity,
                        newCity.cityName.equals(newDefaultCity),
                        newSummaries.get(newCity.locationId),
                        newTemperatureUnit
                );
                return Objects.equals(oldCity.cityName, newCity.cityName)
                        && Objects.equals(oldCity.province, newCity.province)
                        && Objects.equals(oldCity.country, newCity.country)
                        && Objects.equals(oldText, newText);
            }
        });
        cities.clear();
        cities.addAll(newCities);
        summaries = newSummaries;
        defaultCity = newDefaultCity;
        temperatureUnit = newTemperatureUnit;
        diffResult.dispatchUpdatesTo(this);
    }

    @NonNull
    @Override
    public CityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCityCardBinding binding = ItemCityCardBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new CityViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CityViewHolder holder, int position) {
        CityEntity city = cities.get(position);
        boolean isDefault = city.cityName.equals(defaultCity);
        holder.bind(city, isDefault, summaries.get(city.locationId), temperatureUnit, listener);
    }

    @Override
    public int getItemCount() {
        return cities.size();
    }

    static final class CityViewHolder extends RecyclerView.ViewHolder {
        private final ItemCityCardBinding binding;

        private CityViewHolder(ItemCityCardBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        private void bind(
                CityEntity city,
                boolean isDefault,
                CityWeatherSummary summary,
                String temperatureUnit,
                Listener listener
        ) {
            binding.cityNameText.setText(city.cityName);
            binding.cityMetaText.setText(binding.getRoot().getContext().getString(
                    R.string.city_meta_format,
                    city.province,
                    city.country
            ));
            binding.defaultBadgeText.setVisibility(isDefault ? View.VISIBLE : View.GONE);
            renderWeatherSummary(summary, temperatureUnit);
            binding.setDefaultButton.setEnabled(!isDefault);
            binding.setDefaultButton.setText(isDefault ? "默认" : "设为默认");
            binding.setDefaultButton.setOnClickListener(view -> listener.onSetDefault(city));
            binding.deleteCityButton.setOnClickListener(view -> listener.onDelete(city));
        }

        private void renderWeatherSummary(CityWeatherSummary summary, String temperatureUnit) {
            if (summary == null) {
                binding.cityWeatherIconImage.setImageResource(WeatherIconUtils.getWeatherIconRes(""));
                binding.cityTemperatureText.setText("--");
                binding.cityRangeText.setText("加载中");
                binding.citySummaryText.setText("天气摘要加载中");
                return;
            }
            if (!summary.getErrorMessage().isEmpty()) {
                binding.cityWeatherIconImage.setImageResource(WeatherIconUtils.getWeatherIconRes(""));
                binding.cityTemperatureText.setText("--");
                binding.cityRangeText.setText("");
                binding.citySummaryText.setText(summary.getErrorMessage());
                return;
            }
            binding.cityWeatherIconImage.setImageResource(resolveWeatherIcon(summary.getCondition()));
            binding.cityTemperatureText.setText(WeatherDisplayUtils.formatTemperature(
                    summary.getTemperature(),
                    temperatureUnit
            ));
            binding.cityRangeText.setText(binding.getRoot().getContext().getString(
                    R.string.city_range_format,
                    WeatherDisplayUtils.formatTemperature(summary.getTempMin(), temperatureUnit),
                    WeatherDisplayUtils.formatTemperature(summary.getTempMax(), temperatureUnit)
            ));
            binding.citySummaryText.setText(binding.getRoot().getContext().getString(
                    R.string.city_summary_state_format,
                    summary.getCondition(),
                    binding.getRoot().getContext().getString(summary.isFromCache()
                            ? R.string.city_summary_cache
                            : R.string.city_summary_realtime)
            ));
        }

        private int resolveWeatherIcon(String condition) {
            if (condition == null) {
                return WeatherIconUtils.getWeatherIconRes("");
            }
            if (condition.contains("雨")) {
                return WeatherIconUtils.getWeatherIconRes("300");
            }
            if (condition.contains("雪")) {
                return WeatherIconUtils.getWeatherIconRes("400");
            }
            if (condition.contains("晴")) {
                return WeatherIconUtils.getWeatherIconRes("100");
            }
            return WeatherIconUtils.getWeatherIconRes("");
        }
    }
}
