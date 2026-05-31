package com.litroenade.yunjiweather.ui.compose.screens

import com.litroenade.yunjiweather.data.model.CityWeatherSummary
import com.litroenade.yunjiweather.ui.location.LocationStatus
import com.litroenade.yunjiweather.utils.WeatherDisplayUtils
import org.junit.Assert.assertEquals
import org.junit.Test

class CityScreenFormatTest {

    @Test
    fun blankTemperatureUsesPlaceholder() {
        assertEquals("--\u2103", formatCitySummaryTemperature("", WeatherDisplayUtils.TEMPERATURE_CELSIUS))
        assertEquals("--\u00b0F", formatCitySummaryTemperature("  ", WeatherDisplayUtils.TEMPERATURE_FAHRENHEIT))
        assertEquals("--\u2103", formatCitySummaryTemperature(null, WeatherDisplayUtils.TEMPERATURE_CELSIUS))
    }

    @Test
    fun blankTemperatureRangeUsesLoadingText() {
        assertEquals("\u8bfb\u53d6\u4e2d", formatCitySummaryTemperatureRange("", "22", WeatherDisplayUtils.TEMPERATURE_CELSIUS))
        assertEquals("\u8bfb\u53d6\u4e2d", formatCitySummaryTemperatureRange("30", null, WeatherDisplayUtils.TEMPERATURE_CELSIUS))
    }

    @Test
    fun blankConditionUsesSummaryErrorInsteadOfFakeWeather() {
        assertEquals("\u5929\u6c14\u6458\u8981\u6682\u4e0d\u53ef\u7528", citySummaryConditionText(CityWeatherSummary.unavailable("101010100")))
    }

    @Test
    fun validTemperatureStillUsesConfiguredUnit() {
        assertEquals("26\u00b0", formatCitySummaryTemperature("26", WeatherDisplayUtils.TEMPERATURE_CELSIUS))
        assertEquals("79\u00b0F", formatCitySummaryTemperature("26", WeatherDisplayUtils.TEMPERATURE_FAHRENHEIT))
        assertEquals("86\u00b0F/72\u00b0F", formatCitySummaryTemperatureRange("30", "22", WeatherDisplayUtils.TEMPERATURE_FAHRENHEIT))
    }

    @Test
    fun manualSearchDoesNotKeepShowingStaleLocationError() {
        assertEquals(
            "",
            citySearchStatusText(
                busy = false,
                cityMessage = "",
                locationStatus = LocationStatus.ERROR,
                locationMessage = "\u6682\u672a\u83b7\u53d6\u5230\u5b9a\u4f4d\u7ed3\u679c\uff0c\u53ef\u7a0d\u540e\u91cd\u8bd5\u6216\u624b\u52a8\u641c\u7d22\u57ce\u5e02\u3002",
                manualSearchActive = true
            )
        )
    }

    @Test
    fun cityMessageTakesPrecedenceOverLocationMessage() {
        assertEquals(
            "\u8bf7\u9009\u62e9\u8981\u6dfb\u52a0\u7684\u57ce\u5e02",
            citySearchStatusText(
                busy = false,
                cityMessage = "\u8bf7\u9009\u62e9\u8981\u6dfb\u52a0\u7684\u57ce\u5e02",
                locationStatus = LocationStatus.ERROR,
                locationMessage = "\u6682\u672a\u83b7\u53d6\u5230\u5b9a\u4f4d\u7ed3\u679c",
                manualSearchActive = true
            )
        )
    }
}
