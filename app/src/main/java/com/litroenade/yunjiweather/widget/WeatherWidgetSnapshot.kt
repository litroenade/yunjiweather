package com.litroenade.yunjiweather.widget

data class WeatherWidgetSnapshot(
    val cityName: String,
    val temperatureText: String,
    val conditionText: String,
    val rangeText: String,
    val updateText: String,
    val isAvailable: Boolean
)
