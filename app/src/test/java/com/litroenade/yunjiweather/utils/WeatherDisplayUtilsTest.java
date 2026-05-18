package com.litroenade.yunjiweather.utils;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class WeatherDisplayUtilsTest {

    @Test
    public void formatTemperature_usesCelsiusByDefault() {
        assertEquals("24°", WeatherDisplayUtils.formatTemperature("24", "C"));
    }

    @Test
    public void formatTemperature_convertsCelsiusToFahrenheit() {
        assertEquals("75°F", WeatherDisplayUtils.formatTemperature("24", "F"));
    }

    @Test
    public void formatWind_usesWindScaleMode() {
        assertEquals("东南风 3级", WeatherDisplayUtils.formatWind("东南风", "3", "18", "SCALE"));
    }

    @Test
    public void formatWind_convertsKilometerPerHourToMeterPerSecond() {
        assertEquals("东南风 5.0 m/s", WeatherDisplayUtils.formatWind("东南风", "3", "18", "MS"));
    }
}
