package com.litroenade.yunjiweather.ui.city;

import com.litroenade.yunjiweather.data.entity.CityEntity;
import com.litroenade.yunjiweather.data.model.CityWeatherSummary;
import com.litroenade.yunjiweather.utils.WeatherDisplayUtils;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CitySummaryFormatterTest {

    @Test
    public void formatSummary_withoutSummary_showsLoadingText() {
        CityEntity city = city("北京", "北京", "中国");

        String summary = CitySummaryFormatter.format(city, true, null, WeatherDisplayUtils.TEMPERATURE_CELSIUS);

        assertEquals("北京 · 中国 · 默认城市\n天气摘要加载中", summary);
    }

    @Test
    public void formatSummary_withError_showsErrorMessage() {
        CityEntity city = city("上海", "上海", "中国");
        CityWeatherSummary summary = CityWeatherSummary.unavailable("101020100");

        String text = CitySummaryFormatter.format(city, false, summary, WeatherDisplayUtils.TEMPERATURE_CELSIUS);

        assertEquals("上海 · 中国\n天气摘要暂不可用", text);
    }

    @Test
    public void formatSummary_withCacheWeather_marksCache() {
        CityEntity city = city("广州", "广东", "中国");
        CityWeatherSummary summary = new CityWeatherSummary(
                "101280101",
                "30",
                "晴",
                "34",
                "26",
                1000L,
                true,
                ""
        );

        String text = CitySummaryFormatter.format(city, false, summary, WeatherDisplayUtils.TEMPERATURE_CELSIUS);

        assertEquals("广东 · 中国\n晴 30°  今日 26° / 34° · 缓存", text);
    }

    private static CityEntity city(String cityName, String province, String country) {
        return new CityEntity(1L, cityName, "101010100", province, country, 1.0, 2.0, false, 0, 1L, 1L);
    }
}
