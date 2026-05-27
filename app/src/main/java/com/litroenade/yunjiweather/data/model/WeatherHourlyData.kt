package com.litroenade.yunjiweather.data.model

data class WeatherHourlyData(
    val timeText: String,
    val temperature: String,
    val condition: String,
    val iconCode: String
) {
    init {
        requireText(timeText, "timeText")
        requireText(temperature, "temperature")
        requireText(condition, "condition")
        requireText(iconCode, "iconCode")
    }

    private fun requireText(value: String, fieldName: String) {
        require(value.trim().isNotEmpty()) { "$fieldName must not be empty" }
    }
}
