package com.litroenade.yunjiweather.utils;

import com.litroenade.yunjiweather.R;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class WeatherIconUtilsTest {

    @Test
    public void getWeatherBackgroundRes_returnsSunnyForDaySunnyCode() {
        assertEquals(R.drawable.bg_weather_sunny, WeatherIconUtils.getWeatherBackgroundRes("100"));
    }

    @Test
    public void getWeatherBackgroundRes_returnsNightForNightSunnyCode() {
        assertEquals(R.drawable.bg_weather_night, WeatherIconUtils.getWeatherBackgroundRes("150"));
    }

    @Test
    public void getWeatherBackgroundRes_returnsRainForRainCode() {
        assertEquals(R.drawable.bg_weather_rain, WeatherIconUtils.getWeatherBackgroundRes("305"));
    }

    @Test
    public void getWeatherBackgroundRes_keepsNightRainInRainPalette() {
        assertEquals(R.drawable.bg_weather_rain, WeatherIconUtils.getWeatherBackgroundRes("350"));
    }

    @Test
    public void getWeatherBackgroundRes_returnsSnowForSnowCode() {
        assertEquals(R.drawable.bg_weather_snow, WeatherIconUtils.getWeatherBackgroundRes("401"));
    }
}
