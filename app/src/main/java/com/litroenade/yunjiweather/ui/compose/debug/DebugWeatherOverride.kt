package com.litroenade.yunjiweather.ui.compose.debug

import com.litroenade.yunjiweather.data.model.HomeWeatherData
import com.litroenade.yunjiweather.ui.compose.WeatherLightContext

internal data class DebugWeatherTimePreset(
    val key: String,
    val title: String,
    val subtitle: String,
    val minuteOfDay: Int,
    val usesNightIcon: Boolean
)

internal data class DebugWeatherPreset(
    val key: String,
    val title: String,
    val subtitle: String,
    val temperature: String,
    val feelsLike: String,
    val tempMax: String,
    val tempMin: String,
    val condition: String,
    val iconCode: String,
    val nightIconCode: String,
    val windDir: String,
    val windScale: String,
    val windSpeed: String,
    val visibility: String,
    val airQualityIndex: String,
    val airQualityCategory: String,
    val primaryPollutant: String,
    val uvIndex: String,
    val clothingAdvice: String,
    val travelAdvice: String
) {
    fun applyTo(data: HomeWeatherData, time: DebugWeatherTimePreset): HomeWeatherData {
        val displayIconCode = iconCodeFor(time)
        return HomeWeatherData(
            data.cityName,
            data.locationId,
            temperature,
            condition,
            feelsLike,
            tempMax,
            tempMin,
            data.humidity,
            windDir,
            windScale,
            windSpeed,
            data.pressure,
            visibility,
            displayIconCode,
            data.updateTime,
            clothingAdvice,
            travelAdvice,
            airQualityIndex,
            airQualityCategory,
            primaryPollutant,
            uvIndex,
            data.sunrise,
            data.sunset,
            data.hourlyForecasts.map { item ->
                item.copy(condition = condition, iconCode = displayIconCode)
            },
            data.dailyForecasts.map { item ->
                item.copy(condition = condition, iconCode = displayIconCode)
            }
        )
    }

    private fun iconCodeFor(time: DebugWeatherTimePreset): String {
        return if (time.usesNightIcon && nightIconCode.isNotBlank()) {
            nightIconCode
        } else {
            iconCode
        }
    }
}

internal data class DebugWeatherOverride(
    val weather: DebugWeatherPreset,
    val time: DebugWeatherTimePreset
) {
    val title: String
        get() = "${time.title} · ${weather.title}"

    fun applyTo(data: HomeWeatherData): HomeWeatherData {
        return weather.applyTo(data, time)
    }

    fun lightContext(sunrise: String, sunset: String): WeatherLightContext {
        return WeatherLightContext.fromMinuteOfDay(sunrise, sunset, time.minuteOfDay)
    }
}

internal object DebugWeatherPresets {
    val timePresets = listOf(
        DebugWeatherTimePreset(
            key = "dawn",
            title = "清晨",
            subtitle = "06:20 · 低角度暖光",
            minuteOfDay = 380,
            usesNightIcon = false
        ),
        DebugWeatherTimePreset(
            key = "day",
            title = "正午",
            subtitle = "12:30 · 高亮直射",
            minuteOfDay = 750,
            usesNightIcon = false
        ),
        DebugWeatherTimePreset(
            key = "dusk",
            title = "傍晚",
            subtitle = "18:30 · 暖色斜光",
            minuteOfDay = 1110,
            usesNightIcon = false
        ),
        DebugWeatherTimePreset(
            key = "night",
            title = "深夜",
            subtitle = "22:30 · 月光与星空",
            minuteOfDay = 1350,
            usesNightIcon = true
        )
    )

    val weatherPresets = listOf(
        DebugWeatherPreset(
            key = "sunny",
            title = "晴天",
            subtitle = "强光、低云量、空气较好",
            temperature = "29",
            feelsLike = "31",
            tempMax = "32",
            tempMin = "23",
            condition = "晴",
            iconCode = "100",
            nightIconCode = "150",
            windDir = "东南风",
            windScale = "2",
            windSpeed = "10",
            visibility = "18",
            airQualityIndex = "42",
            airQualityCategory = "优",
            primaryPollutant = "无",
            uvIndex = "8",
            clothingAdvice = "白天气温偏高，建议穿轻薄透气衣物。",
            travelAdvice = "天气晴朗，适合出行，注意防晒补水。"
        ),
        DebugWeatherPreset(
            key = "cloudy",
            title = "多云",
            subtitle = "弱光、云层、常规首页状态",
            temperature = "24",
            feelsLike = "25",
            tempMax = "28",
            tempMin = "21",
            condition = "多云",
            iconCode = "101",
            nightIconCode = "151",
            windDir = "东北风",
            windScale = "2",
            windSpeed = "12",
            visibility = "14",
            airQualityIndex = "58",
            airQualityCategory = "良",
            primaryPollutant = "PM2.5",
            uvIndex = "4",
            clothingAdvice = "体感舒适，早晚可加一件薄外套。",
            travelAdvice = "云量较多，整体适宜通勤和户外活动。"
        ),
        DebugWeatherPreset(
            key = "rain",
            title = "小雨",
            subtitle = "雨层动效、湿度场景",
            temperature = "19",
            feelsLike = "18",
            tempMax = "21",
            tempMin = "17",
            condition = "小雨",
            iconCode = "305",
            nightIconCode = "350",
            windDir = "北风",
            windScale = "3",
            windSpeed = "18",
            visibility = "8",
            airQualityIndex = "35",
            airQualityCategory = "优",
            primaryPollutant = "无",
            uvIndex = "1",
            clothingAdvice = "雨天体感偏凉，建议携带雨具并加外套。",
            travelAdvice = "道路湿滑，出行请预留时间。"
        ),
        DebugWeatherPreset(
            key = "snow",
            title = "大雪",
            subtitle = "雪粒动效、低温场景",
            temperature = "-4",
            feelsLike = "-8",
            tempMax = "-1",
            tempMin = "-7",
            condition = "大雪",
            iconCode = "402",
            nightIconCode = "456",
            windDir = "西北风",
            windScale = "4",
            windSpeed = "24",
            visibility = "4",
            airQualityIndex = "66",
            airQualityCategory = "良",
            primaryPollutant = "PM10",
            uvIndex = "1",
            clothingAdvice = "低温降雪，建议穿厚羽绒服并注意防滑。",
            travelAdvice = "积雪可能影响交通，减少不必要外出。"
        )
    )

    fun defaultOverride(): DebugWeatherOverride {
        return DebugWeatherOverride(
            weather = weatherPreset("sunny"),
            time = timePreset("day")
        )
    }

    fun weatherPreset(key: String): DebugWeatherPreset {
        return weatherPresets.first { preset -> preset.key == key }
    }

    fun timePreset(key: String): DebugWeatherTimePreset {
        return timePresets.first { preset -> preset.key == key }
    }
}
