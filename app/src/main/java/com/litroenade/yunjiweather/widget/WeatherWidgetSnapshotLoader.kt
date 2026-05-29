package com.litroenade.yunjiweather.widget

import android.content.Context
import com.google.gson.Gson
import com.litroenade.yunjiweather.data.entity.CityEntity
import com.litroenade.yunjiweather.data.entity.WeatherCacheEntity
import com.litroenade.yunjiweather.data.local.WeatherCacheTypes
import com.litroenade.yunjiweather.data.model.HomeWeatherData
import com.litroenade.yunjiweather.utils.DateTimeUtils
import com.litroenade.yunjiweather.utils.DefaultCityUtils
import dagger.hilt.android.EntryPointAccessors

/**
 * 小组件只读取本地数据库缓存快照，不直接发起网络请求。
 * 后台刷新由统一任务调度，避免桌面频繁唤醒导致启动器卡顿或耗电。
 */
class WeatherWidgetSnapshotLoader(
    private val cityReader: DefaultCityReader,
    private val cacheReader: HomeWeatherCacheReader,
    private val gson: Gson
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
            WeatherWidgetSnapshot(
                cityName = data.cityName,
                temperatureText = "${data.temperature}°",
                conditionText = data.condition,
                rangeText = "${data.tempMin}° / ${data.tempMax}°",
                updateText = DateTimeUtils.formatMinuteTime(cache.updateTime),
                isAvailable = true,
                humidityText = "Humidity ${data.humidity}%",
                windText = "${data.windDir} ${data.windScale}",
                airQualityText = "AQI ${data.airQualityIndex} ${data.airQualityCategory}",
                adviceText = data.travelAdvice
            )
        } catch (exception: RuntimeException) {
            unavailable(cityName)
        }
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

    fun interface DefaultCityReader {
        fun readDefaultCity(): CityEntity?
    }

    fun interface HomeWeatherCacheReader {
        fun readHomeWeather(locationId: String): WeatherCacheEntity?
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
                Gson()
            )
        }
    }
}
