package com.litroenade.yunjiweather.utils;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class WeatherIconUtilsTest {

    @Test
    public void getWeatherCategory_classifiesAnimationCategories() {
        assertEquals(WeatherIconUtils.WeatherCategory.SUNNY, WeatherIconUtils.getWeatherCategory("100"));
        assertEquals(WeatherIconUtils.WeatherCategory.NIGHT, WeatherIconUtils.getWeatherCategory("150"));
        assertEquals(WeatherIconUtils.WeatherCategory.RAIN, WeatherIconUtils.getWeatherCategory("305"));
        assertEquals(WeatherIconUtils.WeatherCategory.RAIN, WeatherIconUtils.getWeatherCategory("350"));
        assertEquals(WeatherIconUtils.WeatherCategory.SNOW, WeatherIconUtils.getWeatherCategory("400"));
        assertEquals(WeatherIconUtils.WeatherCategory.CLOUDY, WeatherIconUtils.getWeatherCategory("104"));
        assertEquals(WeatherIconUtils.WeatherCategory.CLOUDY, WeatherIconUtils.getWeatherCategory(""));
        assertEquals(WeatherIconUtils.WeatherCategory.CLOUDY, WeatherIconUtils.getWeatherCategory(null));
    }
}
