package com.litroenade.yunjiweather.ui.compose.screens

import androidx.compose.runtime.Composable
import com.litroenade.yunjiweather.data.entity.WarningEntity
import com.litroenade.yunjiweather.data.model.HomeWeatherData
import com.litroenade.yunjiweather.ui.compose.MessageCard
import com.litroenade.yunjiweather.ui.compose.home.modules.HomeModuleDefinition
import com.litroenade.yunjiweather.ui.compose.home.modules.HomeModuleKeys
import com.litroenade.yunjiweather.utils.HomeBlock

@Composable
internal fun HomeContentBlocks(
    data: HomeWeatherData,
    warnings: List<WarningEntity>,
    temperatureUnit: String,
    windUnit: String,
    modules: List<HomeModuleDefinition>,
    enabledModules: Map<String, Boolean>,
    onOpenAlerts: () -> Unit,
    onOpenLifeIndex: () -> Unit,
    onPersonalization: () -> Unit
) {
    val visibleModules = modules.filter { module -> enabledModules[module.key] ?: module.defaultEnabled }
    if (visibleModules.isEmpty()) {
        MessageCard(
            title = "首页模块已全部隐藏",
            message = "可在个性换肤右上角设置中恢复默认布局。",
            buttonText = "打开个性换肤",
            onButtonClick = onPersonalization
        )
        return
    }
    visibleModules.forEach { module ->
        when (module.key) {
            HomeBlock.WEATHER_METRICS.key -> WeatherMetricPanel(data, temperatureUnit, windUnit)
            HomeBlock.WIND_DETAIL.key -> WindDetailPanel(data, windUnit)
            HomeBlock.AIR_SUN.key -> SunAndAirPanel(data)
            HomeBlock.ADVICE.key -> AdvicePanel(data)
            HomeBlock.WEATHER_INSIGHT.key -> WeatherInsightPanel(data, warnings, onOpenAlerts, onOpenLifeIndex)
            HomeBlock.HOURLY_FORECAST.key -> HourlyForecast(data.hourlyForecasts, temperatureUnit)
            HomeBlock.DAILY_FORECAST.key -> DailyForecast(data.dailyForecasts, temperatureUnit, data.updateTime)
            HomeModuleKeys.CALENDAR_LIFE -> CalendarLifePanel(data, onOpenLifeIndex)
        }
    }
}
