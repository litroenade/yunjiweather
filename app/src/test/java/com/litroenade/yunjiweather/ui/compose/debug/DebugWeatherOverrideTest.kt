package com.litroenade.yunjiweather.ui.compose.debug

import com.litroenade.yunjiweather.data.model.HomeWeatherData
import com.litroenade.yunjiweather.ui.compose.WeatherLightContext
import org.junit.Assert.assertEquals
import org.junit.Test

class DebugWeatherOverrideTest {

    @Test
    fun clearWeatherUsesNightIconWhenNightTimeSelected() {
        val override = DebugWeatherOverride(
            weather = DebugWeatherPresets.weatherPreset("sunny"),
            time = DebugWeatherPresets.timePreset("night")
        )

        val result = override.applyTo(homeWeather())

        assertEquals("150", result.iconCode)
        assertEquals("晴", result.condition)
        assertEquals("29", result.temperature)
    }

    @Test
    fun rainWeatherKeepsRainIconWhenNightTimeSelected() {
        val override = DebugWeatherOverride(
            weather = DebugWeatherPresets.weatherPreset("rain"),
            time = DebugWeatherPresets.timePreset("night")
        )

        val result = override.applyTo(homeWeather())

        assertEquals("350", result.iconCode)
        assertEquals("小雨", result.condition)
    }

    @Test
    fun cloudyWeatherUsesNightCloudIconWhenNightTimeSelected() {
        val override = DebugWeatherOverride(
            weather = DebugWeatherPresets.weatherPreset("cloudy"),
            time = DebugWeatherPresets.timePreset("night")
        )

        val result = override.applyTo(homeWeather())

        assertEquals("151", result.iconCode)
        assertEquals("多云", result.condition)
    }

    @Test
    fun selectedTimePresetCreatesMatchingLightContext() {
        val override = DebugWeatherOverride(
            weather = DebugWeatherPresets.weatherPreset("cloudy"),
            time = DebugWeatherPresets.timePreset("night")
        )

        val context = override.lightContext("06:00", "18:00")

        assertEquals(WeatherLightContext.Phase.NIGHT, context.phase)
        assertEquals(true, context.isNight)
    }

    private fun homeWeather(): HomeWeatherData {
        return HomeWeatherData(
            "上海",
            "101020100",
            "24",
            "多云",
            "25",
            "28",
            "21",
            "76",
            "东风",
            "1",
            "5",
            "1012",
            "12",
            "101",
            1L,
            "体感舒适。",
            "适合出行。",
            "42",
            "优",
            "无",
            "5",
            "06:00",
            "18:00",
            emptyList(),
            emptyList()
        )
    }
}
