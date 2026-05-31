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
 * 灏忕粍浠跺彧璇诲彇鏈湴鏁版嵁搴撶紦瀛樺揩鐓э紝涓嶇洿鎺ュ彂璧风綉缁滆姹傘€? * 鍚庡彴鍒锋柊鐢辩粺涓€浠诲姟璋冨害锛岄伩鍏嶆闈㈤绻佸敜閱掑鑷村惎鍔ㄥ櫒鍗￠】鎴栬€楃數銆? */
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
            return WeatherWidgetSnapshotFactory.unavailable(cityName)
        }
        return try {
            val data = gson.fromJson(cache.weatherJson, HomeWeatherData::class.java)
            data.validateForDisplay()
            WeatherWidgetSnapshotFactory.fromHomeWeather(
                data = data,
                updateText = DateTimeUtils.formatMinuteTime(cache.updateTime),
                customBackground = customBackgroundFor(data)
            )
        } catch (exception: RuntimeException) {
            WeatherWidgetSnapshotFactory.unavailable(cityName)
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
