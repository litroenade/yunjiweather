package com.litroenade.yunjiweather.data.repository;

import com.litroenade.yunjiweather.common.UiState;
import com.litroenade.yunjiweather.data.entity.CityEntity;
import com.litroenade.yunjiweather.data.model.CityWeatherSummary;
import com.litroenade.yunjiweather.data.model.HomeWeatherData;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class CityWeatherSummaryRepository {

    private final HomeWeatherSource homeWeatherSource;

    public CityWeatherSummaryRepository(HomeWeatherSource homeWeatherSource) {
        this.homeWeatherSource = Objects.requireNonNull(homeWeatherSource, "homeWeatherSource");
    }

    public Map<String, CityWeatherSummary> loadSummaries(List<CityEntity> cities) {
        return loadSummaries(cities, null);
    }

    public Map<String, CityWeatherSummary> loadSummaries(
            List<CityEntity> cities,
            SummaryUpdateListener updateListener
    ) {
        Objects.requireNonNull(cities, "cities");
        Map<String, CityWeatherSummary> summaries = new LinkedHashMap<>();
        for (CityEntity city : cities) {
            Objects.requireNonNull(city, "city");
            UiState<HomeWeatherData> state = homeWeatherSource.loadHomeWeather(
                    city.locationId,
                    city.cityName,
                    city.latitude,
                    city.longitude
            );
            summaries.put(city.locationId, CityWeatherSummary.fromWeatherState(city.locationId, state));
            if (updateListener != null) {
                updateListener.onSummariesUpdated(new LinkedHashMap<>(summaries));
            }
        }
        return summaries;
    }

    public interface SummaryUpdateListener {
        void onSummariesUpdated(Map<String, CityWeatherSummary> summaries);
    }
}
