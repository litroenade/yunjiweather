package com.litroenade.yunjiweather.data.repository;

import com.litroenade.yunjiweather.common.UiState;
import com.litroenade.yunjiweather.data.model.HomeWeatherData;

public interface HomeWeatherCacheSource {
    UiState<HomeWeatherData> loadCachedHomeWeather(String locationId);
}
