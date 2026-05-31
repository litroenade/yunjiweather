package com.litroenade.yunjiweather.data.model;

import com.litroenade.yunjiweather.common.UiState;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CityWeatherSummaryTest {

    @Test
    public void fromWeatherState_mapsSuccessDataToSummary() {
        UiState<HomeWeatherData> state = UiState.success(homeWeather("\u4e0a\u6d77", "26", "\u591a\u4e91"));

        CityWeatherSummary summary = CityWeatherSummary.fromWeatherState("101020100", state);

        assertEquals("101020100", summary.getLocationId());
        assertEquals("26", summary.getTemperature());
        assertEquals("101", summary.getIconCode());
        assertEquals("\u591a\u4e91", summary.getCondition());
        assertEquals("31", summary.getTempMax());
        assertEquals("22", summary.getTempMin());
        assertEquals(1_700_000_000_000L, summary.getUpdateTime());
        assertFalse(summary.isFromCache());
        assertEquals("", summary.getErrorMessage());
    }

    @Test
    public void fromWeatherState_marksCacheSummary() {
        UiState<HomeWeatherData> state = UiState.cache(
                homeWeather("\u4e0a\u6d77", "24", "\u6674"),
                "\u7f51\u7edc\u8fde\u63a5\u5931\u8d25\uff0c\u5df2\u663e\u793a\u672c\u5730\u7f13\u5b58\u3002",
                1_699_999_980_000L
        );

        CityWeatherSummary summary = CityWeatherSummary.fromWeatherState("101020100", state);

        assertEquals("24", summary.getTemperature());
        assertEquals("\u6674", summary.getCondition());
        assertTrue(summary.isFromCache());
        assertEquals(1_699_999_980_000L, summary.getUpdateTime());
    }

    @Test
    public void fromWeatherState_returnsUnavailableWhenStateHasNoData() {
        UiState<HomeWeatherData> state = UiState.error(
                "\u5929\u6c14\u8bf7\u6c42\u5931\u8d25\uff0c\u8bf7\u68c0\u67e5\u7f51\u7edc\u8fde\u63a5\u6216\u5929\u6c14\u670d\u52a1\u914d\u7f6e\u3002"
        );

        CityWeatherSummary summary = CityWeatherSummary.fromWeatherState("101020100", state);

        assertEquals("101020100", summary.getLocationId());
        assertEquals("", summary.getTemperature());
        assertEquals("", summary.getCondition());
        assertEquals("", summary.getIconCode());
        assertEquals("\u5929\u6c14\u6458\u8981\u6682\u4e0d\u53ef\u7528", summary.getErrorMessage());
    }

    private static HomeWeatherData homeWeather(String cityName, String temperature, String condition) {
        return new HomeWeatherData(
                cityName,
                "101020100",
                temperature,
                condition,
                "27",
                "31",
                "22",
                "68",
                "\u4e1c\u5357\u98ce",
                "2",
                "10",
                "1008",
                "18",
                "101",
                1_700_000_000_000L,
                "\u5efa\u8bae\u7a7f\u77ed\u8896\u6216\u8584\u5916\u5957\u3002",
                "\u5929\u6c14\u9002\u5408\u51fa\u884c\uff0c\u5efa\u8bae\u5173\u6ce8\u5b9e\u65f6\u5929\u6c14\u53d8\u5316\u3002",
                "55",
                "\u826f",
                "PM2.5"
        );
    }
}
