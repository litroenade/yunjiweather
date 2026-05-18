package com.litroenade.yunjiweather.utils;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class WeatherCodeMapperTest {

    @Test
    public void toCondition_returnsSunnyForClearCode() {
        assertEquals("晴", WeatherCodeMapper.toCondition(0));
    }

    @Test
    public void toCondition_returnsRainForShowerCode() {
        assertEquals("阵雨", WeatherCodeMapper.toCondition(81));
    }

    @Test
    public void toCondition_returnsThunderstormForHailCode() {
        assertEquals("雷雨", WeatherCodeMapper.toCondition(99));
    }

    @Test
    public void toIconCode_usesNightSunnyIconForClearNight() {
        assertEquals("150", WeatherCodeMapper.toIconCode(0, false));
    }

    @Test(expected = IllegalArgumentException.class)
    public void toCondition_rejectsUnsupportedCode() {
        WeatherCodeMapper.toCondition(777);
    }
}
