package com.litroenade.yunjiweather.data.model;

import com.litroenade.yunjiweather.common.UiState;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CityWeatherSummaryTest {

    @Test
    public void fromWeatherState_mapsSuccessDataToSummary() {
        UiState<HomeWeatherData> state = UiState.success(homeWeather("上海", "26", "多云"));

        CityWeatherSummary summary = CityWeatherSummary.fromWeatherState("101020100", state);

        assertEquals("101020100", summary.getLocationId());
        assertEquals("26", summary.getTemperature());
        assertEquals("多云", summary.getCondition());
        assertEquals("31", summary.getTempMax());
        assertEquals("22", summary.getTempMin());
        assertEquals(1_700_000_000_000L, summary.getUpdateTime());
        assertFalse(summary.isFromCache());
        assertEquals("", summary.getErrorMessage());
    }

    @Test
    public void fromWeatherState_marksCacheSummary() {
        UiState<HomeWeatherData> state = UiState.cache(
                homeWeather("上海", "24", "晴"),
                "网络连接失败，已显示本地缓存。",
                1_699_999_980_000L
        );

        CityWeatherSummary summary = CityWeatherSummary.fromWeatherState("101020100", state);

        assertEquals("24", summary.getTemperature());
        assertEquals("晴", summary.getCondition());
        assertTrue(summary.isFromCache());
        assertEquals(1_699_999_980_000L, summary.getUpdateTime());
    }

    @Test
    public void fromWeatherState_returnsUnavailableWhenStateHasNoData() {
        UiState<HomeWeatherData> state = UiState.error("天气请求失败，请检查网络连接或天气服务配置。");

        CityWeatherSummary summary = CityWeatherSummary.fromWeatherState("101020100", state);

        assertEquals("101020100", summary.getLocationId());
        assertEquals("", summary.getTemperature());
        assertEquals("", summary.getCondition());
        assertEquals("天气摘要暂不可用", summary.getErrorMessage());
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
                "东南风",
                "2",
                "10",
                "1008",
                "18",
                "101",
                1_700_000_000_000L,
                "建议穿短袖或薄外套。",
                "天气适合出行，建议关注实时天气变化。",
                "55",
                "良",
                "PM2.5"
        );
    }
}
