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
import java.util.Calendar

/**
 * Widget rendering reads only the local HOME cache; refresh scheduling owns network work.
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
        val settings = settingsReader.read()
        return fromCache(cityName, cache, settings)
    }

    private fun fromCache(
        cityName: String,
        cache: WeatherCacheEntity?,
        settings: WidgetThemeSettings
    ): WeatherWidgetSnapshot {
        val minuteOfDay = currentMinuteOfDay()
        if (cache == null) {
            return unavailable(cityName, settings, minuteOfDay)
        }
        return try {
            val data = gson.fromJson(cache.weatherJson, HomeWeatherData::class.java)
            data.validateForDisplay()
            WeatherWidgetSnapshotFactory.fromHomeWeather(
                data = data,
                updateText = DateTimeUtils.formatMinuteTime(cache.updateTime),
                customBackground = WeatherWidgetSnapshotFactory.customBackgroundForHomeWeather(
                    settings.visualThemeKey,
                    settings.customThemeProfile,
                    data,
                    minuteOfDay
                ),
                temperatureUnit = settings.temperatureUnit,
                visualThemeKey = settings.visualThemeKey
            )
        } catch (exception: RuntimeException) {
            unavailable(cityName, settings, minuteOfDay)
        }
    }

    private fun unavailable(
        cityName: String,
        settings: WidgetThemeSettings,
        minuteOfDay: Int
    ): WeatherWidgetSnapshot {
        return WeatherWidgetSnapshotFactory.unavailable(
            cityName,
            settings.visualThemeKey,
            WeatherWidgetSnapshotFactory.customBackgroundForFallback(
                settings.visualThemeKey,
                settings.customThemeProfile,
                minuteOfDay
            )
        )
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
                { WidgetThemeSettings(entryPoint.settingsRepository().visualTheme, entryPoint.settingsRepository().customThemeProfile, entryPoint.settingsRepository().temperatureUnit) }
            )
        }

        private fun currentMinuteOfDay(): Int {
            val calendar = Calendar.getInstance()
            return calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE)
        }
    }
}
