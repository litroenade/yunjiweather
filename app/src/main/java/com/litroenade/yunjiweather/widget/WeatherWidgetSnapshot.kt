package com.litroenade.yunjiweather.widget

data class WeatherWidgetSnapshot @JvmOverloads constructor(
    val cityName: String,
    val temperatureText: String,
    val conditionText: String,
    val rangeText: String,
    val updateText: String,
    val isAvailable: Boolean,
    val humidityText: String = "",
    val windText: String = "",
    val airQualityText: String = "",
    val adviceText: String = ""
)
