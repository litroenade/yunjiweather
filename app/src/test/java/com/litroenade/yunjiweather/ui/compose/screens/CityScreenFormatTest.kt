package com.litroenade.yunjiweather.ui.compose.screens

import com.litroenade.yunjiweather.utils.WeatherDisplayUtils
import org.junit.Assert.assertEquals
import org.junit.Test

class CityScreenFormatTest {

    @Test
    fun blankTemperatureUsesPlaceholder() {
        assertEquals("--℃", formatCitySummaryTemperature("", WeatherDisplayUtils.TEMPERATURE_CELSIUS))
        assertEquals("--°F", formatCitySummaryTemperature("  ", WeatherDisplayUtils.TEMPERATURE_FAHRENHEIT))
        assertEquals("--℃", formatCitySummaryTemperature(null, WeatherDisplayUtils.TEMPERATURE_CELSIUS))
    }

    @Test
    fun blankTemperatureRangeUsesLoadingText() {
        assertEquals("读取中", formatCitySummaryTemperatureRange("", "22", WeatherDisplayUtils.TEMPERATURE_CELSIUS))
        assertEquals("读取中", formatCitySummaryTemperatureRange("30", null, WeatherDisplayUtils.TEMPERATURE_CELSIUS))
    }

    @Test
    fun validTemperatureStillUsesConfiguredUnit() {
        assertEquals("26°", formatCitySummaryTemperature("26", WeatherDisplayUtils.TEMPERATURE_CELSIUS))
        assertEquals("79°F", formatCitySummaryTemperature("26", WeatherDisplayUtils.TEMPERATURE_FAHRENHEIT))
        assertEquals("86°F/72°F", formatCitySummaryTemperatureRange("30", "22", WeatherDisplayUtils.TEMPERATURE_FAHRENHEIT))
    }
}
