package com.litroenade.yunjiweather.utils;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class WeatherAdviceUtilsTest {

    @Test
    public void generateClothingAdvice_returnsHotWeatherAdviceAtThirtyDegrees() {
        String advice = WeatherAdviceUtils.generateClothingAdvice(30, "晴", 2, 50);

        assertEquals("建议穿短袖、短裤，并注意防晒和补水。", advice);
    }

    @Test
    public void generateClothingAdvice_returnsDownJacketAdviceBelowZero() {
        String advice = WeatherAdviceUtils.generateClothingAdvice(-1, "雪", 3, 40);

        assertEquals("建议穿羽绒服，搭配围巾和手套，减少长时间户外停留。", advice);
    }

    @Test
    public void generateTravelAdvice_combinesRainAndPoorAirQualityRisks() {
        String advice = WeatherAdviceUtils.generateTravelAdvice(22, "小雨", 2, 180, 3, false);

        assertTrue(advice.contains("雨伞"));
        assertTrue(advice.contains("路面湿滑"));
        assertTrue(advice.contains("减少户外活动"));
    }

    @Test
    public void generateTravelAdvice_warnsAboutStrongWindAndWarning() {
        String advice = WeatherAdviceUtils.generateTravelAdvice(18, "多云", 6, 60, 3, true);

        assertTrue(advice.contains("减少骑行"));
        assertTrue(advice.contains("远离临时搭建物"));
        assertTrue(advice.contains("关注天气预警"));
    }
}
