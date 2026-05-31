package com.litroenade.yunjiweather.widget

import android.content.Context
import com.google.gson.Gson
import com.litroenade.yunjiweather.data.entity.CityEntity
import com.litroenade.yunjiweather.data.entity.WeatherCacheEntity
import com.litroenade.yunjiweather.data.local.WeatherCacheTypes
import com.litroenade.yunjiweather.data.model.CustomThemeAsset
import com.litroenade.yunjiweather.data.model.CustomThemeResolver
import com.litroenade.yunjiweather.data.model.CustomThemeWeatherKey
import com.litroenade.yunjiweather.data.model.HomeWeatherData
import com.litroenade.yunjiweather.utils.DateTimeUtils
import com.litroenade.yunjiweather.utils.DefaultCityUtils
import com.litroenade.yunjiweather.utils.VisualThemeUtils
import com.litroenade.yunjiweather.utils.WeatherIconUtils
import dagger.hilt.android.EntryPointAccessors
import java.util.Calendar

/**
 * 小组件只读取本地数据库缓存快照，不直接发起网络请求。
 * 后台刷新由统一任务调度，避免桌面频繁唤醒导致启动器卡顿或耗电。
 */
class WeatherWidgetSnapshotLoader @JvmOverloads constructor(
    private val cityReader: DefaultCityReader,
    private val cacheReader: HomeWeatherCacheReader,
    private val gson: Gson,
    private val settingsReader: WidgetThemeSettingsReader = WidgetThemeSettingsReader { WidgetThemeSettings.defaults() }
) {

    fun load(): WeatherWidgetSnapshot {
        val city = cityReader.readDefaultCity()
        val cityName = city?.cityName ?: DefaultCityUtils.DEFAULT_CITY_NAME
        val locationId = city?.locationId ?: DefaultCityUtils.DEFAULT_LOCATION_ID
        val cache = cacheReader.readHomeWeather(locationId)
        return fromCache(cityName, cache)
    }

    private fun fromCache(cityName: String, cache: WeatherCacheEntity?): WeatherWidgetSnapshot {
        if (cache == null) {
            return unavailable(cityName)
        }
        return try {
            val data = gson.fromJson(cache.weatherJson, HomeWeatherData::class.java)
            data.validateForDisplay()
            val customBackground = customBackgroundFor(data)
            WeatherWidgetSnapshot(
                cityName = data.cityName,
                temperatureText = "${data.temperature}°",
                conditionText = data.condition,
                rangeText = "${data.tempMax}° / ${data.tempMin}°",
                updateText = DateTimeUtils.formatMinuteTime(cache.updateTime),
                isAvailable = true,
                humidityText = "湿度 ${data.humidity}%",
                windText = "${data.windDir} ${data.windScale}",
                airQualityText = "空气 ${data.airQualityCategory}",
                adviceText = data.travelAdvice,
                clothingValue = clothingValue(data.temperature, data.clothingAdvice),
                fishingValue = fishingValue(data.condition),
                sunsetValue = sunsetValue(data.condition),
                coldValue = coldValue(data.temperature),
                customBackgroundUri = customBackground.uri,
                customBackgroundCropAnchor = customBackground.cropAnchor,
                customBackgroundMediaType = customBackground.mediaType
            )
        } catch (exception: RuntimeException) {
            unavailable(cityName)
        }
    }

    private fun customBackgroundFor(data: HomeWeatherData): CustomThemeAsset {
        val settings = settingsReader.read()
        if (settings.visualThemeKey != VisualThemeUtils.THEME_CUSTOM_1 || settings.customThemeProfile.isEmpty) {
            return CustomThemeAsset.empty()
        }
        val category = WeatherIconUtils.getWeatherCategory(data.iconCode)
        val weatherKey = CustomThemeWeatherKey.fromWeatherCategory(category)
        val night = category == WeatherIconUtils.WeatherCategory.NIGHT
        return CustomThemeResolver.resolve(settings.customThemeProfile, weatherKey, night, currentMinuteOfDay())
    }

    private fun unavailable(cityName: String): WeatherWidgetSnapshot {
        return WeatherWidgetSnapshot(
            cityName = cityName,
            temperatureText = "打开查看实时天气",
            conditionText = "暂无缓存",
            rangeText = "城市、预警、生活建议",
            updateText = "",
            isAvailable = false
        )
    }

    private fun clothingValue(temperature: String, advice: String): String {
        val normalizedAdvice = advice.trim()
        return when {
            normalizedAdvice.contains("短袖") -> "短袖"
            normalizedAdvice.contains("外套") -> "外套"
            normalizedAdvice.contains("羽绒") -> "羽绒服"
            normalizedAdvice.contains("毛衣") -> "毛衣"
            parseTemperature(temperature) >= 26 -> "短袖"
            parseTemperature(temperature) <= 10 -> "厚外套"
            else -> "薄外套"
        }
    }

    private fun fishingValue(condition: String): String {
        return if (condition.contains("雨") || condition.contains("雪") || condition.contains("雷")) {
            "不宜"
        } else {
            "适宜"
        }
    }

    private fun sunsetValue(condition: String): String {
        return if (condition.contains("晴") || condition.contains("少云")) {
            "较好"
        } else {
            "一般"
        }
    }

    private fun coldValue(temperature: String): String {
        return if (parseTemperature(temperature) <= 12) "注意" else "不易"
    }

    private fun parseTemperature(temperature: String): Int {
        return temperature.trim().toDoubleOrNull()?.toInt() ?: 20
    }

    fun interface DefaultCityReader {
        fun readDefaultCity(): CityEntity?
    }

    fun interface HomeWeatherCacheReader {
        fun readHomeWeather(locationId: String): WeatherCacheEntity?
    }

    fun interface WidgetThemeSettingsReader {
        fun read(): WidgetThemeSettings
    }

    companion object {
        @JvmStatic
        fun fromContext(context: Context): WeatherWidgetSnapshotLoader {
            val entryPoint = EntryPointAccessors.fromApplication(
                context.applicationContext,
                WeatherWidgetEntryPoint::class.java
            )
            return WeatherWidgetSnapshotLoader(
                { entryPoint.cityDao().findDefaultCity() },
                { locationId ->
                    entryPoint.weatherCacheDao().findByLocationAndType(locationId, WeatherCacheTypes.HOME)
                },
                Gson(),
                { WidgetThemeSettings(entryPoint.settingsRepository().visualTheme, entryPoint.settingsRepository().customThemeProfile) }
            )
        }

        private fun currentMinuteOfDay(): Int {
            val calendar = Calendar.getInstance()
            return calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE)
        }
    }
}
