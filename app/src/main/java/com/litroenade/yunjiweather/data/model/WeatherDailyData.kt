package com.litroenade.yunjiweather.data.model

data class WeatherDailyData(
    val dateText: String,
    val tempMax: String,
    val tempMin: String,
    val condition: String,
    val iconCode: String
) {
    init {
        requireText(dateText, "dateText")
        requireText(tempMax, "tempMax")
        requireText(tempMin, "tempMin")
        requireText(condition, "condition")
        requireText(iconCode, "iconCode")
    }

    private fun requireText(value: String, fieldName: String) {
        require(value.trim().isNotEmpty()) { "$fieldName must not be empty" }
    }
}
