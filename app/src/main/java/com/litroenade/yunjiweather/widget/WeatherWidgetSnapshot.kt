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
    val clothingValue: String = "短袖",
    val fishingValue: String = "适宜",
    val sunsetValue: String = "一般",
    val coldValue: String = "不易",
    val customBackgroundUri: String = "",
    val customBackgroundCropAnchor: String = "center",
    val customBackgroundMediaType: String = "image"
)
