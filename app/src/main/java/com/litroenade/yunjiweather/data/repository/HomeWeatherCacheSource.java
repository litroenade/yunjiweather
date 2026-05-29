package com.litroenade.yunjiweather.data.repository;

import androidx.annotation.Nullable;

import com.litroenade.yunjiweather.common.UiState;
import com.litroenade.yunjiweather.data.model.HomeWeatherData;

public interface HomeWeatherCacheSource {
    @Nullable
    UiState<HomeWeatherData> loadCachedHomeWeather(String locationId);
}
