package com.litroenade.yunjiweather.widget

import com.litroenade.yunjiweather.data.model.CustomThemeAsset
import com.litroenade.yunjiweather.data.model.HomeWeatherData
import com.litroenade.yunjiweather.utils.WeatherDisplayUtils

object WeatherWidgetSnapshotFactory {

    @JvmStatic
    fun fromHomeWeather(
        data: HomeWeatherData,
        updateText: String,
        customBackground: CustomThemeAsset = CustomThemeAsset.empty()
    ): WeatherWidgetSnapshot {
        return fromHomeWeather(data, updateText, customBackground, WeatherDisplayUtils.TEMPERATURE_CELSIUS)
    }

    @JvmStatic
    fun fromHomeWeather(
        data: HomeWeatherData,
        updateText: String,
        customBackground: CustomThemeAsset,
        temperatureUnit: String
    ): WeatherWidgetSnapshot {
        return WeatherWidgetSnapshot(
            cityName = data.cityName,
            temperatureText = WeatherDisplayUtils.formatTemperature(data.temperature, temperatureUnit),
            conditionText = data.condition,
            rangeText = "${WeatherDisplayUtils.formatTemperature(data.tempMax, temperatureUnit)} / ${WeatherDisplayUtils.formatTemperature(data.tempMin, temperatureUnit)}",
            updateText = updateText,
            isAvailable = true,
            humidityText = "\u6e7f\u5ea6 ${data.humidity}%",
            windText = "${data.windDir} ${data.windScale}",
            airQualityText = "\u7a7a\u6c14 ${data.airQualityCategory}",
            adviceText = data.travelAdvice,
            iconCode = data.iconCode,
            clothingValue = clothingValue(data.temperature, data.clothingAdvice),
            fishingValue = fishingValue(data.condition),
            sunsetValue = sunsetValue(data.condition),
            coldValue = coldValue(data.temperature),
            customBackgroundUri = customBackground.uri,
            customBackgroundCropAnchor = customBackground.cropAnchor,
            customBackgroundMediaType = customBackground.mediaType
        )
    }

    @JvmStatic
    fun unavailable(cityName: String): WeatherWidgetSnapshot {
        return WeatherWidgetSnapshot(
            cityName = cityName,
            temperatureText = "\u6253\u5f00\u67e5\u770b\u5b9e\u65f6\u5929\u6c14",
            conditionText = "\u6682\u65e0\u7f13\u5b58",
            rangeText = "\u57ce\u5e02\u3001\u9884\u8b66\u3001\u751f\u6d3b\u5efa\u8bae",
            updateText = "",
            isAvailable = false
        )
    }

    private fun clothingValue(temperature: String, advice: String): String {
        val normalizedAdvice = advice.trim()
        val parsedTemperature = parseTemperature(temperature)
        return when {
            normalizedAdvice.contains("\u77ed\u8896") -> "\u77ed\u8896"
            normalizedAdvice.contains("\u5916\u5957") -> "\u5916\u5957"
            normalizedAdvice.contains("\u7fbd\u7ed2") -> "\u7fbd\u7ed2\u670d"
            normalizedAdvice.contains("\u6bdb\u8863") -> "\u6bdb\u8863"
            parsedTemperature == null -> "\u6682\u65e0"
            parsedTemperature >= 26 -> "\u77ed\u8896"
            parsedTemperature <= 10 -> "\u539a\u5916\u5957"
            else -> "\u8584\u5916\u5957"
        }
    }

    private fun fishingValue(condition: String): String {
        return if (
            condition.contains("\u96e8") ||
            condition.contains("\u96ea") ||
            condition.contains("\u96f7")
        ) {
            "\u4e0d\u5b9c"
        } else {
            "\u9002\u5b9c"
        }
    }

    private fun sunsetValue(condition: String): String {
        return if (condition.contains("\u6674") || condition.contains("\u5c11\u4e91")) {
            "\u8f83\u597d"
        } else {
            "\u4e00\u822c"
        }
    }

    private fun coldValue(temperature: String): String {
        val parsedTemperature = parseTemperature(temperature) ?: return "\u6682\u65e0"
        return if (parsedTemperature <= 12) "\u6ce8\u610f" else "\u4e0d\u6613"
    }

    private fun parseTemperature(temperature: String): Int? {
        return temperature.trim().toDoubleOrNull()?.toInt()
    }
}
