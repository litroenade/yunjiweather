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
    private final HomeWeatherCacheSource homeWeatherCacheSource;
    private final boolean remoteFetchForMissingCache;

    public CityWeatherSummaryRepository(HomeWeatherSource homeWeatherSource) {
        this(homeWeatherSource, null, true);
    }

    public CityWeatherSummaryRepository(
            HomeWeatherSource homeWeatherSource,
            HomeWeatherCacheSource homeWeatherCacheSource,
            boolean remoteFetchForMissingCache
    ) {
        this.homeWeatherSource = Objects.requireNonNull(homeWeatherSource, "homeWeatherSource");
        this.homeWeatherCacheSource = homeWeatherCacheSource;
        this.remoteFetchForMissingCache = remoteFetchForMissingCache;
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
            UiState<HomeWeatherData> state = loadStateForSummary(city);
            summaries.put(city.locationId, CityWeatherSummary.fromWeatherState(city.locationId, state));
            if (updateListener != null) {
                updateListener.onSummariesUpdated(new LinkedHashMap<>(summaries));
            }
        }
        return summaries;
    }

    private UiState<HomeWeatherData> loadStateForSummary(CityEntity city) {
        if (homeWeatherCacheSource != null) {
            UiState<HomeWeatherData> cachedState = homeWeatherCacheSource.loadCachedHomeWeather(city.locationId);
            if (cachedState != null && cachedState.getData() != null) {
                return cachedState;
            }
            if (!remoteFetchForMissingCache) {
                return null;
            }
        }
        return homeWeatherSource.loadHomeWeather(
                city.locationId,
                city.cityName,
                city.latitude,
                city.longitude
        );
    }

    public interface SummaryUpdateListener {
        void onSummariesUpdated(Map<String, CityWeatherSummary> summaries);
    }
}
