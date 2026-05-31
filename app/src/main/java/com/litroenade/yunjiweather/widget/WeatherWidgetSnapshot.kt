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
    val clothingValue: String = "\u77ed\u8896",
    val fishingValue: String = "\u9002\u5b9c",
    val sunsetValue: String = "\u4e00\u822c",
    val coldValue: String = "\u4e0d\u6613",
    val customBackgroundUri: String = "",
    val customBackgroundCropAnchor: String = "center",
    val customBackgroundMediaType: String = "image"
)
