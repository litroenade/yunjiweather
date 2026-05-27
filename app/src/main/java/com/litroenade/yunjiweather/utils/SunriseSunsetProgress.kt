package com.litroenade.yunjiweather.utils

object SunriseSunsetProgress {

    private const val UNAVAILABLE = "暂无日照数据"

    @JvmStatic
    fun calculate(sunrise: String, sunset: String, current: String): SunProgressState {
        val sunriseMinute = parseMinuteOfDay(sunrise) ?: return unavailable()
        val sunsetMinute = parseMinuteOfDay(sunset) ?: return unavailable()
        val currentMinute = parseMinuteOfDay(current) ?: return unavailable()
        if (sunsetMinute <= sunriseMinute) {
            return unavailable()
        }
        if (currentMinute < sunriseMinute) {
            return SunProgressState(0f, "日出前", true)
        }
        if (currentMinute > sunsetMinute) {
            return SunProgressState(1f, "已日落", true)
        }
        val progress = (currentMinute - sunriseMinute).toFloat() / (sunsetMinute - sunriseMinute).toFloat()
        return SunProgressState(progress.coerceIn(0f, 1f), "白昼中", true)
    }

    private fun unavailable(): SunProgressState {
        return SunProgressState(0f, UNAVAILABLE, false)
    }

    private fun parseMinuteOfDay(value: String?): Int? {
        if (value == null) {
            return null
        }
        val parts = value.trim().split(":")
        if (parts.size != 2) {
            return null
        }
        val hour = parts[0].toIntOrNull() ?: return null
        val minute = parts[1].toIntOrNull() ?: return null
        if (hour !in 0..23 || minute !in 0..59) {
            return null
        }
        return hour * 60 + minute
    }
}
