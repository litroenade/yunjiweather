package com.litroenade.yunjiweather.data.api;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class WeatherGatewayFactoryTest {

    @Test
    public void shouldUseQWeather_returnsTrueForConfiguredQWeatherCity() {
        assertTrue(WeatherGatewayFactory.shouldUseQWeather(true, "101010100"));
    }

    @Test
    public void shouldUseQWeather_returnsFalseWhenApiNotConfigured() {
        assertFalse(WeatherGatewayFactory.shouldUseQWeather(false, "101010100"));
    }

    @Test
    public void shouldUseQWeather_returnsFalseForOpenMeteoCity() {
        assertFalse(WeatherGatewayFactory.shouldUseQWeather(true, "openmeteo:1816670"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldUseQWeather_rejectsEmptyLocationId() {
        WeatherGatewayFactory.shouldUseQWeather(true, " ");
    }
}
