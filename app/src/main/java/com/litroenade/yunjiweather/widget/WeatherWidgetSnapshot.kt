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
    val adviceText: String = "",
    val iconCode: String = "",
    val clothingValue: String = "\u6682\u65e0",
    val fishingValue: String = "\u6682\u65e0",
    val sunsetValue: String = "\u6682\u65e0",
    val coldValue: String = "\u6682\u65e0",
    val customBackgroundUri: String = "",
    val customBackgroundCropAnchor: String = "center",
    val customBackgroundMediaType: String = "image"
)
