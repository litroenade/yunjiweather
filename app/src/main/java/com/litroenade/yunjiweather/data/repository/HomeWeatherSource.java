package com.litroenade.yunjiweather.data.repository;

import com.litroenade.yunjiweather.common.UiState;
import com.litroenade.yunjiweather.data.model.HomeWeatherData;

public interface HomeWeatherSource {
    UiState<HomeWeatherData> loadHomeWeather(String locationId, String cityName, double latitude, double longitude);
}
