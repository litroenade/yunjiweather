package com.litroenade.yunjiweather.data.model

import com.litroenade.yunjiweather.common.UiState

data class CityWeatherSummary(
    val locationId: String,
    val temperature: String,
    val condition: String,
    val tempMax: String,
    val tempMin: String,
    val updateTime: Long,
    val isFromCache: Boolean,
    val errorMessage: String
) {
    init {
        requireText(locationId, "locationId")
        requireOptionalText(temperature, "temperature")
        requireOptionalText(condition, "condition")
        requireOptionalText(tempMax, "tempMax")
        requireOptionalText(tempMin, "tempMin")
        requireOptionalText(errorMessage, "errorMessage")
    }

    companion object {
        private const val UNAVAILABLE_TEXT = "天气摘要暂不可用"

        @JvmStatic
        fun fromWeatherState(locationId: String, state: UiState<HomeWeatherData>?): CityWeatherSummary {
            val data = state?.data ?: return unavailable(locationId)
            val cache = state.status == UiState.Status.CACHE
            val updateTime = if (cache) state.updateTime else data.updateTime
            return CityWeatherSummary(
                locationId,
                data.temperature,
                data.condition,
                data.tempMax,
                data.tempMin,
                updateTime,
                cache,
                ""
            )
        }

        @JvmStatic
        fun unavailable(locationId: String): CityWeatherSummary {
            return CityWeatherSummary(locationId, "", "", "", "", 0L, false, UNAVAILABLE_TEXT)
        }

        private fun requireText(value: String, fieldName: String) {
            require(value.trim().isNotEmpty()) { "$fieldName must not be empty" }
        }

        private fun requireOptionalText(value: String, fieldName: String) {
            require(value.isEmpty() || value.trim().isNotEmpty()) { "$fieldName must not be blank" }
        }
    }
}
