package com.litroenade.yunjiweather.data.repository

import com.litroenade.yunjiweather.common.UiState
import com.litroenade.yunjiweather.data.model.HomeWeatherData

interface HomeWeatherSource {
    fun loadHomeWeather(
        locationId: String,
        cityName: String,
        latitude: Double,
        longitude: Double
    ): UiState<HomeWeatherData>
}
