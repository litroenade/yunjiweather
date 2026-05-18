package com.litroenade.yunjiweather.ui.home;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class WeatherAnimationTypeTest {

    @Test
    public void fromIconCode_returnsSunnyForDayAndNightSunnyCodes() {
        assertEquals(WeatherAnimationType.SUNNY, WeatherAnimationType.fromIconCode("100"));
        assertEquals(WeatherAnimationType.SUNNY, WeatherAnimationType.fromIconCode("150"));
    }

    @Test
    public void fromIconCode_returnsRainForRainCodes() {
        assertEquals(WeatherAnimationType.RAIN, WeatherAnimationType.fromIconCode("305"));
        assertEquals(WeatherAnimationType.RAIN, WeatherAnimationType.fromIconCode("399"));
    }

    @Test
    public void fromIconCode_returnsSnowForSnowCodes() {
        assertEquals(WeatherAnimationType.SNOW, WeatherAnimationType.fromIconCode("400"));
    }

    @Test
    public void fromIconCode_returnsCloudyForUnknownOrEmptyCode() {
        assertEquals(WeatherAnimationType.CLOUDY, WeatherAnimationType.fromIconCode("104"));
        assertEquals(WeatherAnimationType.CLOUDY, WeatherAnimationType.fromIconCode(""));
        assertEquals(WeatherAnimationType.CLOUDY, WeatherAnimationType.fromIconCode(null));
    }
}
